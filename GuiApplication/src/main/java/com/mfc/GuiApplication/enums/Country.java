package com.mfc.GuiApplication.enums;

public enum Country {
	// [0 230 231 232 262 310]

	UNKNOWN(0), ALL(-1), CZECH_REPUBLIC(230), SLOVAKIA(231), AUSTRIA(232), GERMANY(
			262), UNITED_STATES(310);
	private final int value;

	private Country(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static Country fromValue(int x) {
		switch (x) {
		case 0:
			return UNKNOWN;
		case -1:
			return ALL;
		case 230:
			return CZECH_REPUBLIC;
		case 231:
			return SLOVAKIA;
		case 232:
			return AUSTRIA;
		case 262:
			return GERMANY;
		case 310:
			return UNITED_STATES;

		}
		return UNKNOWN;
	}
}
