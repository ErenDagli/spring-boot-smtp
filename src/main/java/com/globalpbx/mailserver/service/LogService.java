package com.globalpbx.mailserver.service;

import com.globalpbx.mailserver.dto.LogDto;

import java.util.List;

public interface LogService {

    String createLog(List<LogDto> logDtoList);
}
