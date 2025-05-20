package com.BusBooking.Bus.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendTicket(String toEmail, byte[] pdfBytes, String fileName) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(toEmail);
        helper.setSubject("🚌 Your Bus Ticket");
        helper.setText("Hi,\n\nPlease find your ticket attached.\n\nHappy Journey!\n\n- BusBooking Team");

        ByteArrayResource resource = new ByteArrayResource(pdfBytes);
        helper.addAttachment(fileName, resource);

        mailSender.send(message);
    }
}
