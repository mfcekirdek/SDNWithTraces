package com.mfc.GuiApplication.entity;

import com.mfc.GuiApplication.enums.TimeSegment;

public class PredictionResult {

	private CellTower trueCellTower;
	private CellTower predictedCellTower;
	private long time;
	private long hhmmss;
	private TimeSegment time_segment;
	private boolean isTrue;

	public PredictionResult() {
	}

	public PredictionResult(CellTower trueCellTower,
			CellTower predictedCellTower, long time, long hhmmss,
			TimeSegment time_segment, boolean isTrue) {
		super();
		this.trueCellTower = trueCellTower;
		this.predictedCellTower = predictedCellTower;
		this.time = time;
		this.hhmmss = hhmmss;
		this.time_segment = time_segment;
		this.isTrue = isTrue;
	}

	public CellTower getTrueCellTower() {
		return trueCellTower;
	}

	public void setTrueCellTower(CellTower trueCellTower) {
		this.trueCellTower = trueCellTower;
	}

	public CellTower getPredictedCellTower() {
		return predictedCellTower;
	}

	public void setPredictedCellTower(CellTower predictedCellTower) {
		this.predictedCellTower = predictedCellTower;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public long getHhmmss() {
		return hhmmss;
	}

	public void setHhmmss(long hhmmss) {
		this.hhmmss = hhmmss;
	}

	public TimeSegment getTime_segment() {
		return time_segment;
	}

	public void setTime_segment(TimeSegment time_segment) {
		this.time_segment = time_segment;
	}

	public String getCsvRepresentation() {
		return String.format("%d,%d,%d,%d,%s,%s", time, hhmmss, trueCellTower
				.getCid(), predictedCellTower.getCid(), (isTrue) ? "True"
				: "False", time_segment.getValue());
	}

	public boolean isTrue() {
		return isTrue;
	}

	public void setTrue(boolean isTrue) {
		this.isTrue = isTrue;
	}

	public static void main(String[] args) {
		PredictionResult p = new PredictionResult();
		p.setHhmmss(112233);
		p.setPredictedCellTower(new CellTower(1, 2, 3));
		p.setTrueCellTower(new CellTower(1, 2, 3));
		p.setTime(20101020);
		p.setTime_segment(TimeSegment.AFTERNOON);
		p.isTrue = true;
		System.out.println(p.getCsvRepresentation());
	}

}
