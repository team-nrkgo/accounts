package com.nrkgo.accounts.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



@Entity
@Table(name = "user_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSession extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    private String browser;

    private String cookie;

    private String deviceName;

    private Integer location;

    @Column(nullable = false)
    private Integer status;

    private String deviceOs;

    @Column(nullable = false)
    private Long expireTime;

    private String machineIp;

    private String city;

    // Manual Getters, Setters, and Constructor
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getBrowser() { return browser; }
    public void setBrowser(String browser) { this.browser = browser; }

    public String getCookie() { return cookie; }
    public void setCookie(String cookie) { this.cookie = cookie; }

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public Integer getLocation() { return location; }
    public void setLocation(Integer location) { this.location = location; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getDeviceOs() { return deviceOs; }
    public void setDeviceOs(String deviceOs) { this.deviceOs = deviceOs; }

    public Long getExpireTime() { return expireTime; }
    public void setExpireTime(Long expireTime) { this.expireTime = expireTime; }

    public String getMachineIp() { return machineIp; }
    public void setMachineIp(String machineIp) { this.machineIp = machineIp; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
}
