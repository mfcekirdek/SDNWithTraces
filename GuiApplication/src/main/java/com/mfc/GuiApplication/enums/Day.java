package com.mfc.GuiApplication.enums;

import java.util.HashMap;
import java.util.Map;

public enum Day {
    MONDAY("MO"), TUESDAY("TU"), WEDNESDAY("WE"), THURSDAY("YH"), FRIDAY("FR"),SATURDAY("SA"),SUNDAY("SU"),UNKNOWN("U");

    private String value;

    private Day(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    private static final Map<String, Day> stringToTypeMap = new HashMap<String, Day>();

    static {
        for (Day type : Day.values()) {
            stringToTypeMap.put(type.value, type);
        }
    }

    public static Day fromString(String s) {

        Day type = stringToTypeMap.get(s);

        if (type == null) return Day.UNKNOWN;
        return type;
    }
}