package com.mfc.GuiApplication.main;

import java.awt.Button;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.mfc.GuiApplication.mvc.controller.NormalModeRunnable;

import de.dailab.nemo.imovefan.learningvalidation.config.Config;
import de.dailab.nemo.imovefan.learningvalidation.mobility.Trajectory;

public class tester {
	public static void main(String[] args) {
		
		String filePath = "./.";
        int randomness = 10;
		Trajectory.TrajectoryType type = Trajectory.TrajectoryType.CONSTRAINED_WAYPOINT;
        int settingsId = -1;
        int runId = -1;
                 
        try {
            File dir = new File(filePath);
            if (!dir.exists())
                throw new IllegalArgumentException();
            else {
                File dataPath = new File(dir.getAbsolutePath() + "/data");
                if (!dataPath.exists())
                    dataPath.mkdir();

                Config.DATA_PATH = dataPath.getAbsolutePath();


                File trajectoryPath = new File(dir.getAbsolutePath() + "/trajectories");
                if (!trajectoryPath.exists())
                    trajectoryPath.mkdir();

                Config.TRAJECTORY_PATH = trajectoryPath.getAbsolutePath();
            }

        }catch (Exception e) {
            System.err.println("Illegal path "+ e);          
        }
        JFrame frame = new JFrame();
        JPanel panel = new JPanel();
        panel.add(new Button("jrjja"));
        frame.add(panel);
        frame.setVisible(true);
        frame.setSize(600,600);
		NormalModeRunnable m = new NormalModeRunnable(type, randomness, settingsId, runId, panel);
		m.start(true);
	}
}
