package com.mfc.GuiApplication.entity;

import com.mfc.GuiApplication.enums.TimeSegment;

public class Trace {
	private CellTower cellTower;
	private TimeSegment time_segment;
	private long time; // e.g: 20100916
	private long hhmmss;
	private int mcc;
	private int mnc;
	private int lac;
	private int timezone;
	private int day_number;
	private Boolean is_non_working_day;

	public Trace() {

	}

	public Trace(CellTower cellTower, TimeSegment time_segment, long time,
			long hhmmss, int mcc, int mnc, int lac, int timezone,
			int day_number, Boolean is_non_working_day) {
		this.cellTower = cellTower;
		this.time_segment = time_segment;
		this.time = time;
		this.hhmmss = hhmmss;
		this.mcc = mcc;
		this.mnc = mnc;
		this.lac = lac;
		this.timezone = timezone;
		this.day_number = day_number;
		this.is_non_working_day = is_non_working_day;
	}

	public CellTower getCellTower() {
		return cellTower;
	}

	public void setCellTower(CellTower cellTower) {
		this.cellTower = cellTower;
	}

	public TimeSegment getTime_segment() {
		return time_segment;
	}

	public void setTime_segment(TimeSegment time_segment) {
		this.time_segment = time_segment;
	}

	public Long getTime() {
		return time;
	}

	public static long getYear(long time) {
		return time / 10000;
	}

	public static long getMonth(long time) {
		return (time - getYear(time) * 10000) / 100;
	}

	public static long getDay(long time) {
		return time % 100;
	}

	public long getYear() {
		return Trace.getYear(time);
	}

	public long getMonth() {
		return Trace.getMonth(time);
	}

	public long getDay() {
		return Trace.getDay(time);
	}

	public long getHhmmss() {
		return hhmmss;
	}

	public void setHhmmss(long hhmmss) {
		this.hhmmss = hhmmss;
	}

	public String getCsvRepresentation() {
		return String.format("%d,%d,%d,%d,%d,%d,%f,%f,%d,%d,%s,%s", time,
				hhmmss, mcc, mnc, lac, cellTower.getCid(), cellTower.getLat(),
				cellTower.getLon(), timezone, day_number,
				(is_non_working_day) ? "True" : "False", time_segment
						.getValue());
	}

}
