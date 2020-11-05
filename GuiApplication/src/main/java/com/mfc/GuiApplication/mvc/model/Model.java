package com.mfc.GuiApplication.mvc.model;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import javax.swing.event.MouseInputListener;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.VirtualEarthTileFactoryInfo;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCenter;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.LocalResponseCache;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.jxmapviewer.viewer.WaypointPainter;

import com.mfc.GuiApplication.entity.CellTower;
import com.mfc.GuiApplication.entity.Trace;
import com.mfc.GuiApplication.enums.Country;
import com.mfc.GuiApplication.enums.TimeSegment;
import com.mfc.GuiApplication.helpers.FancyWaypointRenderer;
import com.mfc.GuiApplication.helpers.MergedRoutePainter;
import com.mfc.GuiApplication.helpers.MyWaypoint;
import com.mfc.GuiApplication.helpers.RoutePainter;

public class Model {
    public Model() {
        super();
        init();
    }

    private JXMapViewer mapViewer;
    private String csvTraceStatus = "Not Loaded";

    private ArrayList<GeoPosition> geoPositionList;
    private ArrayList<MyWaypoint> myWaypointList;
    private TileFactoryInfo info;
    private DefaultTileFactory tileFactory;
    GeoPosition currentGeoPosition = null;
    
    

    private void resetModel() {
        geoPositionList = new ArrayList<GeoPosition>();
        myWaypointList = new ArrayList<MyWaypoint>();
        info = new VirtualEarthTileFactoryInfo(VirtualEarthTileFactoryInfo.MAP);
        tileFactory = new DefaultTileFactory(info);
        tileFactory.setThreadPoolSize(8);

        // Setup local file cache
        File cacheDir = new File(System.getProperty("user.home") + File.separator + ".jxmapviewer2");
        LocalResponseCache.installResponseCache(info.getBaseURL(), cacheDir, false);

        mapViewer = new JXMapViewer();
        mapViewer.setTileFactory(tileFactory);

    }

    private void init() {
        resetModel();
    }

