package com.nrkgo.accounts.modules.scheduler.service;

import com.nrkgo.accounts.modules.scheduler.model.JobArguments;
import com.nrkgo.accounts.modules.scheduler.model.JobTrigger;
import com.nrkgo.accounts.modules.scheduler.repository.JobArgumentsRepository;
import com.nrkgo.accounts.modules.scheduler.repository.JobRepeatRuleRepository;
import com.nrkgo.accounts.modules.scheduler.repository.JobTriggerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerService.class);

    private final JobArgumentsRepository argsRepository;
    private final JobTriggerRepository triggerRepository;
    private final JobRepeatRuleRepository repeatRuleRepository;
    private final Map<String, JobExecutor> executors;

    public SchedulerService(
            JobArgumentsRepository argsRepository,
            JobTriggerRepository triggerRepository,
            JobRepeatRuleRepository repeatRuleRepository,
            List<JobExecutor> executorList) {
        this.argsRepository = argsRepository;
        this.triggerRepository = triggerRepository;
        this.repeatRuleRepository = repeatRuleRepository;
        this.executors = executorList.stream()
                .collect(Collectors.toMap(JobExecutor::getJobClass, Function.identity()));
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void heartbeat() {
        long now = System.currentTimeMillis();
        List<JobTrigger> pending = triggerRepository.findPendingTriggers(JobTrigger.WAIT, now, 10);

        if (pending.isEmpty())
            return;

        List<Long> ids = pending.stream().map(JobTrigger::getId).toList();

        int updatedCount = triggerRepository.updateStatusForIds(ids, JobTrigger.WAIT, JobTrigger.RUNNING, now, 0L);

        if (updatedCount > 0) {
            for (JobTrigger trigger : pending) {
                // We re-check the status because the atomic update might have failed for some
                // in the batch if they were modified
                triggerRepository.findById(trigger.getId()).ifPresent(t -> {
                    if (t.getStatus() == JobTrigger.RUNNING) {
                        executeJob(t);
                    }
                });
            }
        }
    }

    private void executeJob(JobTrigger trigger) {
        JobArguments args = argsRepository.findById(trigger.getJobArgsId()).orElse(null);
        if (args == null) {
            handleFailure(trigger, "Parent JobArguments not found");
            return;
        }

        JobExecutor executor = executors.get(args.getJobClass());
        if (executor == null) {
            handleFailure(trigger, "No executor found for class: " + args.getJobClass());
            return;
        }

        try {
            executor.execute(trigger, args);
            trigger.setStatus(JobTrigger.FINISHED);
            triggerRepository.save(trigger);

            if (args.getJobType() == 1) {
                handleRepeats(args, trigger);
            }
        } catch (Exception e) {
            handleFailure(trigger, e.getMessage());
        }
    }

    private void handleFailure(JobTrigger trigger, String error) {
        int newRetryCount = (trigger.getRetryCount() == null ? 0 : trigger.getRetryCount()) + 1;
        trigger.setRetryCount(newRetryCount);
        trigger.setLastError(error);
        trigger.setModifiedTime(System.currentTimeMillis());

        if (newRetryCount >= 3) {
            trigger.setStatus(JobTrigger.FAILED);
        } else {
            trigger.setStatus(JobTrigger.WAIT);
            long delay = (newRetryCount == 1) ? 5 : (newRetryCount == 2 ? 15 : 60);
            trigger.setStartTime(System.currentTimeMillis() + (delay * 60000));
        }
        triggerRepository.save(trigger);
    }

    private void handleRepeats(JobArguments args, JobTrigger lastTrigger) {
        repeatRuleRepository.findByJobArgsId(args.getId()).ifPresent(rule -> {
            boolean shouldRepeat = rule.getRepeatCount() == -1 || rule.getRepeatCount() > 0;
            long nextTime = calculateNextTime(lastTrigger.getStartTime(), rule.getRepeatType());

            if (shouldRepeat && (rule.getEndTime() == null || nextTime <= rule.getEndTime())) {
                if (rule.getRepeatCount() > 0) {
                    rule.setRepeatCount(rule.getRepeatCount() - 1);
                    repeatRuleRepository.save(rule);
                }

                JobTrigger nextTrigger = new JobTrigger();
                nextTrigger.setJobArgsId(args.getId());
                nextTrigger.setStartTime(nextTime);
                nextTrigger.setStatus(JobTrigger.WAIT);
                nextTrigger.setCreatedBy(0L);
                nextTrigger = triggerRepository.save(nextTrigger);

                args.setActiveTriggerId(nextTrigger.getId());
                argsRepository.save(args);
            }
        });
    }

    private long calculateNextTime(long lastTime, int type) {
        long day = 24 * 60 * 60 * 1000L;
        return switch (type) {
            case 1 -> lastTime + day;
            case 2 -> lastTime + (7 * day);
            case 3 -> lastTime + (30 * day);
            default -> lastTime + day;
        };
    }

    @Transactional
    public void scheduleSingle(String jobClass, Long entityId, Long startTime, Long orgId, Long userId,
            Integer productCode, String jsonData, String argRef) {
        // 1. Identity Check
        JobArguments args = argsRepository
                .findByOrgIdAndProductCodeAndJobClassAndEntityId(orgId, productCode, jobClass, entityId)
                .orElse(new JobArguments());

        // 2. Cancel existing trigger if any
        if (args.getActiveTriggerId() != null) {
            triggerRepository.findById(args.getActiveTriggerId()).ifPresent(t -> {
                if (t.getStatus() == JobTrigger.WAIT) {
                    t.setStatus(JobTrigger.CANCELLED);
                    t.setModifiedBy(userId);
                    triggerRepository.save(t);
                }
            });
        }

        // 3. Setup Parent
        args.setOrgId(orgId);
        args.setProductCode(productCode);
        args.setJobClass(jobClass);
        args.setEntityId(entityId);
        args.setArgRef(argRef);
        args.setUserId(userId);
        args.setJobType(0);
        args.setJsonData(jsonData);
        args.setModifiedBy(userId);
        if (args.getId() == null)
            args.setCreatedBy(userId);

        args = argsRepository.save(args);

        // 4. Create Trigger
        JobTrigger trigger = new JobTrigger();
        trigger.setJobArgsId(args.getId());
        trigger.setStartTime(startTime);
        trigger.setCreatedBy(userId);
        trigger = triggerRepository.save(trigger);

        // 5. Store Pointer
        args.setActiveTriggerId(trigger.getId());
        argsRepository.save(args);
    }

    @Transactional
    public void cancelJobByEntityId(String jobClass, Long entityId, Long orgId, Integer productCode, Long userId) {
        argsRepository.findByOrgIdAndProductCodeAndJobClassAndEntityId(orgId, productCode, jobClass, entityId)
                .ifPresent(args -> {
                    if (args.getActiveTriggerId() != null) {
                        triggerRepository.findById(args.getActiveTriggerId()).ifPresent(t -> {
                            if (t.getStatus() == JobTrigger.WAIT) {
                                t.setStatus(JobTrigger.CANCELLED);
                                t.setModifiedBy(userId);
                                triggerRepository.save(t);
                            }
                        });
                    }
                    args.setStatus(1); // Deactivate Parent
                    args.setModifiedBy(userId);
                    argsRepository.save(args);
                });
    }

    /**
     * Purges old finished or cancelled jobs.
     * Logic is ready but inactive (commented out @Scheduled).
     */
    // @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void purgeOldJobs() {
        // 30 days history
        long thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);
        int deletedCount = triggerRepository.deleteOldFinishedJobs(thirtyDaysAgo);
        if (deletedCount > 0) {
            logger.info("Purged {} old job execution records from history.", deletedCount);
        }
    }
}
