package com.mfc.GuiApplication.learning;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.mfc.GuiApplication.entity.PredictionResult;
import com.mfc.GuiApplication.entity.Trace;
import com.mfc.GuiApplication.enums.LearningModel;
import com.mfc.GuiApplication.mvc.view.View;

public class HMM_Controller {

	private static BufferedReader inp;
	private static BufferedReader err;
	private static BufferedWriter out;

	private static final String START_COMMAND = "python3.5 "
			+ System.getProperty("user.dir")
			+ "/PythonLearning/learning_script.py";

	private View activeView = null;
	private boolean isModelReady = false;
	private boolean isInitialized = false;
	private Process hmmProcess = null;
  
    private List<PredictionResult> predictionResults = new ArrayList<PredictionResult>();

	private static HMM_Controller instance = null;

	public static HMM_Controller getInstance(View v) {
		if (instance == null) {
			instance = new HMM_Controller(v);
		}
		return instance;
	}

	public static HMM_Controller getInstance() {
		if (instance == null) {
			instance = new HMM_Controller(null);
		}
		return instance;
	}

	private HMM_Controller(View v) {
		this.activeView = v;
	}

	private void log(String s) {
		if (s != null)
			System.out.println("### " + s);
	}

	private synchronized String pipe(String msg) {
		String ret;

		try {
			log(msg);
			out.write(msg + "\n");
			out.flush();
			ret = inp.readLine();
			log(ret);
			return ret;
		} catch (Exception err) {
			return "";
		}
	}

	public void startHMMProcess() {
		try {
			System.out.println(START_COMMAND);
			System.out.println(System.getProperty("user.dir"));
			hmmProcess = Runtime.getRuntime().exec(START_COMMAND);

			inp = new BufferedReader(new InputStreamReader(
					hmmProcess.getInputStream()));
			err = new BufferedReader(new InputStreamReader(
					hmmProcess.getErrorStream()));
			out = new BufferedWriter(new OutputStreamWriter(
					hmmProcess.getOutputStream()));

			while (!isInitialized) {
				Thread.sleep(500);
				String ret = inp.readLine();
				log(ret);
				if ("Initialized".equals(ret)) {
					isInitialized = true;
					if (activeView != null) {
						activeView.updateLearnerStatus("Initialized");
					}
					break;
				}
				System.err
						.println("Python script initialization is not finished... Waiting for logs...");
			}
		} catch (Exception err) {
			err.printStackTrace();
		}
	}

	private void logModelErrors() {
		String line;
		try {
			while ((line = err.readLine()) != null) {
				if (line.isEmpty()) {
					break;
				}
				System.err.println("! " + line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// returns true_counter,total_counter,accuracy
	public String[] getAccuracy() {
		if (isModelReady) {
			String response = this.pipe("getAccuracy");
			return response.split("\\_");
		}
		log("Model not ready!");
		return null;
	}

	// returns isPredictionTrue, true_cell, predicted_cell, whenTimeSegment, whenTime, whenHHMMSS
	public String[] predictAndLearn(String cid, String timeSegment,long time, long hhmmss) {
	
		if (isModelReady) {
			String response = this.pipe("predictAndLearn" + "_" + cid + "_"
					+ timeSegment);
			System.err.println("RESPONSE : " + response);
			if (response == null)
				logModelErrors();
			else
				response+= "_"+time+"_"+hhmmss;
			return response.split("\\_");
		}
		log("Model not ready!");
		return null;
	}

	public void clearHistoryStatistics() {
		if (isModelReady) {
			this.pipe("clearHistoryStatistics");
		}
	}

	public void closeProcessAndConnections() {
		pipe("quit");
		try {
			inp.close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		    activeView = null;
		    isModelReady = false;
		    isInitialized = false;
		    hmmProcess = null;
		    predictionResults = new ArrayList<PredictionResult>();
		    instance = null;
	}

	public String[] getArrays() {
		if (isModelReady) {
			return this.pipe("getAccuracy").split("\\_");
		}
		return new String[] { "", "", "" };
	}

	public void newData(String path,LearningModel model) {
		isModelReady = false;
		if (activeView != null)
			activeView.updateLearnerStatus("Learning");
		String ret = pipe("newData " + path + " " + model.getValue());
		if (ret == null)
			logModelErrors();
		try {
			while (!"Ready".equals(ret)) {
				Thread.sleep(5000);
				System.out.println(ret);
				System.err.println("Learning...");
				ret = inp.readLine();
				log(ret);
				if ("Ready".equals(ret)) {
					isModelReady = true;
					if (activeView != null)
						activeView.updateLearnerStatus("Ready");
					break;
				}
			}
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}
	}

	public void notifyPythonToReadTopology() {
		String ret = this.pipe("topologyFileIsReady").split("\\_")[0];
		if (ret.equals("read"))
			System.out.println("holeyTopology");
		else
			System.out.println(":'( error");
	}
	
	public void notifyPythonToPlot(String mode) {
		String ret = this.pipe("predictionFileIsReady_"+mode).split("\\_")[0];
		if (ret.equals("plotted"))
			System.out.println("holeyPredictions");
		else
			System.out.println(":'( error");
	}

	public List<PredictionResult> getPredictionResults() {
		return predictionResults;
	}

	public void setPredictionResults(List<PredictionResult> predictionResults) {
		this.predictionResults = predictionResults;
	}
	
	
	public void sendTracesToPython(String csvFileFullPath,int fromIndex, int toIndex,List<Trace> traceList){
        System.out.println("saveToFile() : " + csvFileFullPath);
        System.out.println("From " + fromIndex);
        System.out.println("To " + toIndex);
        System.out.println(traceList.get(0).getCsvRepresentation());
        Path path = Paths.get(csvFileFullPath);
        try {
            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                writer.write("YYYYMMDD,hhmmss (UTC+0),MCC,MNC,LAC,CID,Latitude,Longitude,Timezone,day_number,is_non_working_day,Timestate\n");
                for(int i = fromIndex; i < toIndex; i++) {
                    writer.write(traceList.get(i).getCsvRepresentation() + "\n");
                }
                writer.close();
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        
        notifyPythonToReadTopology();
	}
	
	
	
	

	public void sendPredictionResultsToPython(String csvFileFullPath,String mode) {
        System.out.println("saveToFile() : " + csvFileFullPath);
        Path path = Paths.get(csvFileFullPath);
        try {
            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                writer.write("YYYYMMDD,hhmmss (UTC+0),Real,Predicted,Result,Timestate\n");
                for(int i = 0; i < predictionResults.size(); i++) {
                    writer.write(predictionResults.get(i).getCsvRepresentation() + "\n");
                }
                writer.close();
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        notifyPythonToPlot(mode);
	}
	
	
	
	
	
	public void clearPredictionResults(){
		predictionResults.clear();
	}
	
	 public Process getHmmProcess() {
	    return hmmProcess;
	  }
	
}