package com.nrkgo.accounts.modules.snapsteps.service;

import com.nrkgo.accounts.modules.snapsteps.dto.SnapGuideDto;
import com.nrkgo.accounts.modules.snapsteps.model.SnapGuide;
import com.nrkgo.accounts.model.User;
import java.util.List;

public interface SnapGuideService {
        SnapGuide saveGuide(SnapGuideDto guideDto, User user, Long orgId);

        List<SnapGuide> getGuidesForUser(User user, Long orgId);

        List<SnapGuide> getStarredGuides(User user, Long orgId);

        List<SnapGuide> searchGuides(User user, Long orgId, String query);

        org.springframework.data.domain.Page<SnapGuide> getGuidesForUser(User user, Long orgId,
                        org.springframework.data.domain.Pageable pageable);

        org.springframework.data.domain.Page<SnapGuide> searchGuides(User user, Long orgId, String query,
                        org.springframework.data.domain.Pageable pageable);

        SnapGuide getGuideById(String id, User user, Long orgId);

        SnapGuide getGuideByNumericId(Long id, User user, Long orgId);

        SnapGuide updateGuide(Long id, SnapGuideDto guideDto, User user, Long orgId);

        void deleteGuide(String id, User user, Long orgId);

        void deleteGuideByNumericId(Long id, User user, Long orgId);

        byte[] exportGuide(Long id, String format, User user, Long orgId);

        org.springframework.data.domain.Page<SnapGuide> getStarredGuides(User user, Long orgId,
                        org.springframework.data.domain.Pageable pageable);
}
