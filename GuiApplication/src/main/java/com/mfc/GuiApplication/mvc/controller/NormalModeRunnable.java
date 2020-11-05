package com.mfc.GuiApplication.mvc.controller;

import java.awt.Point;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.Observable;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.dailab.nemo.imovefan.learningvalidation.io.StatisticsWriter;
import de.dailab.nemo.imovefan.learningvalidation.learning.ControllerLearning;
import de.dailab.nemo.imovefan.learningvalidation.mobility.Action;
import de.dailab.nemo.imovefan.learningvalidation.mobility.TimeProvider;
import de.dailab.nemo.imovefan.learningvalidation.mobility.Trajectory;
import de.dailab.nemo.imovefan.learningvalidation.topo.Cell;
import de.dailab.nemo.imovefan.learningvalidation.topo.Host;
import de.dailab.nemo.imovefan.learningvalidation.topo.TopoUtil;
import de.dailab.nemo.imovefan.learningvalidation.topo.Topology;
import de.dailab.nemo.imovefan.learningvalidation.ui.MainView;

/**
 * Created by nemo-develop on 23.10.15.
 */
public class NormalModeRunnable extends Observable implements Runnable {

  private final static Logger logger = LogManager.getLogger(NormalModeRunnable.class.getName());

  private boolean workDone = false;
  private volatile boolean pauseWork = false;
  private volatile WorkerState state = WorkerState.NEW;
  private Thread workerThread;
  private long delayTime = 30L;


  private final int randomness;
  private final Trajectory.TrajectoryType trajectoryType;

  private final static boolean VISUALIZE = true;

  private Topology topology;

  private int settingsId;
  private int runId;
  private final MainView[] mv = new MainView[1];

  private JPanel normalPanel;

  public MainView getMainView() {
    return mv[0];
  }

  private static void printUsage() {
    System.err.println("Usage: <program> trajectory_type randomness data_path [settingsId runId]");
    System.err.println("\t<program>:\t\t\tThe program itself");
    System.err.println("\ttrajectory_type:\t"
        + Trajectory.TrajectoryType.CONSTRAINED_WAYPOINT.getValue() + "|"
        + Trajectory.TrajectoryType.RANDOM_WAYPOINT.getValue());
    System.err.println("\trandomness:\t\t\t[0,100]");
    System.err.println("\tdata_path:\t\t\tPath where the data-folder will be created");
    System.err.println("\tsettings_id:\t\t\tSettings-Id for Cell-Usage [1, 2]");
    System.err.println("\trun_id:\t\t\tRun-Id for Cell-Usage [1, 2]");


    System.exit(1);
  }


  /*
   * ******************************************+ MAIN IMPLEMENTATION
   * ******************************************+
   */

  public NormalModeRunnable(Trajectory.TrajectoryType type, int randomness, int settingsId,
      int runId, JPanel normalPanel) {
    this.trajectoryType = type;
    this.randomness = randomness;
    this.settingsId = settingsId;
    this.runId = runId;
    this.normalPanel = normalPanel;


    logger.debug("Settings-ID: {}, Run-ID: {}", settingsId, runId);
    if (settingsId == -1 || runId == -1) {
      logger.info("Will use pyramid-scheme usage for cells");
    }
  }

