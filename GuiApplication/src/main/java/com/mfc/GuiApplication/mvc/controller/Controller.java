package com.mfc.GuiApplication.mvc.controller;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.stream.Collectors;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.mfc.GuiApplication.entity.CellTower;
import com.mfc.GuiApplication.entity.Trace;
import com.mfc.GuiApplication.enums.Country;
import com.mfc.GuiApplication.enums.LearningModel;
import com.mfc.GuiApplication.learning.HMM_Controller;
import com.mfc.GuiApplication.mvc.model.Model;
import com.mfc.GuiApplication.mvc.view.MySplittedPanel;
import com.mfc.GuiApplication.mvc.view.View;
import com.mfc.GuiApplication.topology.Topology;

import de.dailab.nemo.imovefan.learningvalidation.config.Config;
import de.dailab.nemo.imovefan.learningvalidation.mobility.Trajectory;

public class Controller implements ChangeListener, Observer {

  private Model model_true;
  private Model model_predicted;
  private Model model_merged;
  private Model model_total;
  private View view;
  private TraceModePausableRunnable trajectory_player;
  private boolean isFocusAndIntereactionsSet = false;
  private List<Trace> traceList;
  private List<Long> times;
  private String activeCsvFileFullPath = "None";
  private String trajectoryViewState = "Merged";

  private NormalModeRunnable normalModeRunnable = null;

  public Controller(Model model_true, Model model_predicted, Model model_merged, Model model_total,
      View view) {
    this.model_true = model_true;
    this.model_predicted = model_predicted;
    this.model_total = model_total;
    this.model_merged = model_merged;
    this.view = view;
    view.getFromSlider().addChangeListener(this);
    view.getToSlider().addChangeListener(this);
    view.getSpeedSlider().addChangeListener(this);
    view.getNormalModeSlider().addChangeListener(this);
  }

  private int getFromIndex() {
    System.out.println(view.getFromSlider().getValue() + " slider : ");
    long fromValue = times.get(view.getFromSlider().getValue());
    for (int i = 0; i < traceList.size(); i++) {
      if (traceList.get(i).getTime() >= fromValue)
        return i;
    }
    return 0;
  }

  private int getToIndex() {
    int toIndex = -1;
    long toValue = times.get(view.getToSlider().getValue());
    for (int i = 0; i < traceList.size(); i++) {
      if (toIndex == -1 && traceList.get(i).getTime() > toValue)
        return toIndex = i;
    }
    return traceList.size();
  }

  private void setTrajectoryPlayer() {
    
    if(trajectory_player != null){
      trajectory_player.stop();
      trajectory_player = null;
    }
    
    int fromIndex = getFromIndex();
    int toIndex = getToIndex();
    trajectory_player =
        new TraceModePausableRunnable(activeCsvFileFullPath, fromIndex, toIndex, traceList,
            new TraceModeWorker() {
              public void work(CellTower c_true, CellTower c_predicted) {

                model_merged.updateMapViewWithASingleTraceWithRoutingMerged(c_true, c_predicted,
                    Color.BLACK);

                model_true.updateMapViewWithASingleTraceWithRouting(c_true, c_predicted,
                    Color.BLACK);

                if (c_true.getCid() == c_predicted.getCid()) {
                  model_predicted.updateMapViewWithASingleTraceWithRouting(c_true, c_predicted,
                      Color.GREEN);
                  int true_counter =
                      Integer.parseInt(view.getLabel_truePredictionCounterValue().getText()) + 1;
                  view.getLabel_truePredictionCounterValue().setText("" + true_counter);
                } else {
                  int false_counter =
                      Integer.parseInt(view.getLabel_falsePredictionCounterValue().getText()) + 1;
                  view.getLabel_falsePredictionCounterValue().setText("" + false_counter);
                  model_predicted.updateMapViewWithASingleTraceWithRouting(c_true, c_predicted,
                      Color.RED);
                }

                int total_counter =
                    Integer.parseInt(view.getLabel_totalMovementCounterValue().getText()) + 1;
                view.getLabel_totalMovementCounterValue().setText("" + total_counter);

                if (total_counter != 0) {
                  int true_counter =
                      Integer.parseInt(view.getLabel_truePredictionCounterValue().getText());
                  float accuracy = 100 * ((float) true_counter / total_counter);
                  view.getLabel_accuracyValue().setText(accuracy + "%");
                }

                if (view.getToggle_button_switch_mode().isSelected()) {
                  view.getPanel_map().validate();
                }
              }
            });

    trajectory_player.addObserver(this);
  }

