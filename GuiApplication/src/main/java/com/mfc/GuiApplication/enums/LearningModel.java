package com.mfc.GuiApplication.enums;

import java.util.HashMap;
import java.util.Map;

public enum LearningModel {
    HIDDEN_MARKOV_MODEL("HMM"), MARKOV_CHAIN_MODEL("MCM"),UNKNOWN("U");

    private String value;

    private LearningModel(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    private static final Map<String, LearningModel> stringToTypeMap = new HashMap<String, LearningModel>();

    static {
        for (LearningModel type : LearningModel.values()) {
            stringToTypeMap.put(type.value, type);
        }
    }

    public static LearningModel fromString(String s) {

      LearningModel type = stringToTypeMap.get(s);

        if (type == null) return LearningModel.UNKNOWN;
        return type;
    }
}
