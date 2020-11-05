package com.mfc.GuiApplication.mvc.view;


import com.mfc.GuiApplication.topology.Topology;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.VisualizationViewer;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TopologyPopClickListener extends MouseAdapter {

    Topology t;

    public TopologyPopClickListener(Topology t) {
        this.t = t;
    }

    public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) doPop(e);
    }

    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) doPop(e);
    }

    private void doPop(MouseEvent e) {
        JPopupMenu menu;
        JMenuItem item;

        VisualizationViewer<Integer, Integer> vv = (VisualizationViewer<Integer, Integer>) e.getSource();
        Layout<Integer, Integer> layout = vv.getGraphLayout();
        int x = e.getX();
        int y = e.getY();

        try {
            final int vertex = (Integer) vv.getPickSupport().getVertex(layout, x, y);

            menu = new JPopupMenu();
            if (vertex == -1) item = new JMenuItem("User Details");
            else item = new JMenuItem("Cell Details");

            menu.add(item);

            item.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    if (vertex != -1) {
                        System.err.println(vertex);
                        System.out.println(t.getUniqueCellTowers().get(vertex));
                        System.out.println(t.getEdgeMap().get(vertex).keySet());
                        System.out.println(t.getEdgeMap().get(vertex).values());
                    } else {
                        System.err.println(vertex);
                        System.out.println(t.getUser().getConnectedCellTower());
                        System.out.println(t.getUser().getUserEdge());
                    }
                }
            });
            menu.show(e.getComponent(), e.getX(), e.getY());

        } catch (Exception ex) {
            // System.out.println(ex);
            menu = null;
            item = null;
        }

    }
}
