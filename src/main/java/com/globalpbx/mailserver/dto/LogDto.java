package com.globalpbx.mailserver.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogDto {
    private Long id;
    private LocalDateTime logTime;
    private String projectName;
    private String log;
    private String type;

    @Override
    public String toString() {
        return "LogDto{" +
                "id=" + id +
                ", logTime=" + logTime +
                ", projectName='" + projectName + '\'' +
                ", log='" + log + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
