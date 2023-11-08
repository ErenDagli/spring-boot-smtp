package com.globalpbx.mailserver.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.globalpbx.mailserver.dto.MailInfoDto;
import com.globalpbx.mailserver.service.MailServerService;
import jakarta.mail.MessagingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mail/")
public class MailServerController {

    private MailServerService mailServerService;

    public MailServerController(MailServerService mailServerService) {
        this.mailServerService = mailServerService;
    }

    @GetMapping
    public ResponseEntity<String> getMail() {
        return ResponseEntity.ok("Get Mail");
    }

    @PostMapping
    public ResponseEntity<String> sendMail(@RequestBody List<MailInfoDto> mailInfoDtoList) throws JsonProcessingException {
        return ResponseEntity.ok(mailServerService.sendMail(mailInfoDtoList));
    }

}
