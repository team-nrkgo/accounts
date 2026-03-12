package com.nrkgo.accounts.modules.integrations.model;

import jakarta.persistence.*;

@Entity
@Table(name = "org_integrations")
public class OrgIntegration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "org_id", nullable = false)
    private Long orgId;

    @Column(name = "product_code", nullable = false)
    private Integer productCode;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ext_account_id", nullable = false)
    private ExternalAccount externalAccount;

    @Column(name = "config_json", columnDefinition = "JSON")
    private String configJson;

    @Column(nullable = false)
    private Integer status = 1;

    @Column(name = "created_time")
    private Long createdTime;

    @PrePersist
    protected void onCreate() {
        this.createdTime = System.currentTimeMillis();
    }

    // Manual Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public Integer getProductCode() {
        return productCode;
    }

    public void setProductCode(Integer productCode) {
        this.productCode = productCode;
    }

    public ExternalAccount getExternalAccount() {
        return externalAccount;
    }

    public void setExternalAccount(ExternalAccount externalAccount) {
        this.externalAccount = externalAccount;
    }

    public String getConfigJson() {
        return configJson;
    }

    public void setConfigJson(String configJson) {
        this.configJson = configJson;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }
}
