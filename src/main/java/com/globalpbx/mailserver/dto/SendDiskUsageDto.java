package com.globalpbx.mailserver.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendDiskUsageDto {
    private String diskName;
    private long totalSpace;
    private long usedSpace;
    private long freeSpace;

    @Override
    public String toString() {
        return "SendDiskUsageDto{" +
                "diskName='" + diskName + '\'' +
                ", totalSpace=" + totalSpace +
                ", usedSpace=" + usedSpace +
                ", freeSpace=" + freeSpace +
                '}';
    }
}
