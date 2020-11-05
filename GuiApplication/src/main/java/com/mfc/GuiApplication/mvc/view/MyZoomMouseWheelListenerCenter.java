package com.mfc.GuiApplication.mvc.view;


import org.jxmapviewer.JXMapViewer;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

/**
 * zooms using the mouse wheel on the view center
 *
 * @author joshy
 */
public class MyZoomMouseWheelListenerCenter implements MouseWheelListener {
    private JXMapViewer viewer;

    /**
     * @param viewer the jxmapviewer
     */
    public MyZoomMouseWheelListenerCenter(JXMapViewer viewer) {
        this.viewer = viewer;
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        int i = viewer.getZoom() + e.getWheelRotation() / 2;
        viewer.setZoom(i);
    }
}