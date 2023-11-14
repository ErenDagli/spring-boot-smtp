package com.globalpbx.mailserver.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.globalpbx.mailserver.dto.DiskUsageDto;
import com.globalpbx.mailserver.dto.MailInfoDto;
import com.globalpbx.mailserver.service.DiskService;
import com.globalpbx.mailserver.service.MailServerService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.globalpbx.mailserver.util.FormatterUtil.generateHTMLTable;

@Service
public class DiskServiceImpl implements DiskService {


    private MailServerService mailServerService;

    public DiskServiceImpl(MailServerService mailServerService) {
        this.mailServerService = mailServerService;
    }

    @Override
    public String diskUsage(DiskUsageDto diskUsageDto) throws JsonProcessingException {

        String[] headers = {" Disk Name ", " Total Space ", " Used Space ", " Free Space ", " Free Space(%) "};
        String[][] data = {
                {diskUsageDto.getDiskName(),
                        String.valueOf(diskUsageDto.getTotalSpace()),
                        String.valueOf(diskUsageDto.getUsedSpace()),
                        String.valueOf(diskUsageDto.getFreeSpace()),
                        String.valueOf(100*(diskUsageDto.getTotalSpace() - diskUsageDto.getFreeSpace()) / diskUsageDto.getTotalSpace())
                }
        };

        String tableContent = generateHTMLTable(headers, data);

        MailInfoDto diskUsageMailInfoDto = MailInfoDto.builder()
                .path("jdbc:sqlite:disk.db")
                .versionNumber(1)
                .recipient("ahmet.dagli@karel.com.tr")
                .subject("Disk Usage")
                .body(tableContent)
                .isHtml(true)
                .build();

        List<MailInfoDto> diskUsageMailInfoDtoList = new ArrayList<>();
        diskUsageMailInfoDtoList.add(diskUsageMailInfoDto);
        return mailServerService.sendMail(diskUsageMailInfoDtoList);
    }
}
