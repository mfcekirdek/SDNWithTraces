package com.mfc.GuiApplication.helpers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;

public class RoutePainter implements Painter<JXMapViewer> {
	private Color color = Color.RED;
	private boolean antiAlias = true;
	private List<GeoPosition> track;
	private double phi;
	private int barb;

	/**
	 * @param track
	 *            the track
	 */
	public RoutePainter(List<GeoPosition> track, Color c) {
		// copy the list so that changes in the
		// original list do not have an effect here
		this.track = new ArrayList<GeoPosition>(track);
		this.color = c;
		this.phi = Math.toRadians(40);
		this.barb = 14;
	}

	public List<GeoPosition> getTrack() {
		return track;
	}

	public void setTrack(List<GeoPosition> track) {
		this.track = track;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
		g = (Graphics2D) g.create();

		// convert from viewport to world bitmap
		Rectangle rect = map.getViewportBounds();
		g.translate(-rect.x, -rect.y);

		if (antiAlias)
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);

		// do the drawing
		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(4));

		drawRoute(g, map);

		// do the drawing again
		g.setColor(color);
		g.setStroke(new BasicStroke(2));

		drawRoute(g, map);

		g.dispose();
	}

	/**
	 * @param g
	 *            the graphics object
	 * @param map
	 *            the map
	 */
	private void drawRoute(Graphics2D g, JXMapViewer map) {

		int lastX = 0;
		int lastY = 0;

		boolean first = true;

		for (GeoPosition gp : track) {
			// convert geo-coordinate to world bitmap pixel
			Point2D pt = map.getTileFactory().geoToPixel(gp, map.getZoom());

			if (first) {
				first = false;
			} else {
				g.drawLine(lastX, lastY, (int) pt.getX(), (int) pt.getY());
				// drawArrow(g,lastX, lastY, (int) pt.getX(), (int) pt.getY());
				drawArrowHead(g, new Point(lastX, lastY),
						new Point((int) pt.getX(), (int) pt.getY()), color);

			}

			lastX = (int) pt.getX();
			lastY = (int) pt.getY();
		}
	}

	private void drawArrowHead(Graphics2D g2, Point tail, Point tip, Color color) {
		g2.setPaint(color);
		double dy = tip.y - tail.y;
		double dx = tip.x - tail.x;
		double theta = Math.atan2(dy, dx);
		// System.out.println("theta = " + Math.toDegrees(theta));
		double x, y, rho = theta + phi;
		for (int j = 0; j < 2; j++) {
			x = tip.x - barb * Math.cos(rho);
			y = tip.y - barb * Math.sin(rho);
			g2.draw(new Line2D.Double(tip.x, tip.y, x, y));
			rho = theta - phi;
		}
	}


}
