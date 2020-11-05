package com.mfc.GuiApplication.mvc.controller;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;

import com.mfc.GuiApplication.entity.CellTower;
import com.mfc.GuiApplication.entity.PredictionResult;
import com.mfc.GuiApplication.entity.Trace;
import com.mfc.GuiApplication.enums.Country;
import com.mfc.GuiApplication.enums.TimeSegment;
import com.mfc.GuiApplication.learning.HMM_Controller;
import com.mfc.GuiApplication.mvc.model.Model;

public class TraceModePausableRunnable extends Observable implements Runnable {

	private volatile boolean pauseWork = false;
	private volatile WorkerState state = WorkerState.NEW;
	private Thread workerThread;
	private boolean workDone = false;
	private TraceModeWorker worker;
	private int counter = 0;
	private List<Trace> traces;

	HashMap<Integer, CellTower> hm;
	private int fromIndex;
	private int toIndex;
	private int delayTime = 500;

	private int defaultFromIndex;
	private int defaultToIndex;
	private List<Trace> defaultTraces;
	private String csvFilePath;

	private void resetPlayer() {
		this.fromIndex = this.defaultFromIndex;
		this.toIndex = this.defaultToIndex;
		this.traces = this.defaultTraces;
		HMM_Controller.getInstance().clearPredictionResults();
		hm = new HashMap<Integer, CellTower>();
	}

	public TraceModePausableRunnable(String csvFilePath, int from, int to,
			List<Trace> traces, TraceModeWorker worker) {
		this.csvFilePath = csvFilePath;
		this.worker = worker;
		this.defaultFromIndex = from;
		this.defaultToIndex = to;
		this.defaultTraces = traces;
		hm = new HashMap<Integer, CellTower>();
	}

	public void run() {
		List<Trace> list = traces;

		List<Trace> biggerList = null;
		// ## temp
		// TODO !!! teach cid <-> lat,lon mappings. (fill hashmap)
		try {
			biggerList = Model.getTraces(this.csvFilePath, Country.ALL);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		// ##

		hm.clear();
		for (Trace aList : biggerList) {
			hm.put(aList.getCellTower().getCid(), aList.getCellTower());
		}

		counter = fromIndex;
		while (!workDone) {
			while (pauseWork) {
				setState(WorkerState.PAUSED);
				try {
					Thread.sleep(5000); // stop for 100ms increments
				} catch (InterruptedException ie) {
					// report or ignore
				}
			}

			setState(WorkerState.RUNNING);
			if (counter < Math.min(toIndex, list.size())) {
				String[] results = HMM_Controller.getInstance()
						.predictAndLearn(
								"" + list.get(counter).getCellTower().getCid(),
								list.get(counter).getTime_segment().getValue(),
								list.get(counter).getTime(),
								list.get(counter).getHhmmss());
				// returns isPredictionTrue, true_cell, predicted_cell,
				// whenTimeSegment, whenTime, whenHHMMSS

				boolean isPredictionTrue = Boolean.parseBoolean(results[0]);
				int true_cid = Integer.parseInt(results[1]);
				int predicted_cid = Integer.parseInt(results[2]);
				TimeSegment timeSegment = TimeSegment
						.fromString(results[3]);
				long time = Long.parseLong(results[4]);
				long hhmmss = Long.parseLong(results[5]);
				PredictionResult prediction = new PredictionResult(
						hm.get(true_cid), hm.get(predicted_cid), time, hhmmss,
						timeSegment, isPredictionTrue);
				HMM_Controller.getInstance().getPredictionResults()
						.add(prediction);
				System.out.println(prediction.getCsvRepresentation());

				this.worker.work(hm.get(true_cid), hm.get(predicted_cid));
				counter++;
				try {
					Thread.sleep(delayTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				this.stop();
				break;
			}
		}

		setState(WorkerState.FINISHED);
	}

	public void pause() {
		this.pauseWork = true;
		setState(WorkerState.PAUSED);
	}

	public void resume() {
		this.pauseWork = false;
		setState(WorkerState.RUNNING);
		if (workerThread != null)
			workerThread.interrupt(); // wakeup if sleeping
	}

	public void stop() {
		this.workDone = true;
		setState(WorkerState.NEW);
		HMM_Controller.getInstance().clearPredictionResults();
	}

	/**
	 * startImmediately = true to begin work right away, false = start Work in
	 * paused state, call resume() to do work
	 */
	public void start(boolean startImmediately) {
		this.resetPlayer();
		this.pauseWork = !startImmediately;
		workerThread = new Thread(this);
		workerThread.start();
	}

	public void setDelayTime(int delayTime) {
		this.delayTime = delayTime;
	}

	public WorkerState getState() {
		return state;
	}

	public void setState(WorkerState state) {
		if (this.state != state) {
			this.state = state;
			setChanged();
			notifyObservers();
		}
	}

}