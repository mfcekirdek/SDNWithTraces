package com.mfc.GuiApplication.mvc.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultButtonModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import com.mfc.GuiApplication.enums.Country;
import com.mfc.GuiApplication.enums.LearningModel;

import de.dailab.nemo.imovefan.learningvalidation.mobility.Trajectory;

public class View {

	private JFrame frame;
	private JPanel panel_map;
	private JLabel label_csvTraceStatus;
	private JLabel label_learnerStatus;
	private JButton button_browse_csv;
	private JButton button_teach_csv;
	private JToggleButton toggle_button_switch_mode;
	private JButton buttonTraceModeStopTrajectory;
	private JButton buttonTraceModePlayTrajectory;
	private JButton buttonNormalModeStop;
	private JButton buttonNormalModePlay;
	private JButton btnVisualizeTopology;
	private JButton btnSaveTraces;
	private JSlider fromSlider;
	private JSlider toSlider;
	private JSlider speedSlider;
	private JLabel label_totalMovementCounterValue;
	private JLabel label_accuracyValue;
	private JLabel label_falsePredictionCounterValue;
	private JLabel label_truePredictionCounterValue;
	private JButton btnCreateMininetTopology;
	private JButton btnPlotRealMovement;
	private JButton btnPlotPredictedMovement;
	private JComboBox<Country> comboBox_Country;
	private JTabbedPane tabbedPane;
	private JTextField textFieldRandomness;
	private JLabel labelNormalModePath;
	private JSlider normalModeSlider;
	private JButton buttonNormalModePath;
	private JComboBox<Trajectory.TrajectoryType> comboBoxMovementType;
	private JComboBox<LearningModel> comboBoxAlgorithm;
	