  /**
   * Only relevant for UI<br>
   * Move host from one position to another position. Also interpolate time and usage of the cells.<br>
   * The whole movement is split up into 50 steps
   * 
   * @param host The host to move
   * @param from The host start position
   * @param to The host end position
   * @param fromTime The starting time (not real time, but virtual time-stamp)
   * @param toTime The ending time (not real time, but virtual time-stamp)
   * @param fromUsage The Cell usage at start
   * @param toUsage The Cell usage at end
   * @param waitTime The Sleep time (defines speed of animation)
   * @param mv The View that needs to be repainted after each step.
   */
  public void interpolateBetweenPositions(Host host, Point from, Point to, Date fromTime,
      Date toTime, float[] fromUsage, float[] toUsage, long waitTime, MainView mv) {
    Point pos;
    int max = 50;
    for (int i = 0; i < max; i++) {
      pos = new Point((to.x * i + from.x * (max - i)) / max, (to.y * i + from.y * (max - i)) / max);

      host.setPosition(new Point(pos.x, pos.y));
      interpolateTimeStep(fromTime, toTime, (float) i / (float) max, mv);
      interpolateCellUsages(fromUsage, toUsage, (float) i / (float) max);

      SwingUtilities.invokeLater(() -> mv.repaint());

      try {
        Thread.sleep(delayTime);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Interpolate between to Dates.
   * 
   * @param from The start date
   * @param to The end date
   * @param delta The current interpolation delta (0 at start, 1 at end)
   * @param mv The View to apply changes to.
   */
  public void interpolateTimeStep(Date from, Date to, float delta, MainView mv) {
    long fromLong = from.getTime();
    long toLong = to.getTime();

    Date res = new Date();
    res.setTime((long) ((1.0f - delta) * fromLong + delta * toLong));

    mv.setCurrentTimeStep(res);
  }

  /**
   * Interpolate between cell usages
   * 
   * @param from Array of usages at start
   * @param to Array of usages at end
   * @param delta The current interpolation delta (0 at start, 1 at end)
   */
  public void interpolateCellUsages(float[] from, float[] to, float delta) {
    java.util.List<Cell> cells = topology.cells;
    for (int i = 0; i < cells.size(); i++) {
      Cell c = cells.get(i);
      c.setRelativeUsage((1.0f - delta) * from[i] + delta * to[i]);
    }
  }

  /**
   * Main Entry point for experiment
   * 
   * @throws InvocationTargetException
   * @throws InterruptedException
   */
  public void runExperiment() {
    this.start(true);
  }

  /**
   * Change the service type of the host with respect to the current timestep. <br>
   * Will simply split up day in three slots: [DATA, AUDIO, VIDEO] at [MORNING, NOON, EVENING]
   * 
   * @param timestep The current timestep
   * @param host The host to update
   */
  private void updateHostServiceType(int timestep, Host host) {
    {
      // TODO: possibly remove this
      if (true) {
        host.setRequestType(Host.RequestType.VIDEO);
        return;
      }
    }

    int noon = (int) ((float) TimeProvider.HO_PER_DAY / 3.f + 0.5f);
    int evening = 2 * noon;

    if (timestep < noon)
      host.setRequestType(Host.RequestType.DATA);
    else if (timestep < evening)
      host.setRequestType(Host.RequestType.AUDIO);
    else
      host.setRequestType(Host.RequestType.VIDEO);

    logger.trace("Host-Type set to {} at step {}", host.getRequestType(), timestep);
  }

  /**
   * Update the usage of all cells with respect to the timestep
   * 
   * @param step The current timestep
   */
  private void updateCellBandwidths(int step) {

    float[] bandwidths = getCellBandwidths(step);

    for (int i = 0; i < topology.cells.size(); i++) {

      Cell cell = topology.cells.get(i);
      cell.setRelativeUsage(bandwidths[i]);

      logger.trace("Relative usage set to {} at step {} for cell-type {}, available: {}",
          (bandwidths[i]), step, cell.getType(), cell.getAvailableBandwidth());
    }

  }

  /**
   * Calculates the cell usage for a given timestep.<br>
   * Creates a pyramid-scheme over each day. 0 at morning, 0.8 at middle of day, 0 at night.
   * 
   * @param step The timestep to calcuate the usage for
   * @return The usage
   */
  private float[] getCellBandwidths(int step) {

    if (settingsId == 1) {
      if (runId == 1) {
        return getSettingOneRunOneBandwidths();
      } else if (runId == 2) {
        return getSettingOneRunTwoBandwidths();
      }
    } else if (settingsId == 2) {
      if (runId == 1) {
        return getSettingTwoRunOneBandwidths();
      } else if (runId == 2) {
        return getSettingTwoRunTwoBandwidths();
      }
    } else if (settingsId == 3) {
      return getSettingsThreeBandwidths();
    }


    float[] bws = new float[topology.cells.size()];

    float maxRelativeUsage = 0.8f;
    // use pyramid scheme with
    for (int i = 0; i < topology.cells.size(); i++) {

      float middle = (float) TimeProvider.HO_PER_DAY / 2.f;

      float relativeUsage;
      if ((float) step > middle) {
        relativeUsage = (float) step / middle;
        relativeUsage -= 1.0f;
        relativeUsage = 1.0f - relativeUsage;
      } else {
        relativeUsage = (float) step / middle;
      }

      bws[i] = relativeUsage * maxRelativeUsage;
    }

    return bws;
  }


  /**
   * All cells are loaded the same (0%)
   * 
   * @return The used bandwidth of the cell in [0.0, 1.0]
   */
  private float[] getSettingOneRunOneBandwidths() {
    float[] bws = new float[topology.cells.size()];

    for (int i = 0; i < bws.length; i++) {
      bws[i] = 0.f;
    }

    return bws;
  }

  /**
   * LTE-Cells are loaded by 80% and WLAN-Cells by 0%
   * 
   * @return The used bandwidth of the cell in [0.0, 1.0]
   */
  private float[] getSettingOneRunTwoBandwidths() {
    float[] bws = new float[topology.cells.size()];

    for (int i = 0; i < bws.length; i++) {
      if (topology.cells.get(i).getType() == Cell.CellType.LTE)
        bws[i] = 0.9f;
      else
        bws[i] = 0.0f;
    }

    return bws;
  }

  /**
   * Cells with even ID are loaded by 80%, other Cells by 0%
   * 
   * @return The used bandwidth of the cell in [0.0, 1.0]
   */
  private float[] getSettingTwoRunOneBandwidths() {
    float[] bws = new float[topology.cells.size()];

    for (int i = 0; i < bws.length; i++) {
      boolean loaded = (i % 2) == 0;

      if (loaded)
        bws[i] = 0.9f;
      else
        bws[i] = 0.0f;
    }

    return bws;
  }

  /**
   * Cells with odd ID are loaded by 80%, other Cells by 0%
   * 
   * @return The used bandwidth of the cell in [0.0, 1.0]
   */
  private float[] getSettingTwoRunTwoBandwidths() {
    float[] bws = new float[topology.cells.size()];

    for (int i = 0; i < bws.length; i++) {
      boolean loaded = (i % 2) == 1;

      if (loaded)
        bws[i] = 0.8f;
      else
        bws[i] = 0.0f;
    }

    return bws;
  }


  public float[] getSettingsThreeBandwidths() {
    float bws[] = new float[topology.cells.size()];

    if (topology.cells.size() != 8) {
      throw new IllegalStateException("Wrong topology for settings 3!");
    }

    bws[0] = 0.0f;
    bws[1] = 0.0f;
    bws[2] = 0.0f;
    bws[3] = 0.0f;
    bws[4] = 0.0f;
    bws[5] = 0.0f;
    bws[6] = 0.0f;
    bws[7] = 0.0f;

    return bws;
  }

  @Override
  public void run() {


    // create a fixed topology with only one host in it
    if (settingsId == 3) {
      topology = TopoUtil.createTUTopology();
    } else {
      topology = TopoUtil.createTestTopology();
    }
    // create a randomized trajectory withing the topology.
    // 2D array for day and time in day
    Trajectory trajectory = new Trajectory(topology, trajectoryType, randomness);
    Point[][] generatedTrajectory = trajectory.generateTrajectory(settingsId);
    // the corresponding timestamps for each day and time in day
    Date[][] timestamps = TimeProvider.getInstance().getTimestamps();

    ControllerLearning controllerLearning = new ControllerLearning(topology);
    Cell prediction = null;

    if (VISUALIZE) {
      try {
        SwingUtilities.invokeAndWait(() -> {
          mv[0] = new MainView(topology, normalPanel);
          mv[0].open();
        });
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      }
    }

    int day = 0;
    while (!workDone) {

      if (VISUALIZE)
        mv[0].setCurrentTrajectory(generatedTrajectory[day]);

      for (int step = 0; step < TimeProvider.HO_PER_DAY && !workDone; step++) {

        while (pauseWork) {
          setState(WorkerState.PAUSED);
          try {
            Thread.sleep(5000); // stop for 100ms increments
          } catch (InterruptedException ie) {
            // report or ignore
          }
        }
        setState(WorkerState.RUNNING);

        topology.hosts.get(0).setPosition(generatedTrajectory[day][step]);

        updateCellBandwidths(step);
        updateHostServiceType(step, topology.hosts.get(0));

        // move a host
        Action hostAction =
            topology.hosts.get(0).autoUpdateConnection(day, step, getCellBandwidths(step));

        String predictedString = prediction == null ? "-1" : prediction.getId() + "";
        if (hostAction.connectionPoint.equals(prediction)) {
          logger.info("Found correct prediction for host 1 at time {}, prediction was {}", step,
              predictedString);
        } else {
          logger.info("Found wrong prediction for host 1 at time {}, prediction was {}", step,
              predictedString);
        }

        // store what user did
        controllerLearning.addAction(hostAction, day, step, false, getCellBandwidths(step));

        // predict if possible (only not possible on last time-step of last day)
        prediction =
            step < TimeProvider.HO_PER_DAY - 1 ? controllerLearning.predict(day, step + 1)
                : day < TimeProvider.EXPERIMENT_DAYS ? controllerLearning.predict(day + 1, 0)
                    : null;

        if (VISUALIZE) {

          interpolateBetweenPositions(topology.hosts.get(0), generatedTrajectory[day][step],
              step < TimeProvider.HO_PER_DAY - 1 ? generatedTrajectory[day][step + 1]
                  : generatedTrajectory[day][step], timestamps[day][step],
              step < TimeProvider.HO_PER_DAY - 1 ? timestamps[day][step + 1]
                  : timestamps[day][step], getCellBandwidths(step),
              step < TimeProvider.HO_PER_DAY - 1 ? getCellBandwidths(step + 1)
                  : getCellBandwidths(step), 15L, // onemli
              mv[0]);

          mv[0].setCurrentTimeStep(timestamps[day][step]);
        }

        day++;
        workDone = day >= TimeProvider.EXPERIMENT_DAYS;

      }
    }
    setState(WorkerState.FINISHED);


    StatisticsWriter writer = StatisticsWriter.getInstance(topology);
    writer.waitToFinish();
  }



  public void pauseExperiment() {
    this.pauseWork = true;
    setState(WorkerState.PAUSED);
  }

  public void resumeExperiment() {
    this.pauseWork = false;
    setState(WorkerState.RUNNING);
    if (workerThread != null)
      workerThread.interrupt(); // wakeup if sleeping
  }

  // public void stop() {
  // this.workDone = true;
  // setState(WorkerState.NEW);
  // }
  //

  public void setState(WorkerState state) {
    if (this.state != state) {
      this.state = state;
      setChanged();
      notifyObservers();
    }
  }

  public WorkerState getState() {
    return state;
  }

  public void start(boolean startImmediately) {
    this.pauseWork = !startImmediately;
    workerThread = new Thread(this);
    workerThread.start();
  }

  public void setDelayTime(long delayTime) {
    this.delayTime = delayTime;
  }
}
