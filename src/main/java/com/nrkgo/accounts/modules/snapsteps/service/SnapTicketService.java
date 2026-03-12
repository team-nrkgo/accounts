package com.nrkgo.accounts.modules.snapsteps.service;

import com.nrkgo.accounts.modules.snapsteps.dto.SnapTicketDto;
import com.nrkgo.accounts.modules.snapsteps.model.SnapTicket;

public interface SnapTicketService {
    SnapTicket createTicket(SnapTicketDto ticketDto, Long orgId);
}
