package com.mfc.GuiApplication.enums;

import java.util.HashMap;
import java.util.Map;

public enum TimeSegment {
    AFTERNOON("A"), EVENING("E"), MORNING("M"), NIGHT("N"), UNKNOWN("U");

    private String value;

    private TimeSegment(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    private static final Map<String, TimeSegment> stringToTypeMap = new HashMap<String, TimeSegment>();

    static {
        for (TimeSegment type : TimeSegment.values()) {
            stringToTypeMap.put(type.value, type);
        }
    }

    public static TimeSegment fromString(String s) {

        TimeSegment type = stringToTypeMap.get(s);

        if (type == null) return TimeSegment.UNKNOWN;
        return type;
    }
}