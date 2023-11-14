package com.globalpbx.mailserver.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.globalpbx.mailserver.dto.DiskUsageDto;
import com.globalpbx.mailserver.service.DiskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/disks")
public class DiskController {

    private DiskService diskService;

    public DiskController(DiskService diskService) {
        this.diskService = diskService;
    }

    @PostMapping
    public ResponseEntity<String> diskUsage(@RequestBody DiskUsageDto diskUsageDto) throws JsonProcessingException {
        diskService.diskUsage(diskUsageDto);
        return new ResponseEntity<>("success", HttpStatus.OK);
    }
}
