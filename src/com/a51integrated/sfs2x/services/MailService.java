package com.a51integrated.sfs2x.services;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class MailService
{
    private final Session session;
    private final String from;

    public MailService(String host, int port, boolean tls, String username, String password, String from)
    {
        this.from = from;

        var properties = new Properties();
        
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", tls ? "true" : "false");

        var authenticator = new Authenticator() {
          @Override protected PasswordAuthentication getPasswordAuthentication() {
              return new PasswordAuthentication(username, password);
          }
        };
        
        this.session = Session.getDefaultInstance(properties, authenticator);
        
    }

    public void send(String to, String subject, String html) throws MessagingException
    {
        var message = new MimeMessage(session);

        message.setFrom(new InternetAddress(from));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject, "UTF-8");
        message.setContent(html, "text/html; charset=UTF-8");
        Transport.send(message);
    }
}
