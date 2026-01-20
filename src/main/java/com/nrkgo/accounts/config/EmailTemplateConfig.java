package com.nrkgo.accounts.config;

public class EmailTemplateConfig {

    public static String getVerificationEmailTemplate(String verificationLink) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f4f5; margin: 0; padding: 0; }" +
                ".container { max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); overflow: hidden; }" +
                ".header { background-color: #18181b; padding: 20px; text-align: center; }" +
                ".header h2 { color: #ffffff; margin: 0; font-size: 24px; font-weight: 600; }" +
                ".content { padding: 40px; color: #3f3f46; line-height: 1.6; }" +
                ".content h3 { color: #18181b; margin-top: 0; }" +
                ".button { display: inline-block; background-color: #2563eb; color: #ffffff; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: 500; margin-top: 20px; }" +
                ".button:hover { background-color: #1d4ed8; }" +
                ".footer { background-color: #f4f4f5; padding: 20px; text-align: center; color: #71717a; font-size: 14px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h2>NRKGo Accounts</h2>" +
                "</div>" +
                "<div class='content'>" +
                "<h3>Verify your email address</h3>" +
                "<p>Thanks for creating an account with NRKGo. We're excited to have you on board!</p>" +
                "<p>Please verify your email address to activate your account and get started.</p>" +
                "<center><a href='" + verificationLink + "' class='button' style='color: #ffffff;'>Verify Email</a></center>" +
                "<p>If you didn't create an account, you can safely ignore this email.</p>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>&copy; 2026 NRKGo Inc. All rights reserved.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
    public static String getInvitationEmailTemplate(String inviteLink, String orgName, String inviterName) {
        return "<!DOCTYPE html>" +
               "<html>" +
               "<head>" +
               "<style>" +
               "body { width: 100% !important; -webkit-text-size-adjust: 100%; -ms-text-size-adjust: 100%; margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; }" +
               ".ExternalClass { width: 100%; }" +
               "img { outline: none; text-decoration: none; -ms-interpolation-mode: bicubic; }" +
               "a img { border: none; }" +
               "</style>" +
               "</head>" +
               "<body style='margin: 0; padding: 0; background-color: #f4f4f4;'>" +
               "<table border='0' cellpadding='0' cellspacing='0' width='100%'>" +
               "<tr>" +
               "<td style='padding: 20px 0; background-color: #f4f4f4;' align='center'>" +
               
               "<!-- Container -->" +
               "<table border='0' cellpadding='0' cellspacing='0' width='600' style='background-color: #ffffff; border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.05); overflow: hidden;'>" +
               
               "<!-- Header -->" +
               "<tr>" +
               "<td align='center' style='padding: 30px 20px; border-bottom: 1px solid #eeeeee;'>" +
               "<h2 style='margin: 0; color: #333333; font-size: 24px; font-weight: 600;'>NRKGo Accounts</h2>" +
               "</td>" +
               "</tr>" +
               
               "<!-- Content -->" +
               "<tr>" +
               "<td style='padding: 40px 30px; color: #333333; line-height: 1.6; font-size: 16px;'>" +
               "<p style='margin: 0 0 20px 0;'>Hi,</p>" +
               "<p style='margin: 0 0 20px 0;'>Welcome to NRKGo Accounts. <strong>" + inviterName + "</strong> has invited you to join the organization <strong>" + orgName + "</strong>.</p>" +
               "<p style='margin: 0 0 30px 0;'>To accept the invitation and get started, please click the button below:</p>" +
               
               "<!-- Button -->" +
               "<table border='0' cellpadding='0' cellspacing='0' width='100%'>" +
               "<tr>" +
               "<td align='center'>" +
               "<a href='" + inviteLink + "' style='background-color: #6f42c1; color: #ffffff; padding: 14px 28px; text-decoration: none; border-radius: 6px; font-weight: bold; display: inline-block; font-size: 16px;'>Accept Invitation</a>" +
               "</td>" +
               "</tr>" +
               "</table>" +
               
               "<p style='margin: 30px 0 0 0; font-size: 14px; color: #666666;'>If the button above doesn't work, copy and paste this link into your browser:</p>" +
               "<p style='margin: 10px 0 0 0; font-size: 14px; word-break: break-all;'><a href='" + inviteLink + "' style='color: #6f42c1;'>" + inviteLink + "</a></p>" +
               "</td>" +
               "</tr>" +
               
               "<!-- Footer -->" +
               "<tr>" +
               "<td style='background-color: #f9f9f9; padding: 20px; text-align: center; color: #888888; font-size: 12px;'>" +
               "<p style='margin: 0;'>&copy; 2026 NRKGo. All rights reserved.</p>" +
               "</td>" +
               "</tr>" +
               
               "</table>" +
               "<!-- End Container -->" +
               
               "</td>" +
               "</tr>" +
               "</table>" +
               "</body>" +
               "</html>";
    }
}
