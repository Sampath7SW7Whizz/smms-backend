package com.smms.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for sending emails.
 */
@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${smms.mail.offline-mode:true}")
    private boolean offlineMode;

    /**
     * Send OTP email for password reset.
     */
    public void sendOtpEmail(String toEmail, String otp, String userName) throws MessagingException {
        if (offlineMode) {
            logger.info("Offline mail mode enabled. OTP for {} ({}) is {}", userName, toEmail, otp);
            return;
        }

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("🔐 TSAR IT SMMS - Password Reset OTP");

        String htmlContent = buildOtpEmailTemplate(otp, userName);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    /**
     * Build beautiful HTML email template for OTP.
     */
    private String buildOtpEmailTemplate(String otp, String userName) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px; }
                    .container { max-width: 500px; margin: 0 auto; background: white; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.1); }
                    .header { background: linear-gradient(135deg, #16a34a, #22c55e); padding: 30px; text-align: center; }
                    .header h1 { color: white; margin: 0; font-size: 24px; }
                    .header .icon { font-size: 48px; margin-bottom: 10px; }
                    .content { padding: 30px; text-align: center; }
                    .greeting { font-size: 18px; color: #333; margin-bottom: 20px; }
                    .otp-box { background: linear-gradient(135deg, #f0fdf4, #dcfce7); border: 2px dashed #16a34a; border-radius: 12px; padding: 20px; margin: 20px 0; }
                    .otp-code { font-size: 36px; font-weight: bold; color: #16a34a; letter-spacing: 8px; font-family: 'Courier New', monospace; }
                    .info { color: #666; font-size: 14px; margin: 20px 0; }
                    .warning { background: #fef3c7; border-left: 4px solid #f59e0b; padding: 12px; margin: 20px 0; text-align: left; font-size: 13px; color: #92400e; border-radius: 4px; }
                    .footer { background: #f9fafb; padding: 20px; text-align: center; font-size: 12px; color: #888; }
                    .logo { font-weight: bold; color: #16a34a; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="icon">🏪</div>
                        <h1>TSAR IT SMMS</h1>
                    </div>
                    <div class="content">
                        <p class="greeting">Hello, <strong>%s</strong>!</p>
                        <p>You requested to reset your password. Use the OTP below to verify your identity:</p>
                        
                        <div class="otp-box">
                            <div class="otp-code">%s</div>
                        </div>
                        
                        <p class="info">This OTP is valid for <strong>5 minutes</strong>.</p>
                        
                        <div class="warning">
                            ⚠️ <strong>Security Notice:</strong> Never share this OTP with anyone. TSAR IT SMMS staff will never ask for your OTP.
                        </div>
                    </div>
                    <div class="footer">
                        <p>If you didn't request this, please ignore this email.</p>
                        <p class="logo">© 2026 TSAR IT SMMS - SuperMarket Management System</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(userName != null ? userName : "User", otp);
    }
}
