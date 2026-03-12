package com.nrkgo.accounts.modules.sign.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "sign_recipients")
public class SignRecipient implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "envelope_id")
    private Long envelopeId;

    private String email;
    private String name;
    private String role; // e.g., 'signer', 'viewer'
    private Integer routingOrder;

    @Column(name = "signing_order")
    private Integer signingOrder;

    @Column(name = "access_token")
    private String accessToken;

    private String status; // e.g., 'waiting', 'sent', 'delivered', 'completed'

    public SignRecipient() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEnvelopeId() {
        return envelopeId;
    }

    public void setEnvelopeId(Long envelopeId) {
        this.envelopeId = envelopeId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Integer getRoutingOrder() {
        return routingOrder;
    }

    public void setRoutingOrder(Integer routingOrder) {
        this.routingOrder = routingOrder;
    }

    public Integer getSigningOrder() {
        return signingOrder;
    }

    public void setSigningOrder(Integer signingOrder) {
        this.signingOrder = signingOrder;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
