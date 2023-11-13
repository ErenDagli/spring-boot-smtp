package com.globalpbx.mailserver.controller;

import com.globalpbx.mailserver.dto.LogDto;
import com.globalpbx.mailserver.service.LogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/logs/")
public class LogController {

    private LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @PostMapping
    public ResponseEntity<String> createLog(@RequestBody List<LogDto> logDtoListDto) {
        String savedLog = logService.createLog(logDtoListDto);
        return new ResponseEntity<>(savedLog, HttpStatus.CREATED);
    }
}
