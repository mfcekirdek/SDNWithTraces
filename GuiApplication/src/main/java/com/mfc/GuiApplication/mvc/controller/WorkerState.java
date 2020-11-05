package com.mfc.GuiApplication.mvc.controller;

import java.util.HashMap;
import java.util.Map;

public enum WorkerState {
	NEW("New"), PAUSED("Paused"), RUNNING("Running"), FINISHED("Finished"), UNKNOWN("Unknown");

	private String value;

	private WorkerState(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	private static final Map<String, WorkerState> stringToTypeMap = new HashMap<String, WorkerState>();

	static {
		for (WorkerState type : WorkerState.values()) {
			stringToTypeMap.put(type.value, type);
		}
	}

	public static WorkerState fromString(String s) {

		WorkerState type = stringToTypeMap.get(s);

		if (type == null)
			return WorkerState.UNKNOWN;
		return type;
	}
}
