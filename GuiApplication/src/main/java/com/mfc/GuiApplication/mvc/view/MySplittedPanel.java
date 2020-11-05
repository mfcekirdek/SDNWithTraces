package com.mfc.GuiApplication.mvc.view;

import javax.swing.*;
import java.awt.*;

public class MySplittedPanel extends JPanel {

    private JPanel panel_left;
    private JPanel panel_right;
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    // /**
    // * Launch the application.
    // */
    // public static void main(String[] args) {
    // EventQueue.invokeLater(new Runnable() {
    // public void run() {
    // try {
    // MySplittedPanel window = new MySplittedPanel();
    // JFrame frame = new JFrame();
    // frame.setBounds(100, 100, 450, 300);
    //
    // frame.add(window);
    // frame.setVisible(true);
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // }
    // });
    // }

    /**
     * Create the application.
     */
    public MySplittedPanel() {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        this.setBounds(100, 100, 450, 300);
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{0, 0};
        gridBagLayout.rowHeights = new int[]{0, 0};
        gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
        this.setLayout(gridBagLayout);

        JSplitPane splitPane = new JSplitPane();
        GridBagConstraints gbc_splitPane = new GridBagConstraints();
        gbc_splitPane.fill = GridBagConstraints.BOTH;
        gbc_splitPane.gridx = 0;
        gbc_splitPane.gridy = 0;
        this.add(splitPane, gbc_splitPane);

        splitPane.setResizeWeight(0.5);

        panel_left = new JPanel();
        splitPane.setLeftComponent(panel_left);
        panel_left.setLayout(new BorderLayout(0, 0));

        panel_right = new JPanel();
        splitPane.setRightComponent(panel_right);
        panel_right.setLayout(new BorderLayout(0, 0));

    }

    public JPanel getPanel_left() {
        return panel_left;
    }

    public void setPanel_left(JPanel panel_left) {
        this.panel_left = panel_left;
    }

    public JPanel getPanel_right() {
        return panel_right;
    }

    public void setPanel_right(JPanel panel_right) {
        this.panel_right = panel_right;
    }
}
