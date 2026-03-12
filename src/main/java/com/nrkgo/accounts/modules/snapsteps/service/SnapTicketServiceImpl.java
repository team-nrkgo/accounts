package com.nrkgo.accounts.modules.snapsteps.service;

import com.nrkgo.accounts.modules.snapsteps.dto.SnapTicketDto;
import com.nrkgo.accounts.modules.snapsteps.model.SnapTicket;
import com.nrkgo.accounts.modules.snapsteps.repository.SnapTicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SnapTicketServiceImpl implements SnapTicketService {

    private final SnapTicketRepository ticketRepository;

    public SnapTicketServiceImpl(SnapTicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    @Transactional
    public SnapTicket createTicket(SnapTicketDto ticketDto, Long orgId) {
        SnapTicket ticket = new SnapTicket();
        ticket.setEmail(ticketDto.getEmail());
        ticket.setSubject(ticketDto.getSubject());
        ticket.setMessage(ticketDto.getMessage());
        ticket.setUserId(ticketDto.getUserId());
        ticket.setOrgId(orgId);
        ticket.setBrowserInfo(ticketDto.getBrowserInfo());
        ticket.setStatus("OPEN");

        return ticketRepository.save(ticket);
    }
}