  private List<Trace> getSelectedTraceList() {
    ArrayList<Trace> selectedTraceList = new ArrayList<Trace>();

    for (int i = getFromIndex(); i < getToIndex(); i++) {
      selectedTraceList.add(traceList.get(i));
    }

    return selectedTraceList;
  }

  public void control() {
    view.getBtnVisualizeTopology().addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          JFrame frame = new JFrame();
          Container content = frame.getContentPane();
          Topology traceListTopology = new Topology(getSelectedTraceList());

          // traceList
          content.add(traceListTopology);
          frame.pack();
          frame.setVisible(true);
          frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
              traceListTopology.stop();
              traceListTopology.destroy();
            }
          });
        } catch (FileNotFoundException e1) {
          e1.printStackTrace();
        }
      }
    });

    view.getButtonTraceModePlayTrajectory().addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {

        System.out.println("ahahah");

        if (!view.getToggle_button_switch_mode().isSelected())
          view.getToggle_button_switch_mode().setSelected(true);

        System.err.println(trajectory_player.getState());
        if (trajectory_player.getState().equals(WorkerState.NEW)
            || trajectory_player.getState().equals(WorkerState.FINISHED)) {

          setTrajectoryPlayer();
          int delayTime = view.getSpeedSlider().getValue();
          trajectory_player.setDelayTime(delayTime);
          trajectory_player.start(true);

          if (!isFocusAndIntereactionsSet) {
            model_merged.setFocusAndInteractionsOfMapView();
            model_true.setFocusAndInteractionsOfMapView();
            model_predicted.setFocusAndInteractionsOfMapView();
            isFocusAndIntereactionsSet = true;
          }
        } else if (trajectory_player.getState().equals(WorkerState.RUNNING)) {
          trajectory_player.pause();
        } else if (trajectory_player.getState().equals(WorkerState.PAUSED)) {
          trajectory_player.resume();
        }
      }
    });

    view.getButtonTraceModeStopTrajectory().addActionListener(e -> {
      System.err.println(trajectory_player.getState());
      trajectory_player.stop();
      view.getLabel_truePredictionCounterValue().setText("0");
      view.getLabel_falsePredictionCounterValue().setText("0");
      view.getLabel_totalMovementCounterValue().setText("0");
      view.getLabel_accuracyValue().setText("0%");
    });

    view.getToggle_button_switch_mode().addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent ev) {
        if (ev.getStateChange() == ItemEvent.SELECTED) {
          System.out.println("button is selected");
          switchButtonAction(true);
        } else if (ev.getStateChange() == ItemEvent.DESELECTED) {
          System.out.println("button is not selected");
          switchButtonAction(false);
        }
      }
    });

    view.getButton_browse_csv().addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Csv Files", "csv");
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(filter);
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("Browse the CSV file to process");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
          System.out.println("getCurrentDirectory(): " + chooser.getCurrentDirectory());
          System.out.println("getSelectedFile() : " + chooser.getSelectedFile());

          String csvFileName = chooser.getSelectedFile().getName();
          activeCsvFileFullPath = chooser.getSelectedFile().toString();
          Country country = (Country) view.getComboBox_Country().getSelectedItem();

          linkLoadTracesBtnAndMapPanel(csvFileName, activeCsvFileFullPath, country);
          setTrajectoryPlayer();

        } else {
          System.out.println("No Selection");
        }
      }
    });

    view.getButtonTeachCsv().addActionListener(e -> {
      FileNameExtensionFilter filter = new FileNameExtensionFilter("Csv Files", "csv");
      JFileChooser chooser = new JFileChooser();
      chooser.setFileFilter(filter);
      chooser.setCurrentDirectory(new java.io.File("."));
      chooser.setDialogTitle("Browse the CSV file to teach HMMModel");
      chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      chooser.setAcceptAllFileFilterUsed(false);
      if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        System.out.println("getSelectedFile() : " + chooser.getSelectedFile());
        final String csvFileFullPath = chooser.getSelectedFile().getAbsolutePath();
        Thread t = new Thread() {
          public void run() {
            
            if(HMM_Controller.getInstance().getHmmProcess() != null) {
              HMM_Controller.getInstance().closeProcessAndConnections();
              HMM_Controller.getInstance(view).startHMMProcess();
              System.out.println("The model is ready..");
            }
            
            HMM_Controller.getInstance().newData(csvFileFullPath,(LearningModel) view.getComboBoxAlgorithm().getSelectedItem()); // ####
          }
        };
        t.start();

      } else {
        System.out.println("No Selection");
      }
    });

    view.getBtnSaveTraces()
        .addActionListener(
            e -> {
              FileNameExtensionFilter filter = new FileNameExtensionFilter("Csv Files", "csv");
              JFileChooser chooser = new JFileChooser();
              chooser.setFileFilter(filter);
              chooser.setCurrentDirectory(new java.io.File("."));
              chooser.setDialogTitle("Browse the CSV file to save modified csv");
              chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
              chooser.setAcceptAllFileFilterUsed(false);
              if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                final String csvFileFullPath = chooser.getSelectedFile().getAbsolutePath();
                System.out.println("saveToFile() : " + csvFileFullPath);
                Path path = Paths.get(csvFileFullPath);
                try {
                  try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                    writer
                        .write("YYYYMMDD,hhmmss (UTC+0),MCC,MNC,LAC,CID,Latitude,Longitude,Timezone,day_number,is_non_working_day,Timestate\n");
                    for (int i = getFromIndex(); i < getToIndex(); i++) {
                      writer.write(traceList.get(i).getCsvRepresentation() + "\n");
                    }
                  }
                } catch (IOException e1) {
                  e1.printStackTrace();
                }

              } else {
                System.out.println("No Selection");
              }
            });

    view.getBtnCreateMininetTopology().addActionListener(e -> {
      sendTracesToPython();
    });

    view.getBtnPlotPredictedMovement().addActionListener(e -> {
      sendPredictionResultsToPython("predicted");
    });

    view.getBtnPlotRealMovement().addActionListener(e -> {
      sendPredictionResultsToPython("real");
    });

    view.getTabbedPane().addChangeListener(e -> {
      System.out.println("Tab: " + view.getTabbedPane().getSelectedIndex());
      int selectedIndex = view.getTabbedPane().getSelectedIndex();

      if (selectedIndex == 0)
        switchButtonAction(view.getToggle_button_switch_mode().isSelected());
      else if (selectedIndex == 1) {
        view.getPanel_map().removeAll();
        if (normalModeRunnable != null) {
          view.getPanel_map().add(normalModeRunnable.getMainView().getMainPanel());
        }
        view.pack();
      }
    });

    view.getButtonNormalModePath().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {

        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("Browse directory to process");
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
          System.out.println("getCurrentDirectory(): " + chooser.getCurrentDirectory());
          System.out.println("getSelectedFile() : " + chooser.getSelectedFile());

          view.getLabelNormalModePath().setText(chooser.getSelectedFile().toString());

        } else {
          System.out.println("No Selection");
        }
      }
    });

    Controller self = this;
    view.getButtonNormalModePlay().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        String filePath = "./.";
        int randomness = 10;
        Trajectory.TrajectoryType type = Trajectory.TrajectoryType.CONSTRAINED_WAYPOINT;
        int settingsId = -1;
        int runId = -1;

        filePath = view.getLabelNormalModePath().getText().trim();
        randomness = Integer.parseInt(view.getTextFieldRandomness().getText().trim());
        type = (Trajectory.TrajectoryType) view.getComboBoxMovementType().getSelectedItem();

        System.out.println(filePath);
        System.out.println(randomness);
        System.out.println(type);

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

        } catch (Exception ex) {
          System.err.println("Illegal path " + ex);
        }

        if (normalModeRunnable == null || normalModeRunnable.getState().equals(WorkerState.NEW)
            || normalModeRunnable.getState().equals(WorkerState.FINISHED)) {
          view.getPanel_map().removeAll();
          normalModeRunnable =
              new NormalModeRunnable(type, randomness, settingsId, runId, view.getPanel_map());
          normalModeRunnable.addObserver(self);
          normalModeRunnable.runExperiment();

        } else if (normalModeRunnable.getState().equals(WorkerState.PAUSED)) {
          normalModeRunnable.resumeExperiment();
        } else if (normalModeRunnable.getState().equals(WorkerState.RUNNING)) {
          normalModeRunnable.pauseExperiment();

        }
      }
    });

    view.getButtonNormalModeStop().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        if (normalModeRunnable != null) {
          normalModeRunnable.pauseExperiment();
          normalModeRunnable = null;
          view.getPanel_map().removeAll();
          view.pack();
        }
      }
    });

    this.model_true.getMapViewer().addMouseListener(new MapViewPopClickListener());
    this.model_predicted.getMapViewer().addMouseListener(new MapViewPopClickListener());
    this.model_merged.getMapViewer().addMouseListener(new MapViewPopClickListener());
  }

  private void switchButtonAction(boolean isSelected) {
    if (isSelected) {
      if (trajectoryViewState.equals("Merged")) {
        view.getPanel_map().removeAll();
        view.getPanel_map().add(model_merged.getMapViewer());
        view.pack();
      }

      // separated mode
      else if (trajectoryViewState.equals("Separated")) {

        view.getPanel_map().removeAll();
        MySplittedPanel msp = new MySplittedPanel();

        msp.getPanel_left().add(model_true.getMapViewer());
        msp.getPanel_right().add(model_predicted.getMapViewer());

        view.getPanel_map().add(msp);

        if (!isFocusAndIntereactionsSet) {
          model_merged.setFocusAndInteractionsOfMapView();
          model_true.setFocusAndInteractionsOfMapView();
          model_predicted.setFocusAndInteractionsOfMapView();
          isFocusAndIntereactionsSet = true;
        }
        view.pack();
      }

    } else {
      view.getPanel_map().removeAll();
      view.getPanel_map().add(model_total.getMapViewer());
      view.pack();
    }
  }

  private void editSlider() {
    times =
        traceList.stream().mapToLong(Trace::getTime).distinct().boxed()
            .collect(Collectors.toList());
    view.getFromSlider().setMinimum(0);
    view.getFromSlider().setMaximum(times.size() - 1);
    view.getFromSlider().setValue(0);

    view.getToSlider().setMinimum(0);
    view.getToSlider().setMaximum(times.size() - 1);
    view.getToSlider().setValue(times.size() - 1);
    Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
    int lastKey = 0;
    for (int i = 0; i < times.size(); i += times.size() / 5) {
      Long t = times.get(i);
      String s = String.format("%d %d %d", Trace.getYear(t), Trace.getMonth(t), Trace.getDay(t));
      labelTable.put(i, new JLabel(s.trim()));
      lastKey = i;
      if (times.size() < 5)
        i++;
    }

    labelTable.remove(lastKey);
    Long lastTime = times.get(times.size() - 1);
    String lastS =
        String.format("%d %d %d", Trace.getYear(lastTime), Trace.getMonth(lastTime),
            Trace.getDay(lastTime));
    labelTable.put(times.size() - 1, new JLabel(lastS.trim()));

    view.getFromSlider().setLabelTable(labelTable);
    view.getFromSlider().setPaintLabels(true);
    view.getFromSlider().setPaintTicks(true);
    view.getFromSlider().setPaintTrack(true);

    view.getToSlider().setLabelTable(labelTable);
    // view.getToSlider().setInverted(true);
    view.getToSlider().setPaintLabels(true);

    view.getSpeedSlider().setMinorTickSpacing(100);
    view.getSpeedSlider().setMajorTickSpacing(300);
    view.getSpeedSlider().setPaintLabels(true);
    view.getSpeedSlider().setPaintTicks(true);

    view.getNormalModeSlider().setMinorTickSpacing(1);
    view.getNormalModeSlider().setMajorTickSpacing(5);
    view.getNormalModeSlider().setPaintLabels(true);
    view.getNormalModeSlider().setPaintTicks(true);

  }

  private void linkLoadTracesBtnAndMapPanel(String csvFileName, String csvFileFullPath,
      Country country) {
    System.err.println("full path : " + csvFileFullPath);
    System.err.println("file name  : " + csvFileName);

    model_total.updateMapViewWithTracesCollectively(csvFileFullPath, country);
    System.out.println("CIKTI");
    model_total.setCsvTraceStatus(csvFileName);

    try {
      traceList = Model.getTraces(csvFileFullPath, country);
      editSlider();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    view.getPanel_map().removeAll();
    view.getPanel_map().add(model_total.getMapViewer());
    view.getLabel_csvTraceStatus().setText(model_total.getCsvTraceStatus());
    view.pack();
  }

  @Override
  public void stateChanged(ChangeEvent e) {
    JSlider slider = (JSlider) e.getSource();

    if (slider.equals(view.getSpeedSlider())) {
      if (trajectory_player != null) {
        int delayTime = slider.getValue();
        trajectory_player.setDelayTime(delayTime);
      }
    } else if (slider.equals(view.getNormalModeSlider())) {
      if (normalModeRunnable != null) {
        long delayTime = slider.getValue();
        normalModeRunnable.setDelayTime(delayTime);
      }
    } else if (slider.equals(view.getFromSlider())) {
      // Perhaps change some text to explain what's going on better.
    } else if (slider.equals(view.getToSlider())) {

    }
  }

  public void sendTracesToPython() {
    String csvFileFullPath = System.getProperty("user.dir") + "/PythonLearning/topology.csv";
    HMM_Controller.getInstance().sendTracesToPython(csvFileFullPath, getFromIndex(), getToIndex(),
        traceList);
  }

  public void sendPredictionResultsToPython(String mode) {

    String csvFileFullPath = System.getProperty("user.dir") + "/PythonLearning/predictions.csv";
    HMM_Controller.getInstance().sendPredictionResultsToPython(csvFileFullPath, mode);
  }

  @Override
  public void update(Observable obs, Object obj) {

    if (obs == this.trajectory_player) {
      if (trajectory_player.getState().equals(WorkerState.NEW)
          || trajectory_player.equals(WorkerState.FINISHED)
          || trajectory_player.getState().equals(WorkerState.PAUSED)) {
        view.getButtonTraceModePlayTrajectory().setIcon(
            new ImageIcon(getClass().getClassLoader().getResource("images/play.png")));
      } else if (trajectory_player.getState().equals(WorkerState.RUNNING))
        view.getButtonTraceModePlayTrajectory().setIcon(
            new ImageIcon(getClass().getClassLoader().getResource("images/pause.png")));
      System.out.println(trajectory_player.getState());
    } else if (obs == this.normalModeRunnable) {
      if (normalModeRunnable.getState().equals(WorkerState.NEW)
          || normalModeRunnable.equals(WorkerState.FINISHED)
          || normalModeRunnable.getState().equals(WorkerState.PAUSED)) {
        view.getButtonNormalModePlay().setIcon(
            new ImageIcon(getClass().getClassLoader().getResource("images/play.png")));
      } else if (normalModeRunnable.getState().equals(WorkerState.RUNNING))
        view.getButtonNormalModePlay().setIcon(
            new ImageIcon(getClass().getClassLoader().getResource("images/pause.png")));
      System.err.println(normalModeRunnable.getState());
    }

  }

  class MapViewPopClickListener extends MouseAdapter {

    public void mousePressed(MouseEvent e) {
      if (e.isPopupTrigger())
        doPop(e);
    }

    public void mouseReleased(MouseEvent e) {
      if (e.isPopupTrigger())
        doPop(e);
    }

    private void doPop(MouseEvent e) {
      JPopupMenu menu;
      JMenuItem item;

      // int x = e.getX();
      // int y = e.getY();

      try {

        menu = new JPopupMenu();
        item = null;
        if (trajectoryViewState.equals("Separated"))
          item = new JMenuItem("Merge Views");
        else if (trajectoryViewState.equals("Merged"))
          item = new JMenuItem("Separate Views");

        menu.add(item);

        item.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (trajectoryViewState.equals("Separated"))
              trajectoryViewState = "Merged";
            else
              trajectoryViewState = "Separated";

            if (trajectoryViewState.equals("Merged")) {
              view.getPanel_map().removeAll();
              view.getPanel_map().add(model_merged.getMapViewer());
            } else if (trajectoryViewState.equals("Separated")) {
              view.getPanel_map().removeAll();
              MySplittedPanel msp = new MySplittedPanel();
              msp.getPanel_left().add(model_true.getMapViewer());
              msp.getPanel_right().add(model_predicted.getMapViewer());
              view.getPanel_map().add(msp);

              if (!isFocusAndIntereactionsSet) {
                model_true.setFocusAndInteractionsOfMapView();
                model_predicted.setFocusAndInteractionsOfMapView();
                isFocusAndIntereactionsSet = true;
              }
            }
            view.pack();
            System.err.println(trajectoryViewState);
          }
        });
        menu.show(e.getComponent(), e.getX(), e.getY());

      } catch (Exception ex) {
        System.out.println(ex);
        menu = null;
        item = null;
      }

    }
  }

}
