package com.globalpbx.mailserver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.globalpbx.mailserver.dto.DiskUsageDto;

public interface DiskService {
    String diskUsage(DiskUsageDto diskUsageDto) throws JsonProcessingException;
}
