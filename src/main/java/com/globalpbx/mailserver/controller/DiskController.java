package com.globalpbx.mailserver.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.globalpbx.mailserver.dto.SendDiskUsageDto;
import com.globalpbx.mailserver.dto.DiskUsageDto;
import com.globalpbx.mailserver.enums.DiskUsageEnum;
import com.globalpbx.mailserver.service.DiskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/disks")
public class DiskController {

    private DiskService diskService;

    public DiskController(DiskService diskService) {
        this.diskService = diskService;
    }

    @PostMapping
    public ResponseEntity<String> diskUsage(@RequestBody SendDiskUsageDto sendDiskUsageDto) throws JsonProcessingException {
        diskService.diskUsage(sendDiskUsageDto);
        return new ResponseEntity<>("success", HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<String[]>> getDiskUsage(@RequestBody DiskUsageDto diskUsageDto) throws IOException {
        return new ResponseEntity<>(diskService.getDiskUsageByPath(diskUsageDto.getPath(),DiskUsageEnum.ALL.getValue()),HttpStatus.OK);
    }
    @GetMapping("/details")
    public ResponseEntity<List<String[]>> getDiskUsageDetails(@RequestBody DiskUsageDto diskUsageDto) throws IOException {
        return new ResponseEntity<>(diskService.getDiskUsageByPath(diskUsageDto.getPath(), DiskUsageEnum.DETAIL.getValue()),HttpStatus.OK);
    }
}
