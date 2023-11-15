package com.globalpbx.mailserver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.globalpbx.mailserver.dto.SendDiskUsageDto;

import java.io.IOException;
import java.util.List;

public interface DiskService {
    String diskUsage(SendDiskUsageDto sendDiskUsageDto) throws JsonProcessingException;

    List<String[]> getDiskUsageByPath(String path,int maxDepth) throws IOException;
}
