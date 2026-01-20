package com.nrkgo.accounts.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;



@Entity
@Table(name = "digests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Digest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String entityType; // e.g., "INVITE", "RESET_PASSWORD"

    @Column(nullable = false)
    private String entityId; // Storing as String to accommodate various ID types if needed, or stick to Long if strict.

    @Column(nullable = false, unique = true)
    private String token;

    @Column(columnDefinition = "TEXT")
    private String metadata; // For extra data like email if entityId isn't enough

    @Column(nullable = false)
    private Long expiryTime;

    // Manual Accessors
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public Long getExpiryTime() { return expiryTime; }
    public void setExpiryTime(Long expiryTime) { this.expiryTime = expiryTime; }
}
