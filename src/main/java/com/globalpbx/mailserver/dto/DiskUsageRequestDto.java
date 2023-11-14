package com.globalpbx.mailserver.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiskUsageRequestDto {
    private String path;

    @Override
    public String toString() {
        return "DiskUsageRequestDto{" +
                "path='" + path + '\'' +
                '}';
    }
}