    public void setFocusAndInteractionsOfMapView() {
        // Set the focus
        mapViewer.setZoom(10);
        
        if(geoPositionList.size() > 0)
        	mapViewer.setAddressLocation(geoPositionList.get(geoPositionList.size() - 1));
        else{
        	GeoPosition prague = new GeoPosition(50.102976, 14.399171);
    		mapViewer.setAddressLocation(prague);
        }
        	
        // Add interactions
        MouseInputListener mia = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(mia);
        mapViewer.addMouseMotionListener(mia);
        mapViewer.addMouseListener(new CenterMapListener(mapViewer));
        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCenter(mapViewer));
        mapViewer.addKeyListener(new PanKeyListener(mapViewer));
    }

    public void updateMapViewWithASingleTrace(int cid, double lat, double lon, Color c) {
        // Get real gps positions of cell towers

        GeoPosition temp = new GeoPosition(lat, lon);
        if (!geoPositionList.contains(temp)) {

            geoPositionList.add(temp);
            myWaypointList.add(new MyWaypoint("Cell Tower " + cid, c, temp));

            // setFocusAndInteractionsOfMapView();
            Set<MyWaypoint> waypoints = new HashSet<MyWaypoint>(myWaypointList);
            // Create a waypoint painter that takes all the waypoints
            WaypointPainter<MyWaypoint> waypointPainter = new WaypointPainter<MyWaypoint>();
            waypointPainter.setWaypoints(waypoints);
            waypointPainter.setRenderer(new FancyWaypointRenderer());

            mapViewer.setOverlayPainter(waypointPainter);
        }
    }

    public void updateMapViewWithASingleTraceWithRouting(CellTower trueCellTower,CellTower predictedCellTower ,Color c) {
    	
    	if(geoPositionList.size() == 0) {
    		updateMapViewWithASingleTrace(trueCellTower.getCid(), trueCellTower.getLat(), trueCellTower.getLon(), Color.BLACK);
    		currentGeoPosition = new GeoPosition(trueCellTower.getLat(), trueCellTower.getLon());
    		return;
    	}
    	
    	CellTower cellTower = null;
    	
    	if(c.equals(Color.BLACK))
    		cellTower = trueCellTower;
    	else if(c.equals(Color.RED) || c.equals(Color.GREEN))
    		cellTower = predictedCellTower;
    		
        GeoPosition prevGeoPosition = currentGeoPosition;
        GeoPosition nextGeoPosition = new GeoPosition(cellTower.getLat(), cellTower.getLon());
        currentGeoPosition = new GeoPosition(trueCellTower.getLat(),trueCellTower.getLon());

        int index = findGeoPositionIndexByLatLon(geoPositionList, cellTower.getLat(), cellTower.getLon());

        if (index == -1) {
            geoPositionList.add(nextGeoPosition);
            myWaypointList.add(new MyWaypoint("Cell Tower " + cellTower.getCid(), Color.BLACK, nextGeoPosition));
        }
        
        int index2 = findGeoPositionIndexByLatLon(geoPositionList,currentGeoPosition.getLatitude(),currentGeoPosition.getLongitude());
        
        if (index2 == -1) {
            geoPositionList.add(currentGeoPosition);
            myWaypointList.add(new MyWaypoint("Cell Tower " + trueCellTower.getCid(), Color.BLACK, currentGeoPosition));
        }
        
        // setFocusAndInteractionsOfMapView();

        Set<MyWaypoint> waypoints = new HashSet<MyWaypoint>(myWaypointList);

        List<Painter<JXMapViewer>> painters = new ArrayList<Painter<JXMapViewer>>();

        List<GeoPosition> route_track;

        if (prevGeoPosition != null) {

            route_track = Arrays.asList(prevGeoPosition, nextGeoPosition);            
            RoutePainter routePainter = new RoutePainter(route_track, c);       
            painters.add(0,routePainter);
        }

        // Create a waypoint painter that takes all the waypoints
        WaypointPainter<MyWaypoint> waypointPainter = new WaypointPainter<MyWaypoint>();
        waypointPainter.setWaypoints(waypoints);
        waypointPainter.setRenderer(new FancyWaypointRenderer());
        painters.add(1,waypointPainter);

        CompoundPainter<JXMapViewer> painter = new CompoundPainter<JXMapViewer>(painters);
        mapViewer.setOverlayPainter(painter);     
    }

    public JXMapViewer getMapViewer() {
        return mapViewer;
    }

    public void setMapViewer(JXMapViewer mapViewer) {
        this.mapViewer = mapViewer;
    }

    public static List<CellTower> getCellTowersCollectively(String csv_file_name,Country country) throws FileNotFoundException {

        ArrayList<CellTower> cellTowers = new ArrayList<CellTower>();
        HashMap<Integer, CellTower> hmap = new HashMap<Integer, CellTower>();

        Scanner scanner = new Scanner(new File(csv_file_name));
        scanner.useDelimiter(",");

        while (scanner.hasNextLine()) {
            String temp = scanner.nextLine();
            if (temp.charAt(0) != 'Y') {

                String[] line = temp.split(",");
                int mcc = Integer.parseInt(line[2]);
                
            	if(country.equals(Country.ALL) || country.getValue() == mcc) {
	                int cid = Integer.parseInt(line[5]);
	                float temp_lat = Float.parseFloat(line[6]);
	                float temp_lon = Float.parseFloat(line[7]);
	                CellTower c = new CellTower(cid, temp_lat, temp_lon);
	                hmap.put(c.getCid(), c);
            	}
            }
        }

        scanner.close();
        cellTowers = new ArrayList<CellTower>(hmap.values());
        return cellTowers;
    }

    private static void cellTowerListToString(List<CellTower> cellTowers) {

        for (int i = 0; i < cellTowers.size(); i++) {
            System.out.println(cellTowers.get(i));
        }
    }

    public String getCsvTraceStatus() {
        return csvTraceStatus;
    }

    public void setCsvTraceStatus(String csvTraceStatus) {
        this.csvTraceStatus = csvTraceStatus;
    }

    public void updateMapViewWithTracesCollectively(String csvFileName,Country country) {
        // Get real gps positions of cell towers
        List<CellTower> cellTowers;
        try {
            cellTowers = getCellTowersCollectively(csvFileName,country);
            cellTowerListToString(cellTowers);

            geoPositionList = new ArrayList<GeoPosition>();
            myWaypointList = new ArrayList<MyWaypoint>();

            // Create a TileFactoryInfo for Virtual Earth
            info = new VirtualEarthTileFactoryInfo(VirtualEarthTileFactoryInfo.MAP);
            tileFactory = new DefaultTileFactory(info);
            tileFactory.setThreadPoolSize(8);

            // Setup local file cache
            File cacheDir = new File(System.getProperty("user.home") + File.separator + ".jxmapviewer2");
            LocalResponseCache.installResponseCache(info.getBaseURL(), cacheDir, false);

            // Setup JXMapViewer
            mapViewer = new JXMapViewer();
            mapViewer.setTileFactory(tileFactory);

            //
            for (int i = 0; i < cellTowers.size(); i++) {
                geoPositionList.add(new GeoPosition(cellTowers.get(i).getLat(), cellTowers.get(i).getLon()));
                myWaypointList.add(new MyWaypoint("X", Color.BLACK, geoPositionList.get(i)));
            }

            // Set the focus
            mapViewer.setZoom(10);
            mapViewer.setAddressLocation(geoPositionList.get(0));

            setFocusAndInteractionsOfMapView();
            Set<MyWaypoint> waypoints = new HashSet<MyWaypoint>(myWaypointList);

            // Create a waypoint painter that takes all the waypoints
            WaypointPainter<MyWaypoint> waypointPainter = new WaypointPainter<MyWaypoint>();
            waypointPainter.setWaypoints(waypoints);
            waypointPainter.setRenderer(new FancyWaypointRenderer());
            mapViewer.setOverlayPainter(waypointPainter);

        } catch (FileNotFoundException e) {
            System.err.println("File not found !!!");
            e.printStackTrace();
        }
    }

    public static List<Trace> getTraces(String csv_file_name,Country country) throws FileNotFoundException {

        ArrayList<Trace> traces = new ArrayList<Trace>();

        Scanner scanner = new Scanner(new File(csv_file_name));
        scanner.useDelimiter(",");

        while (scanner.hasNextLine()) {
            String temp = scanner.nextLine();
            if (temp.charAt(0) != 'Y') {
            	
        	    String[] line = temp.split(",");
                int mcc = Integer.parseInt(line[2]);
                
            	if(country.equals(Country.ALL)|| country.getValue() == mcc) {
                    int cid = Integer.parseInt(line[5]);
                    double temp_lat = Double.parseDouble(line[6]);
                    double temp_lon = Double.parseDouble(line[7]);
                    String time_segment = line[11];
                    CellTower c = new CellTower(cid, temp_lat, temp_lon);
                    Trace t = new Trace(c, TimeSegment.fromString(time_segment),
                            Long.parseLong(line[0]), Long.parseLong(line[1]),
                            Integer.parseInt(line[2]), Integer.parseInt(line[3]), Integer.parseInt(line[4]),
                            Integer.parseInt(line[8]), Integer.parseInt(line[9]), Boolean.parseBoolean(line[10]));
                    traces.add(t);             
            	}            	
            }
        }
        scanner.close();
        return traces;
    }
    


    private static int findGeoPositionIndexByLatLon(List<GeoPosition> geoList, double lat, double lon) {
        GeoPosition g = new GeoPosition(lat, lon);
        for (int i = 0; i < geoList.size(); i++) {
            if (geoList.get(i).equals(g)) {
                return i;
            }
        }

        return -1;
    }
    
    
    public void updateMapViewWithASingleTraceWithRoutingMerged(CellTower trueCellTower,CellTower predictedCellTower ,Color c) {
    	

    	if(geoPositionList.size() == 0) {
    		updateMapViewWithASingleTrace(trueCellTower.getCid(), trueCellTower.getLat(), trueCellTower.getLon(), Color.BLACK);
    		currentGeoPosition = new GeoPosition(trueCellTower.getLat(), trueCellTower.getLon());
    		return;
    	}

    	
        GeoPosition prevGeoPosition = currentGeoPosition;
        GeoPosition nextRealGeoPosition = new GeoPosition(trueCellTower.getLat(), trueCellTower.getLon());
        GeoPosition nextPredictedGeoPosition = new GeoPosition(predictedCellTower.getLat(), predictedCellTower.getLon());
        currentGeoPosition = new GeoPosition(trueCellTower.getLat(),trueCellTower.getLon());

        
        
        int indexCurrentPosition = findGeoPositionIndexByLatLon(geoPositionList,currentGeoPosition.getLatitude(),currentGeoPosition.getLongitude());
        
        if (indexCurrentPosition == -1) {
            geoPositionList.add(currentGeoPosition);
            myWaypointList.add(new MyWaypoint("Cell Tower " + trueCellTower.getCid(), Color.BLACK, currentGeoPosition));
        }
            
        int indexNextRealPosition = findGeoPositionIndexByLatLon(geoPositionList, nextRealGeoPosition.getLatitude(), nextRealGeoPosition.getLongitude());

        if (indexNextRealPosition == -1) {
            geoPositionList.add(nextRealGeoPosition);
            myWaypointList.add(new MyWaypoint("Cell Tower " + trueCellTower.getCid(), Color.BLACK, nextRealGeoPosition));
        }

        int indexNextPredictedPosition = findGeoPositionIndexByLatLon(geoPositionList, nextPredictedGeoPosition.getLatitude(), nextPredictedGeoPosition.getLongitude());

        if (indexNextPredictedPosition == -1) {
            geoPositionList.add(nextPredictedGeoPosition);
            myWaypointList.add(new MyWaypoint("Cell Tower " + predictedCellTower.getCid(), Color.BLACK, nextPredictedGeoPosition));
        }
        
        
        
        
        
        // setFocusAndInteractionsOfMapView();

        Set<MyWaypoint> waypoints = new HashSet<MyWaypoint>(myWaypointList);

        List<Painter<JXMapViewer>> painters = new ArrayList<Painter<JXMapViewer>>();

        List<GeoPosition> route_track_real;
        List<GeoPosition> route_track_predicted;

        if (prevGeoPosition != null) {

            route_track_real = Arrays.asList(prevGeoPosition, nextRealGeoPosition);
            route_track_predicted = Arrays.asList(prevGeoPosition, nextPredictedGeoPosition);

            
            MergedRoutePainter mergedRoutePainter = new MergedRoutePainter(route_track_real,route_track_predicted, c);
            painters.add(0,mergedRoutePainter);
        }

        // Create a waypoint painter that takes all the waypoints
        WaypointPainter<MyWaypoint> waypointPainter = new WaypointPainter<MyWaypoint>();
        waypointPainter.setWaypoints(waypoints);
        waypointPainter.setRenderer(new FancyWaypointRenderer());
        painters.add(1,waypointPainter);

        CompoundPainter<JXMapViewer> painter = new CompoundPainter<JXMapViewer>(painters);
        mapViewer.setOverlayPainter(painter);
        

        // mapViewer.setOverlayPainter(waypointPainter);

    }
}