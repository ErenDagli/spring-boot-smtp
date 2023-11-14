package com.globalpbx.mailserver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.globalpbx.mailserver.dto.DiskUsageDto;

import java.io.IOException;
import java.util.List;

public interface DiskService {
    String diskUsage(DiskUsageDto diskUsageDto) throws JsonProcessingException;

    List<String[]> getDiskUsageByPath(String path,int maxDepth) throws IOException;
}
