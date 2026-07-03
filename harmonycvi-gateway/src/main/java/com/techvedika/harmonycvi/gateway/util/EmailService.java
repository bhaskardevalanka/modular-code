package com.techvedika.harmonycvi.gateway.util;

import java.util.Date;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;


@Service
public class EmailService {
	private static final Logger log = LoggerFactory.getLogger(EmailService.class);
	private final boolean auth = true;
    private final boolean debug = true;

    @Value("${mail.host}")
    private String mailHost;

    @Value("${mail.port}")
    private String mailPort;

    @Value("${mail.username}")
    private String username;

    @Value("${mail.password}")
    private String password;

    @Value("${mail.from}")
    private String mailFrom;

    public void sendEmail(String toEmail, String subject, String body) {
        Properties props = new Properties();
        props.put("mail.smtp.host", mailHost);
        props.put("mail.smtp.port", mailPort);

        if ("465".equals(mailPort)) {
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.ssl.trust", mailHost);
            props.put("mail.smtp.ssl.checkserveridentity", "true");
        } else if ("587".equals(mailPort)) {
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
            props.put("mail.smtp.ssl.checkserveridentity", "true");
        }

        props.put("mail.debug", String.valueOf(debug));

        Authenticator authenticator = null;
        if (auth) {
            props.put("mail.smtp.auth", true);
            authenticator = new Authenticator() {
                private final PasswordAuthentication pa = new PasswordAuthentication(username, password);

                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return pa;
                }
            };
        }

        Session session = Session.getInstance(props, authenticator);
        session.setDebug(debug);

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(mailFrom));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setSentDate(new Date());
            message.setContent(body, "text/html; charset=utf-8");

            Transport.send(message);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}", toEmail, e);
        }
    }
}
