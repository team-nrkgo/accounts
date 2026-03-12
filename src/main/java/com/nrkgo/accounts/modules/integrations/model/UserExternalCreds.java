package com.nrkgo.accounts.modules.integrations.model;

import jakarta.persistence.*;

@Entity
@Table(name = "user_external_creds")
public class UserExternalCreds {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ext_account_id", nullable = false)
    private ExternalAccount externalAccount;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_type", nullable = false)
    private AuthType authType;

    @Column(name = "token_main", nullable = false, columnDefinition = "LONGTEXT")
    private String tokenMain;

    @Column(name = "token_secret", columnDefinition = "LONGTEXT")
    private String tokenSecret;

    @Column(name = "granted_scopes", columnDefinition = "TEXT")
    private String grantedScopes;

    @Column(name = "expiry_time")
    private Long expiryTime;

    @Column(name = "meta_json", columnDefinition = "JSON")
    private String metaJson;

    @Column(name = "created_time")
    private Long createdTime;

    @Column(name = "modified_time")
    private Long modifiedTime;

    @PrePersist
    protected void onCreate() {
        this.createdTime = System.currentTimeMillis();
        this.modifiedTime = System.currentTimeMillis();
    }

    @PreUpdate
    protected void onUpdate() {
        this.modifiedTime = System.currentTimeMillis();
    }

    // Manual Getters & Setters to fix compilation issues in Eclipse/WSL environment
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public ExternalAccount getExternalAccount() {
        return externalAccount;
    }

    public void setExternalAccount(ExternalAccount externalAccount) {
        this.externalAccount = externalAccount;
    }

    public AuthType getAuthType() {
        return authType;
    }

    public void setAuthType(AuthType authType) {
        this.authType = authType;
    }

    public String getTokenMain() {
        return tokenMain;
    }

    public void setTokenMain(String tokenMain) {
        this.tokenMain = tokenMain;
    }

    public String getTokenSecret() {
        return tokenSecret;
    }

    public void setTokenSecret(String tokenSecret) {
        this.tokenSecret = tokenSecret;
    }

    public String getGrantedScopes() {
        return grantedScopes;
    }

    public void setGrantedScopes(String grantedScopes) {
        this.grantedScopes = grantedScopes;
    }

    public Long getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(Long expiryTime) {
        this.expiryTime = expiryTime;
    }

    public String getMetaJson() {
        return metaJson;
    }

    public void setMetaJson(String metaJson) {
        this.metaJson = metaJson;
    }

    public Long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }

    public Long getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Long modifiedTime) {
        this.modifiedTime = modifiedTime;
    }
}
