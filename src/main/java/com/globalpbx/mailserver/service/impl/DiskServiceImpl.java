package com.globalpbx.mailserver.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.globalpbx.mailserver.dto.SendDiskUsageDto;
import com.globalpbx.mailserver.dto.MailInfoDto;
import com.globalpbx.mailserver.service.DiskService;
import com.globalpbx.mailserver.service.MailServerService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.globalpbx.mailserver.util.FormatterUtil.formatFileSize;
import static com.globalpbx.mailserver.util.FormatterUtil.generateHTMLTable;

@Service
public class DiskServiceImpl implements DiskService {


    private MailServerService mailServerService;

    public DiskServiceImpl(MailServerService mailServerService) {
        this.mailServerService = mailServerService;
    }

    @Override
    public String diskUsage(SendDiskUsageDto sendDiskUsageDto) throws JsonProcessingException {

        String[] headers = {" Disk Name ", " Total Space ", " Used Space ", " Free Space ", " Free Space(%) "};
        String[][] data = {
                {sendDiskUsageDto.getDiskName(),
                        String.valueOf(sendDiskUsageDto.getTotalSpace()),
                        String.valueOf(sendDiskUsageDto.getUsedSpace()),
                        String.valueOf(sendDiskUsageDto.getFreeSpace()),
                        String.valueOf(100 * (sendDiskUsageDto.getTotalSpace() - sendDiskUsageDto.getFreeSpace()) / sendDiskUsageDto.getTotalSpace())
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

    @Override
    @Cacheable("diskUsage")
    public List<String[]> getDiskUsageByPath(String path,int maxDepth) {
        List<String[]> fileSizeList = new ArrayList<>();
        File file = new File(path);

        if (file.exists()) {
            try {
                Files.walk(Path.of(path), maxDepth)
                        .parallel()
                        .forEach(subDir -> {
                            long subDirSize = 0;
                            try {
                                subDirSize = Files.walk(subDir)
                                        .filter(Files::isRegularFile)
                                        .parallel()
                                        .mapToLong(p -> p.toFile().length())
                                        .sum();
                            } catch (IOException e) {
                                e.printStackTrace();
                                throw new RuntimeException(e);
                            }
                            fileSizeList.add(new String[]{String.valueOf(subDir.getFileName()), formatFileSize(subDirSize)});
                        });
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            return fileSizeList;
        }
        fileSizeList.add(new String[]{"file could not be found"});
        return fileSizeList;
    }


}
