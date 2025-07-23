package edu.zia.international.school.controller;

import edu.zia.international.school.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/email")
public class EmailTestController {

    @Autowired
    private EmailService emailService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String sendTestMail(@RequestParam String to) {
        emailService.sendEmail(
                to,
                "Test Email from ZIA School System",
                "Hi, this is a test email sent from Spring Boot using Gmail SMTP."
        );
        return "Mail sent successfully to " + to;
    }
}