	/**
	 * Create the application.
	 */
	public View() {

		try {
			// select Look and Feel
			UIManager.setLookAndFeel("com.jtattoo.plaf.fast.FastLookAndFeel");
//			UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
//			UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");

			// start application
			initialize();
			this.frame.setVisible(true);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void pack() {
		frame.pack();
	}

	private void initialize() {
		frame = new JFrame();
		Dimension d = new Dimension(1200, 700);
		frame.setPreferredSize(d);
		frame.setBounds(100, 100, 1200, 700);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 407, 500, 0 };
		gridBagLayout.rowHeights = new int[] { 20, 20, 0, 50, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 1.0, 0.0,
				Double.MIN_VALUE };
		frame.getContentPane().setLayout(gridBagLayout);

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
		gbc_tabbedPane.insets = new Insets(0, 0, 5, 5);
		gbc_tabbedPane.gridheight = 3;
		gbc_tabbedPane.fill = GridBagConstraints.BOTH;
		gbc_tabbedPane.gridx = 0;
		gbc_tabbedPane.gridy = 0;
		frame.getContentPane().add(tabbedPane, gbc_tabbedPane);

		JPanel panel_tracing = new JPanel();
		panel_tracing.setBorder(new TitledBorder(null, "Tracing Mode",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		tabbedPane.addTab("Tracing Emulation Mode", null, panel_tracing, null);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 30, 0, 0, 30, 0, 0, 0 };
		gbl_panel.rowHeights = new int[] { 56, 0, 73 };
		gbl_panel.columnWeights = new double[] { 1.0, 1.0, 0.0, 0.0, 0.0, 1.0 };
		gbl_panel.rowWeights = new double[] { 0.0, 1.0, 1.0 };
		panel_tracing.setLayout(gbl_panel);

		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(UIManager
				.getBorder("TitledBorder.border"), "Import Traces",
				TitledBorder.LEADING, TitledBorder.TOP, null,
				new Color(0, 0, 0)));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.gridwidth = 6;
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		panel_tracing.add(panel, gbc_panel);
		GridBagLayout gbl_panel_3 = new GridBagLayout();
		gbl_panel_3.rowHeights = new int[] { 0, 0, 31, 0 };
		gbl_panel_3.rowWeights = new double[] { 0.0, 0.0, 1.0, 1.0 };
		gbl_panel_3.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 1.0 };
		gbl_panel_3.columnWidths = new int[] { 100, 0, 0, 0, 0, 20 };
		gbl_panel.columnWidths = new int[] { 0, 0, 0, 0, 0, 0 };
		gbl_panel.rowHeights = new int[] { 0, 20, 0, 20, 0, 0 };
		gbl_panel.columnWeights = new double[] { 1.0, 0.0, 0.0, 0.0, 1.0,
				Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0,
				Double.MIN_VALUE };
		panel.setLayout(gbl_panel_3);

		JLabel lblLearner = new JLabel("Model Status:");
		lblLearner.setFont(new Font("SansSerif", Font.BOLD, 17));
		GridBagConstraints gbc_lblLearner = new GridBagConstraints();
		gbc_lblLearner.anchor = GridBagConstraints.WEST;
		gbc_lblLearner.insets = new Insets(0, 0, 5, 5);
		gbc_lblLearner.gridx = 0;
		gbc_lblLearner.gridy = 0;
		panel.add(lblLearner, gbc_lblLearner);

		JLabel dotLabel = new JLabel(":");
		dotLabel.setFont(new Font("SansSerif", Font.BOLD, 17));
		GridBagConstraints gbc_dotLabel = new GridBagConstraints();
		gbc_dotLabel.insets = new Insets(0, 0, 5, 5);
		gbc_dotLabel.gridx = 2;
		gbc_dotLabel.gridy = 0;
		panel.add(dotLabel, gbc_dotLabel);

		label_learnerStatus = new JLabel("Not initialized");
		label_learnerStatus.setForeground(Color.GRAY);
		label_learnerStatus.setFont(new Font("SansSerif", Font.BOLD, 17));
		GridBagConstraints gbc_label_learnerStatus = new GridBagConstraints();
		gbc_label_learnerStatus.gridwidth = 2;
		gbc_label_learnerStatus.anchor = GridBagConstraints.EAST;
		gbc_label_learnerStatus.insets = new Insets(0, 0, 5, 0);
		gbc_label_learnerStatus.gridx = 4;
		gbc_label_learnerStatus.gridy = 0;
		panel.add(label_learnerStatus, gbc_label_learnerStatus);

		JLabel lblTraces = new JLabel("Traces");
		lblTraces.setFont(new Font("SansSerif", Font.BOLD, 17));
		GridBagConstraints gbc_lblTraces = new GridBagConstraints();
		gbc_lblTraces.anchor = GridBagConstraints.WEST;
		gbc_lblTraces.insets = new Insets(0, 0, 35, 5);
		gbc_lblTraces.gridx = 0;
		gbc_lblTraces.gridy = 0;
		panel.add(lblTraces, gbc_lblTraces);

		JLabel label = new JLabel(":");
		label.setFont(new Font("SansSerif", Font.BOLD, 17));
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.insets = new Insets(0, 0, 35, 5);
		gbc_label.gridx = 2;
		gbc_label.gridy = 0;
		panel.add(label, gbc_label);

		label_csvTraceStatus = new JLabel("Not loaded");
		label_csvTraceStatus.setForeground(Color.GRAY);
		label_csvTraceStatus.setFont(new Font("SansSerif", Font.BOLD, 17));
		GridBagConstraints gbc_label_csvTraceStatus = new GridBagConstraints();
		gbc_label_csvTraceStatus.gridwidth = 2;
		gbc_label_csvTraceStatus.anchor = GridBagConstraints.EAST;
		gbc_label_csvTraceStatus.insets = new Insets(0, 0, 35, 0);
		gbc_label_csvTraceStatus.gridx = 4;
		gbc_label_csvTraceStatus.gridy = 0;
		panel.add(label_csvTraceStatus, gbc_label_csvTraceStatus);

		JPanel panel_3 = new JPanel();
		GridBagConstraints gbc_panel_3 = new GridBagConstraints();
		gbc_panel_3.insets = new Insets(0, 0, 5, 0);
		gbc_panel_3.anchor = GridBagConstraints.EAST;
		gbc_panel_3.gridwidth = 6;
		gbc_panel_3.fill = GridBagConstraints.VERTICAL;
		gbc_panel_3.gridx = 0;
		gbc_panel_3.gridy = 2;
		panel.add(panel_3, gbc_panel_3);
		// JComboBox comboBox_Country = new JComboBox();
		// panel_3.add(comboBox_Country);
		// comboBox_Country.addItem(Country.ALL);
		// comboBox_Country.addItem(Country.CZECH_REPUBLIC);
		// comboBox_Country.addItem(Country.SLOVAKIA);
		// comboBox_Country.addItem(Country.AUSTRIA);
		// comboBox_Country.addItem(Country.UNITED_STATES);

		button_teach_csv = new JButton("Teach traces");
		panel_3.add(button_teach_csv);

		button_browse_csv = new JButton("Load Traces");
		panel_3.add(button_browse_csv);

		toggle_button_switch_mode = new JToggleButton("Switch View");
		panel_3.add(toggle_button_switch_mode);

		buttonTraceModePlayTrajectory = new RoundButton(new ImageIcon(getClass()
				.getClassLoader().getResource("images/play.png")));
		panel_3.add(buttonTraceModePlayTrajectory);

		buttonTraceModeStopTrajectory = new RoundButton(new ImageIcon(getClass()
				.getClassLoader().getResource("images/stop.png")));
		panel_3.add(buttonTraceModeStopTrajectory);

		JPanel panel_8 = new JPanel();
		GridBagConstraints gbc_panel_8 = new GridBagConstraints();
		gbc_panel_8.anchor = GridBagConstraints.EAST;
		gbc_panel_8.insets = new Insets(0, 0, 5, 0);
		gbc_panel_8.fill = GridBagConstraints.VERTICAL;
		gbc_panel_8.gridx = 5;
		gbc_panel_8.gridy = 3;
		panel.add(panel_8, gbc_panel_8);

		JLabel lblFilterByCountry = new JLabel("Filter by Country : ");
		panel_8.add(lblFilterByCountry);

		comboBox_Country = new JComboBox<Country>();
		panel_8.add(comboBox_Country);

		comboBox_Country.addItem(Country.ALL);
		comboBox_Country.addItem(Country.CZECH_REPUBLIC);
		comboBox_Country.addItem(Country.SLOVAKIA);
		comboBox_Country.addItem(Country.AUSTRIA);
		comboBox_Country.addItem(Country.UNITED_STATES);

		speedSlider = new JSlider(100, 1000, 500);
		speedSlider.setInverted(true);
		GridBagConstraints gbcSpeedSlider = new GridBagConstraints();
		gbcSpeedSlider.fill = GridBagConstraints.BOTH;
		gbcSpeedSlider.gridx = 0;
		gbcSpeedSlider.gridy = 4;
		gbcSpeedSlider.gridwidth = 6;
		gbcSpeedSlider.weightx = 1.0f;
		panel.add(speedSlider, gbcSpeedSlider);

		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new TitledBorder(null, "Select Algorithm",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.insets = new Insets(0, 0, 5, 0);
		gbc_panel_2.gridwidth = 6;
		gbc_panel_2.fill = GridBagConstraints.BOTH;
		gbc_panel_2.gridx = 0;
		gbc_panel_2.gridy = 1;
		panel_tracing.add(panel_2, gbc_panel_2);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[] { 100, 5, 0, 20, 0, 0 };
		gbl_panel_2.rowHeights = new int[] { 0, 0, 0, 0 };
		gbl_panel_2.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 1.0,
				Double.MIN_VALUE };
		gbl_panel_2.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panel_2.setLayout(gbl_panel_2);

		JLabel lblAlgorithm = new JLabel("Algorithm");
		lblAlgorithm.setFont(new Font("SansSerif", Font.BOLD, 17));
		GridBagConstraints gbc_lblAlgorithm = new GridBagConstraints();
		gbc_lblAlgorithm.anchor = GridBagConstraints.WEST;
		gbc_lblAlgorithm.insets = new Insets(0, 0, 5, 5);
		gbc_lblAlgorithm.gridx = 0;
		gbc_lblAlgorithm.gridy = 0;
		panel_2.add(lblAlgorithm, gbc_lblAlgorithm);

		JLabel label_2 = new JLabel(":");
		label_2.setFont(new Font("SansSerif", Font.BOLD, 17));
		GridBagConstraints gbc_label_2 = new GridBagConstraints();
		gbc_label_2.insets = new Insets(0, 0, 5, 5);
		gbc_label_2.gridx = 2;
		gbc_label_2.gridy = 0;
		panel_2.add(label_2, gbc_label_2);

		comboBoxAlgorithm = new JComboBox<LearningModel>();
		GridBagConstraints gbc_comboBoxAlgorithm = new GridBagConstraints();
		gbc_comboBoxAlgorithm.insets = new Insets(0, 0, 5, 0);
		gbc_comboBoxAlgorithm.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBoxAlgorithm.gridx = 4;
		gbc_comboBoxAlgorithm.gridy = 0;
	    comboBoxAlgorithm.addItem(LearningModel.MARKOV_CHAIN_MODEL);
		comboBoxAlgorithm.addItem(LearningModel.HIDDEN_MARKOV_MODEL);
		panel_2.add(comboBoxAlgorithm, gbc_comboBoxAlgorithm);

		JPanel panel_4 = new JPanel();
		panel_4.setBorder(new TitledBorder(null, "Trajectory Results",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_panel_4 = new GridBagConstraints();
		gbc_panel_4.gridwidth = 6;
		gbc_panel_4.insets = new Insets(0, 0, 0, 5);
		gbc_panel_4.fill = GridBagConstraints.BOTH;
		gbc_panel_4.gridx = 0;
		gbc_panel_4.gridy = 2;
		panel_tracing.add(panel_4, gbc_panel_4);
		GridBagLayout gbl_panel_4 = new GridBagLayout();
		gbl_panel_4.columnWidths = new int[] { 0, 0, 0, 0, 0 };
		gbl_panel_4.rowHeights = new int[] { 14, 0, 14, 14, 0, 0 };
		gbl_panel_4.columnWeights = new double[] { 1.0, 0.0, 0.0, 1.0,
				Double.MIN_VALUE };
		gbl_panel_4.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 1.0,
				Double.MIN_VALUE };
		panel_4.setLayout(gbl_panel_4);

		JLabel label1 = new JLabel("True Predicted Movement Counter : ",
				SwingConstants.LEFT);
		label1.setFont(new Font("SansSerif", Font.BOLD, 13));
		GridBagConstraints gbc_label1 = new GridBagConstraints();
		gbc_label1.fill = GridBagConstraints.BOTH;
		gbc_label1.insets = new Insets(0, 0, 5, 5);
		gbc_label1.gridx = 0;
		gbc_label1.gridy = 0;
		panel_4.add(label1, gbc_label1);

		label_truePredictionCounterValue = new JLabel("0");
		label_truePredictionCounterValue.setForeground(Color.GREEN);
		GridBagConstraints gbc_label_truePredictionCounterValue = new GridBagConstraints();
		gbc_label_truePredictionCounterValue.insets = new Insets(0, 0, 5, 0);
		gbc_label_truePredictionCounterValue.gridx = 3;
		gbc_label_truePredictionCounterValue.gridy = 0;
		panel_4.add(label_truePredictionCounterValue,
				gbc_label_truePredictionCounterValue);

		JLabel label5 = new JLabel("False Predicted Movement Counter : ",
				SwingConstants.LEFT);
		label5.setFont(new Font("SansSerif", Font.BOLD, 13));

		GridBagConstraints gbc_label5 = new GridBagConstraints();
		gbc_label5.fill = GridBagConstraints.BOTH;
		gbc_label5.insets = new Insets(0, 0, 5, 5);
		gbc_label5.gridx = 0;
		gbc_label5.gridy = 1;
		panel_4.add(label5, gbc_label5);

		label_falsePredictionCounterValue = new JLabel("0");
		label_falsePredictionCounterValue.setForeground(Color.RED);
		GridBagConstraints gbc_label_falsePredictionCounterValue = new GridBagConstraints();
		gbc_label_falsePredictionCounterValue.insets = new Insets(0, 0, 5, 0);
		gbc_label_falsePredictionCounterValue.gridx = 3;
		gbc_label_falsePredictionCounterValue.gridy = 1;
		panel_4.add(label_falsePredictionCounterValue,
				gbc_label_falsePredictionCounterValue);

		JLabel label2 = new JLabel("Total Movement Counter : ",
				SwingConstants.LEFT);
		label2.setFont(new Font("SansSerif", Font.BOLD, 13));

		GridBagConstraints gbc_label2 = new GridBagConstraints();
		gbc_label2.fill = GridBagConstraints.BOTH;
		gbc_label2.insets = new Insets(0, 0, 5, 5);
		gbc_label2.gridx = 0;
		gbc_label2.gridy = 2;
		panel_4.add(label2, gbc_label2);

		label_totalMovementCounterValue = new JLabel("0");
		GridBagConstraints gbc_label_TotalMovementCounterValue = new GridBagConstraints();
		gbc_label_TotalMovementCounterValue.insets = new Insets(0, 0, 5, 0);
		gbc_label_TotalMovementCounterValue.gridx = 3;
		gbc_label_TotalMovementCounterValue.gridy = 2;
		panel_4.add(label_totalMovementCounterValue,
				gbc_label_TotalMovementCounterValue);
		JLabel label3 = new JLabel("Accuracy : ", SwingConstants.LEFT);
		label3.setFont(new Font("SansSerif", Font.BOLD, 13));

		GridBagConstraints gbc_label3 = new GridBagConstraints();
		gbc_label3.insets = new Insets(0, 0, 5, 5);
		gbc_label3.fill = GridBagConstraints.BOTH;
		gbc_label3.gridx = 0;
		gbc_label3.gridy = 3;
		panel_4.add(label3, gbc_label3);

		label_accuracyValue = new JLabel("0");
		GridBagConstraints gbc_label_AccuracyValue = new GridBagConstraints();
		gbc_label_AccuracyValue.insets = new Insets(0, 0, 5, 0);
		gbc_label_AccuracyValue.gridx = 3;
		gbc_label_AccuracyValue.gridy = 3;
		panel_4.add(label_accuracyValue, gbc_label_AccuracyValue);

		JPanel panel_6 = new JPanel();
		GridBagConstraints gbc_panel_6 = new GridBagConstraints();
		gbc_panel_6.anchor = GridBagConstraints.EAST;
		gbc_panel_6.gridwidth = 4;
		gbc_panel_6.fill = GridBagConstraints.VERTICAL;
		gbc_panel_6.gridx = 0;
		gbc_panel_6.gridy = 4;
		panel_4.add(panel_6, gbc_panel_6);

		btnPlotRealMovement = new JButton("Plot Real Movement Graph");
		panel_6.add(btnPlotRealMovement);

		btnPlotPredictedMovement = new JButton("Plot Predicted Movement Graph");
		panel_6.add(btnPlotPredictedMovement);
		btnVisualizeTopology = new JButton("Visualize Topology");
		panel_6.add(btnVisualizeTopology);

		JPanel panel_normal = new JPanel();
		panel_normal.setBorder(new TitledBorder(null, "Normal Mode",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		tabbedPane.addTab("Normal Emulation Mode", null, panel_normal, null);
		GridBagLayout gbl_panel_5 = new GridBagLayout();
		panel_normal.setLayout(gbl_panel_5);

		JPanel panel_5 = new JPanel();
		panel_5.setBorder(new TitledBorder(null, "CSV Editor",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_panel_5 = new GridBagConstraints();
		gbc_panel_5.gridwidth = 6;
		gbc_panel_5.gridheight = 2;
		gbc_panel_5.insets = new Insets(0, 0, 5, 0);
		gbc_panel_5.fill = GridBagConstraints.BOTH;
		gbc_panel_5.gridx = 0;
		gbc_panel_5.gridy = 3;
		panel_tracing.add(panel_5, gbc_panel_5);
		GridBagLayout gbl_panel_51 = new GridBagLayout();
		gbl_panel_51.rowWeights = new double[] { 0.0, 0.0, 1.0 };
		gbl_panel_51.columnWeights = new double[] { 1.0 };
		gbl_panel_5.columnWidths = new int[] { 0, 0, 0, 0 };
		gbl_panel_5.rowHeights = new int[] { 14, 0, 0, 14, 0 };
		gbl_panel_5.columnWeights = new double[] { 1.0, 0.0, 1.0,
				Double.MIN_VALUE };
		gbl_panel_5.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0,
				Double.MIN_VALUE };

		JPanel panel_9 = new JPanel();
		panel_9.setBorder(new TitledBorder(UIManager
				.getBorder("TitledBorder.border"), "Import Traces",
				TitledBorder.LEADING, TitledBorder.TOP, null,
				new Color(0, 0, 0)));
		GridBagConstraints gbc_panel_9 = new GridBagConstraints();
		gbc_panel_9.gridwidth = 3;
		gbc_panel_9.insets = new Insets(0, 0, 5, 0);
		gbc_panel_9.fill = GridBagConstraints.BOTH;
		gbc_panel_9.gridx = 0;
		gbc_panel_9.gridy = 0;
		panel_normal.add(panel_9, gbc_panel_9);
		GridBagLayout gbl_panel_9 = new GridBagLayout();
		gbl_panel_9.columnWidths = new int[] { 0, 0, 0, 20, 0 };
		gbl_panel_9.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panel_9.columnWeights = new double[] { 0.0, 0.0, 0.0, 1.0,
				Double.MIN_VALUE };
		gbl_panel_9.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 1.0,
				0.0, Double.MIN_VALUE };
		panel_9.setLayout(gbl_panel_9);

		JLabel lblNewLabel = new JLabel("Movement Type");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 1;
		gbc_lblNewLabel.gridy = 1;
		panel_9.add(lblNewLabel, gbc_lblNewLabel);

		comboBoxMovementType = new JComboBox<Trajectory.TrajectoryType>();
		comboBoxMovementType.addItem(Trajectory.TrajectoryType.CONSTRAINED_WAYPOINT);
		comboBoxMovementType.addItem(Trajectory.TrajectoryType.RANDOM_WAYPOINT);
		
		GridBagConstraints gbc_comboBoxMovementType = new GridBagConstraints();
		gbc_comboBoxMovementType.insets = new Insets(0, 0, 5, 0);
		gbc_comboBoxMovementType.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBoxMovementType.gridx = 3;
		gbc_comboBoxMovementType.gridy = 1;
		panel_9.add(comboBoxMovementType, gbc_comboBoxMovementType);

		JLabel lblRandomness = new JLabel("Randomness");
		GridBagConstraints gbc_lblRandomness = new GridBagConstraints();
		gbc_lblRandomness.anchor = GridBagConstraints.EAST;
		gbc_lblRandomness.insets = new Insets(0, 0, 5, 5);
		gbc_lblRandomness.gridx = 1;
		gbc_lblRandomness.gridy = 2;
		panel_9.add(lblRandomness, gbc_lblRandomness);

		textFieldRandomness = new JTextField();
		GridBagConstraints gbc_txtSafgsafg = new GridBagConstraints();
		gbc_txtSafgsafg.insets = new Insets(0, 0, 5, 0);
		gbc_txtSafgsafg.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtSafgsafg.gridx = 3;
		gbc_txtSafgsafg.gridy = 2;
		panel_9.add(textFieldRandomness, gbc_txtSafgsafg);
		textFieldRandomness.setColumns(10);

		buttonNormalModePath = new JButton("Choose Path");
		GridBagConstraints gbc_buttonNormalModePath = new GridBagConstraints();
		gbc_buttonNormalModePath.anchor = GridBagConstraints.EAST;
		gbc_buttonNormalModePath.insets = new Insets(0, 0, 5, 5);
		gbc_buttonNormalModePath.gridx = 1;
		gbc_buttonNormalModePath.gridy = 3;
		panel_9.add(buttonNormalModePath, gbc_buttonNormalModePath);

		labelNormalModePath = new JLabel("Not initialized");
		GridBagConstraints gbc_labelNormalModeFilePath = new GridBagConstraints();
		gbc_labelNormalModeFilePath.insets = new Insets(0, 0, 5, 0);
		gbc_labelNormalModeFilePath.gridx = 3;
		gbc_labelNormalModeFilePath.gridy = 3;
		panel_9.add(labelNormalModePath, gbc_labelNormalModeFilePath);

		normalModeSlider = new JSlider(15, 60, 30);
		normalModeSlider.setInverted(true);
		GridBagConstraints gbc_normalModeSlider = new GridBagConstraints();
		gbc_normalModeSlider.weightx = 1.0;
		gbc_normalModeSlider.fill = GridBagConstraints.BOTH;
		gbc_normalModeSlider.gridwidth = 4;
		gbc_normalModeSlider.gridx = 0;
		gbc_normalModeSlider.gridy = 6;
		panel_9.add(normalModeSlider, gbc_normalModeSlider);
		
		JPanel panel_10 = new JPanel();
		GridBagConstraints gbc_panel_10 = new GridBagConstraints();
		gbc_panel_10.anchor = GridBagConstraints.LINE_END;
		gbc_panel_10.gridwidth = 3;
		gbc_panel_10.insets = new Insets(0, 0, 5, 5);
		gbc_panel_10.fill = GridBagConstraints.BOTH;
		gbc_panel_10.gridx = 0;
		gbc_panel_10.gridy = 1;
		panel_normal.add(panel_10, gbc_panel_10);
		panel_5.setLayout(gbl_panel_51);
		
		buttonNormalModePlay = new RoundButton(new ImageIcon(getClass()
				.getClassLoader().getResource("images/play.png")));
		panel_10.add(buttonNormalModePlay);

		buttonNormalModeStop = new RoundButton(new ImageIcon(getClass()
				.getClassLoader().getResource("images/stop.png")));
		panel_10.add(buttonNormalModeStop);
		


		fromSlider = new JSlider(0, 1, 0);
		GridBagConstraints gbcFromSlider = new GridBagConstraints();
		gbcFromSlider.insets = new Insets(0, 0, 5, 0);
		gbcFromSlider.fill = GridBagConstraints.BOTH;
		gbcFromSlider.gridx = 0;
		gbcFromSlider.gridy = 0;
		gbcFromSlider.weightx = 1.0f;

		toSlider = new JSlider(0, 1, 0);
		GridBagConstraints gbcToSlider = new GridBagConstraints();
		gbcToSlider.insets = new Insets(0, 0, 5, 0);
		gbcToSlider.fill = GridBagConstraints.BOTH;
		gbcToSlider.gridx = 0;
		gbcToSlider.gridy = 1;
		gbcToSlider.weightx = 1.0f;

		panel_5.add(fromSlider, gbcFromSlider);
		panel_5.add(toSlider, gbcToSlider);

		JPanel panel_7 = new JPanel();
		GridBagConstraints gbc_panel_7 = new GridBagConstraints();
		gbc_panel_7.anchor = GridBagConstraints.EAST;
		gbc_panel_7.insets = new Insets(0, 0, 5, 0);
		gbc_panel_7.fill = GridBagConstraints.VERTICAL;
		gbc_panel_7.gridx = 0;
		gbc_panel_7.gridy = 2;
		panel_5.add(panel_7, gbc_panel_7);

		btnSaveTraces = new JButton("Save modified traces");
		panel_7.add(btnSaveTraces);

		btnCreateMininetTopology = new JButton("Create Mininet Topology");
		panel_7.add(btnCreateMininetTopology);

		JPanel panel_1 = new JPanel();
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.insets = new Insets(0, 0, 5, 0);
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 1;
		gbc_panel_1.gridy = 0;
		frame.getContentPane().add(panel_1, gbc_panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[] { 0 };
		gbl_panel_1.rowHeights = new int[] { 0 };
		gbl_panel_1.columnWeights = new double[] { Double.MIN_VALUE };
		gbl_panel_1.rowWeights = new double[] { Double.MIN_VALUE };
		panel_1.setLayout(gbl_panel_1);

		panel_map = new JPanel();
		panel_map.setBorder(new TitledBorder(null, "MapView",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_panel_map = new GridBagConstraints();
		gbc_panel_map.insets = new Insets(0, 0, 5, 0);
		gbc_panel_map.fill = GridBagConstraints.BOTH;
		gbc_panel_map.gridx = 1;
		gbc_panel_map.gridy = 2;
		frame.getContentPane().add(panel_map, gbc_panel_map);
		panel_map.setLayout(new BorderLayout(0, 0));

		// addMapView("resources/coords.csv");
	}

	public JTabbedPane getTabbedPane() {
		return tabbedPane;
	}

	public JComboBox<Country> getComboBox_Country() {
		return comboBox_Country;
	}

	public JButton getButtonTraceModeStopTrajectory() {
		return buttonTraceModeStopTrajectory;
	}



	public JButton getBtnVisualizeTopology() {
		return btnVisualizeTopology;
	}

	public void setBtnVisualizeTopology(JButton btnVisualizeTopology) {
		this.btnVisualizeTopology = btnVisualizeTopology;
	}

	class RoundButton extends JButton {
		/**
     * 
     */
    private static final long serialVersionUID = 4611298520605489046L;

    public RoundButton() {
			this(null, null);
		}

		public RoundButton(Icon icon) {
			this(null, icon);
		}

		public RoundButton(String text) {
			this(text, null);
		}

		public RoundButton(Action a) {
			this();
			setAction(a);
		}

		public RoundButton(String text, Icon icon) {
			setModel(new DefaultButtonModel());
			init(text, icon);
			if (icon == null) {
				return;
			}
			setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
			setBackground(Color.BLACK);
			setContentAreaFilled(false);
			setFocusPainted(false);
			// setVerticalAlignment(SwingConstants.TOP);
			setAlignmentY(Component.TOP_ALIGNMENT);
			initShape();
		}

		protected Shape shape, base;

		protected void initShape() {
			if (!getBounds().equals(base)) {
				Dimension s = getPreferredSize();
				base = getBounds();
				shape = new Ellipse2D.Float(0, 0, s.width - 1, s.height - 1);
			}
		}

		public Dimension getPreferredSize() {
			Icon icon = getIcon();
			Insets i = getInsets();
			int iw = Math.max(icon.getIconWidth(), icon.getIconHeight());
			return new Dimension(iw + i.right + i.left, iw + i.top + i.bottom);
		}

		protected void paintBorder(Graphics g) {
			initShape();
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(getBackground());
			// g2.setStroke(new BasicStroke (1f));
			g2.draw(shape);
			g2.dispose();
		}

		public boolean contains(int x, int y) {
			initShape();
			return shape.contains(x, y);
			// Following, example of a case in which the transparent color is to
			// click No 0
			// Or return super.contains (x, y) && ((image.getRGB (x, y) >> 24) &
			// 0xff)> 0;
		}
	}

	public void updateLearnerStatus(String status) {
		label_learnerStatus.setText(status);
	}

	public JButton getBtnCreateMininetTopology() {
		return btnCreateMininetTopology;
	}

	public JButton getBtnPlotRealMovement() {
		return btnPlotRealMovement;
	}

	public JButton getBtnPlotPredictedMovement() {
		return btnPlotPredictedMovement;
	}

	public JButton getButton_browse_csv() {
		return button_browse_csv;
	}

	public JButton getButtonTeachCsv() {
		return button_teach_csv;
	}

	public JButton getBtnSaveTraces() {
		return btnSaveTraces;
	}

	public JSlider getFromSlider() {
		return fromSlider;
	}

	public JSlider getSpeedSlider() {
		return speedSlider;
	}

	public JSlider getToSlider() {
		return toSlider;
	}

	public void setButton_browse_csv(JButton button_browse_csv) {
		this.button_browse_csv = button_browse_csv;
	}

	public JToggleButton getToggle_button_switch_mode() {
		return toggle_button_switch_mode;
	}

	public void setToggle_button_switch_mode(
			JToggleButton toggle_button_switch_mode) {
		this.toggle_button_switch_mode = toggle_button_switch_mode;
	}

	public JButton getButtonTraceModePlayTrajectory() {
		return buttonTraceModePlayTrajectory;
	}



	public JLabel getLabel_csvTraceStatus() {
		return label_csvTraceStatus;
	}

	public void setLabel_csvTraceStatus(JLabel label_csvTraceStatus) {
		this.label_csvTraceStatus = label_csvTraceStatus;
	}

	public JPanel getPanel_map() {
		return panel_map;
	}

	public void setPanel_map(JPanel panel_map) {
		this.panel_map = panel_map;
	}

	public JLabel getLabel_totalMovementCounterValue() {
		return label_totalMovementCounterValue;
	}

	public void setLabel_totalMovementCounterValue(
			JLabel label_totalMovementCounterValue) {
		this.label_totalMovementCounterValue = label_totalMovementCounterValue;
	}

	public JLabel getLabel_accuracyValue() {
		return label_accuracyValue;
	}

	public void setLabel_accuracyValue(JLabel label_accuracyValue) {
		this.label_accuracyValue = label_accuracyValue;
	}

	public JLabel getLabel_falsePredictionCounterValue() {
		return label_falsePredictionCounterValue;
	}

	public void setLabel_falsePredictionCounterValue(
			JLabel label_falsePredictionCounterValue) {
		this.label_falsePredictionCounterValue = label_falsePredictionCounterValue;
	}

	public JLabel getLabel_truePredictionCounterValue() {
		return label_truePredictionCounterValue;
	}

	public void setLabel_truePredictionCounterValue(
			JLabel label_truePredictionCounterValue) {
		this.label_truePredictionCounterValue = label_truePredictionCounterValue;
	}
	
	public JButton getButtonNormalModeStop() {
		return buttonNormalModeStop;
	}

	public JButton getButtonNormalModePlay() {
		return buttonNormalModePlay;
	}

	public JButton getButtonNormalModePath() {
		return buttonNormalModePath;
	}

	public JComboBox<Trajectory.TrajectoryType> getComboBoxMovementType() {
		return comboBoxMovementType;
	}

  public JComboBox<LearningModel> getComboBoxAlgorithm() {
    return comboBoxAlgorithm;
  }

  public JTextField getTextFieldRandomness() {
    return textFieldRandomness;
}

public JLabel getLabelNormalModePath() {
    return labelNormalModePath;
}

public JSlider getNormalModeSlider() {
    return normalModeSlider;
}

}