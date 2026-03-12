package com.nrkgo.accounts.modules.snapsteps.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "ss_tickets")
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class SnapTicket extends com.nrkgo.accounts.model.BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "org_id")
    private Long orgId;

    private String email;
    private String subject;
    private String message;

    @Column(name = "browser_info")
    private String browserInfo;

    private String status = "OPEN";
}
