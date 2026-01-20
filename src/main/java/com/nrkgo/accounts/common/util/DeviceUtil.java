package com.nrkgo.accounts.common.util;

import jakarta.servlet.http.HttpServletRequest;

public class DeviceUtil {

    private static final String UNKNOWN = "Unknown";

    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    public static String getBrowser(String userAgent) {
        if (userAgent == null) return UNKNOWN;
        String browser = UNKNOWN;
        String agent = userAgent.toLowerCase();

        if (agent.contains("msie") || agent.contains("trident")) {
            browser = "Internet Explorer";
        } else if (agent.contains("edge")) {
            browser = "Microsoft Edge";
        } else if (agent.contains("chrome") && !agent.contains("edge") && !agent.contains("yandex")) {
            browser = "Google Chrome";
        } else if (agent.contains("safari") && !agent.contains("chrome")) {
            browser = "Safari";
        } else if (agent.contains("firefox")) {
            browser = "Mozilla Firefox";
        } else if (agent.contains("opera") || agent.contains("opr")) {
            browser = "Opera";
        }
        return browser;
    }

    public static String getOs(String userAgent) {
        if (userAgent == null) return UNKNOWN;
        String os = UNKNOWN;
        String agent = userAgent.toLowerCase();

        if (agent.contains("windows")) {
            os = "Windows";
        } else if (agent.contains("mac")) {
            os = "MacOS";
        } else if (agent.contains("x11")) {
            os = "Unix";
        } else if (agent.contains("android")) {
            os = "Android";
        } else if (agent.contains("iphone")) {
            os = "iOS";
        } else if (agent.contains("linux")) {
            os = "Linux";
        }
        return os;
    }

    public static String getDeviceName(String userAgent) {
        if (userAgent == null) return UNKNOWN;
        String device = "Desktop";
        String agent = userAgent.toLowerCase();

        if (agent.contains("android")) {
            device = "Android Device";
        } else if (agent.contains("iphone") || agent.contains("ipad") || agent.contains("ipod")) {
            device = "iOS Device";
        }
        return device;
    }
}
