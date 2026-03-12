package com.nrkgo.accounts.modules.snapsteps.repository;

import com.nrkgo.accounts.modules.snapsteps.model.SnapTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SnapTicketRepository extends JpaRepository<SnapTicket, Long> {
}
