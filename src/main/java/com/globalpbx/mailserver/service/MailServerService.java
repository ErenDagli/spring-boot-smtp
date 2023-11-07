package com.globalpbx.mailserver.service;

import com.globalpbx.mailserver.dto.MailInfoDto;
import jakarta.mail.MessagingException;

import java.util.List;

public interface MailServerService {

    String sendMail(List<MailInfoDto> mailInfoDtoList);

}
