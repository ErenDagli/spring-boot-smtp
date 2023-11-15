package com.globalpbx.mailserver.enums;

public enum DiskUsageEnum {
    DETAIL(1),
    ALL(0);

    private final int value;

    DiskUsageEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
