package com.mfc.GuiApplication.topology;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import com.google.common.base.Function;
import com.mfc.GuiApplication.entity.CellTower;
import com.mfc.GuiApplication.entity.Trace;
import com.mfc.GuiApplication.entity.User;
import com.mfc.GuiApplication.mvc.view.TopologyPopClickListener;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.util.RandomLocationTransformer;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.LayeredIcon;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.EllipseVertexShapeTransformer;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintTransformer;
import edu.uci.ics.jung.visualization.decorators.PickableVertexPaintTransformer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.decorators.VertexIconShapeTransformer;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.BasicVertexRenderer;
import edu.uci.ics.jung.visualization.renderers.Checkmark;
import edu.uci.ics.jung.visualization.renderers.DefaultEdgeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.DefaultVertexLabelRenderer;
import edu.uci.ics.jung.visualization.util.ImageShapeUtils;


public class Topology extends JApplet {


    /**
     *
     */
    private static final int USER_VERTEX_NUMBER = -1;
    private static final String CELL_TOWER_ICON_FILE_NAME = "images/rsz_cell_tower.png";
    private static final String USER_ICON_FILE_NAME = "images/rsz_mobil_user.png";

    private static final long serialVersionUID = -4332663871914930864L;

    //	private static final int VERTEX_COUNT=11;
    private int vertexCount;
    //	private int edgeCount;

    private List<CellTower> cellTowerTraces;
    private HashMap<Integer, CellTower> uniqueCellTowers;
    private HashMap<Integer, HashMap<Integer, Integer>> edgeMap = new HashMap<Integer, HashMap<Integer, Integer>>();
    private User user;

    // <s1, <s2,5>> -> bu s1 ile s2 arasindaki edge no 5'tir demek. Icteki hashmapin value'si edgeno.
    /**
     * the graph
     */
    UndirectedSparseGraph<Number, Number> graph;

    /**
     * the visual component and renderer for the graph
     */
    VisualizationViewer<Number, Number> vv;

    /**
     * some icon names to use
     */


    /**
     * some icon names to use
     */


    public Topology(List<Trace> traceList) throws FileNotFoundException {

        // Creating graph for smaller dataset..

        cellTowerTraces = getCellTowerTraces(traceList);
        setUniqueCellTowers(findUniqueCellTowers(cellTowerTraces));

        vertexCount = getUniqueCellTowers().size();

        // create a simple graph for the demo
        graph = new UndirectedSparseGraph<Number, Number>();
        createGraph(cellTowerTraces);
        addUser(1);
        System.out.println(user.getUserEdge());
        System.out.println(user.getConnectedCellTower());
        moveUser(2);
        moveUser(1);

        Map<Number, String> map = new HashMap<Number, String>();
        Map<Number, Icon> iconMap = new HashMap<Number, Icon>();

        BufferedImage mapmodifiableUser = null;
        BufferedImage mapmodifiableCellTower = null;

        try {
            mapmodifiableUser = ImageIO.read(getClass().getClassLoader().getResource(USER_ICON_FILE_NAME));
            mapmodifiableCellTower = ImageIO.read(getClass().getClassLoader().getResource(CELL_TOWER_ICON_FILE_NAME));
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        Icon iconUser = new LayeredIcon(mapmodifiableUser);
        iconMap.put(USER_VERTEX_NUMBER, iconUser);
        map.put(USER_VERTEX_NUMBER, "USER");

        Iterator it = uniqueCellTowers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            //          map.put(i, "Cell Tower:" + cellTowerTraces.get(i).toString());
            map.put((Integer) pair.getKey(), "");
            Icon icon = new LayeredIcon(mapmodifiableCellTower);
            iconMap.put((Integer) pair.getKey(), icon);

        }

        CircleLayout<Number, Number> layout = new CircleLayout<Number, Number>(graph);
//        layout.setMaxIterations(100);
        layout.setInitializer(new RandomLocationTransformer<Number>(new Dimension(1000, 900), 0));
        vv = new VisualizationViewer<Number, Number>(layout, new Dimension(1000, 900));

        // This demo uses a special renderer to turn outlines on and off.
        // you do not need to do this in a real application.
        // Instead, just let vv use the Renderer it already has
        vv.getRenderer().setVertexRenderer(new DemoRenderer<Number, Number>());

        Function<Number, Paint> vpf =
                new PickableVertexPaintTransformer<Number>(vv.getPickedVertexState(), Color.white, Color.yellow);
        vv.getRenderContext().setVertexFillPaintTransformer(vpf);
        vv.getRenderContext().setEdgeDrawPaintTransformer(
                new PickableEdgePaintTransformer<Number>(vv.getPickedEdgeState(), Color.black, Color.cyan));

        vv.setBackground(Color.white);

        final Function<Number, String> vertexStringerImpl = new VertexStringerImpl<Number, String>(map);
        vv.getRenderContext().setVertexLabelTransformer(vertexStringerImpl);
        vv.getRenderContext().setVertexLabelRenderer(new DefaultVertexLabelRenderer(Color.cyan));
        vv.getRenderContext().setEdgeLabelRenderer(new DefaultEdgeLabelRenderer(Color.cyan));
        //        vv.getRenderContext().setEdgeLabelTransformer(new Function<Number,String>() {
        //        	URL url = getClass().getResource("/images/lightning-s.gif");
        //			public String transform(Number input) {
        //
        //				return "<html><images src="+url+" height=10 width=21>"+input.toString();
        //			}});

        // For this demo only, I use a special class that lets me turn various
        // features on and off. For a real application, use VertexIconShapeTransformer instead.
        final DemoVertexIconShapeTransformer<Number> vertexIconShapeTransformer =
                new DemoVertexIconShapeTransformer<Number>(new EllipseVertexShapeTransformer<Number>());
        vertexIconShapeTransformer.setIconMap(iconMap);

        final DemoVertexIconTransformer<Number> vertexIconTransformer = new DemoVertexIconTransformer<Number>(iconMap);

        vv.getRenderContext().setVertexShapeTransformer(vertexIconShapeTransformer);
        vv.getRenderContext().setVertexIconTransformer(vertexIconTransformer);

        // un-comment for RStar Tree visual testing
        //vv.addPostRenderPaintable(new BoundingRectanglePaintable(vv.getRenderContext(), vv.getGraphLayout()));

        // Get the pickedState and add a listener that will decorate the
        // Vertex images with a checkmark icon when they are picked
        PickedState<Number> ps = vv.getPickedVertexState();
        ps.addItemListener(new PickWithIconListener<Number>(vertexIconTransformer, this));

        vv.addPostRenderPaintable(new VisualizationViewer.Paintable() {
            int x;
            int y;
            Font font;
            FontMetrics metrics;
            int swidth;
            int sheight;
            String str = "Thank You, random text!";

            public void paint(Graphics g) {
                Dimension d = vv.getSize();
                if (font == null) {
                    font = new Font(g.getFont().getName(), Font.BOLD, 20);
                    metrics = g.getFontMetrics(font);
                    swidth = metrics.stringWidth(str);
                    sheight = metrics.getMaxAscent() + metrics.getMaxDescent();
                    x = (d.width - swidth) / 2;
                    y = (int) (d.height - sheight * 1.5);
                }
                g.setFont(font);
                Color oldColor = g.getColor();
                g.setColor(Color.lightGray);
                g.drawString(str, x, y);
                g.setColor(oldColor);
            }

            public boolean useTransform() {
                return false;
            }
        });

        // add a listener for ToolTips
        vv.setVertexToolTipTransformer(new ToStringLabeller());

        Container content = getContentPane();
        final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
        content.add(panel);

        final DefaultModalGraphMouse<Number, Number> graphMouse = new DefaultModalGraphMouse<Number, Number>();
        vv.setGraphMouse(graphMouse);
        vv.addKeyListener(graphMouse.getModeKeyListener());

        vv.addMouseListener(new TopologyPopClickListener(this)); // ##############3

        final ScalingControl scaler = new CrossoverScalingControl();

        JButton plus = new JButton("+");
        plus.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scaler.scale(vv, 1.1f, vv.getCenter());
            }
        });
        JButton minus = new JButton("-");
        minus.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scaler.scale(vv, 1 / 1.1f, vv.getCenter());
            }
        });

        JCheckBox shape = new JCheckBox("Shape");
        shape.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                vertexIconShapeTransformer.setShapeImages(e.getStateChange() == ItemEvent.SELECTED);
                vv.repaint();
            }
        });
        shape.setSelected(true);

        JCheckBox fill = new JCheckBox("Fill");
        fill.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                vertexIconTransformer.setFillImages(e.getStateChange() == ItemEvent.SELECTED);
                vv.repaint();
            }
        });
        fill.setSelected(true);

        JCheckBox drawOutlines = new JCheckBox("Outline");
        drawOutlines.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                vertexIconTransformer.setOutlineImages(e.getStateChange() == ItemEvent.SELECTED);
                vv.repaint();
            }
        });

        JComboBox<?> modeBox = graphMouse.getModeComboBox();
        JPanel modePanel = new JPanel();
        modePanel.setBorder(BorderFactory.createTitledBorder("Mouse Mode"));
        modePanel.add(modeBox);

        JPanel scaleGrid = new JPanel(new GridLayout(1, 0));
        scaleGrid.setBorder(BorderFactory.createTitledBorder("Zoom"));
        JPanel labelFeatures = new JPanel(new GridLayout(1, 0));
        labelFeatures.setBorder(BorderFactory.createTitledBorder("Image Effects"));
        JPanel controls = new JPanel();
        scaleGrid.add(plus);
        scaleGrid.add(minus);
        controls.add(scaleGrid);
        labelFeatures.add(shape);
        labelFeatures.add(fill);
        labelFeatures.add(drawOutlines);

        controls.add(labelFeatures);
        controls.add(modePanel);
        content.add(controls, BorderLayout.SOUTH);
    }

    /**
     * When Vertices are picked, add a checkmark icon to the imager.
     * Remove the icon when a Vertex is unpicked
     *
     * @author Tom Nelson
     */
    public static class PickWithIconListener<V> implements ItemListener {
        Function<V, Icon> imager;
        Icon checked;
        Topology t;

        public PickWithIconListener(Function<V, Icon> imager, Topology t) {
            this.imager = imager;
            this.t = t;
            checked = new Checkmark();
        }

        public void itemStateChanged(ItemEvent e) {
            @SuppressWarnings("unchecked") Icon icon = imager.apply((V) e.getItem());
            if (icon != null && icon instanceof LayeredIcon) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    ((LayeredIcon) icon).add(checked);
                    //                    System.err.println("SECDUK");
                    //                    System.out.println((V)e.getItem());
                    //                    System.out.println(t.getUniqueCellTowers().get((Integer)e.getItem()));
                    //                    uniqueCellTowers(V)e.getItem()
                    //                    "Cell Tower:" + cellTowerTraces.get(i).toString()
                } else {
                    ((LayeredIcon) icon).remove(checked);
                }
            }
        }
    }

    /**
     * A simple implementation of VertexStringer that
     * gets Vertex labels from a Map
     *
     * @author Tom Nelson
     */
    public static class VertexStringerImpl<V, S> implements Function<V, String> {

        Map<V, String> map = new HashMap<V, String>();

        boolean enabled = true;

        public VertexStringerImpl(Map<V, String> map) {
            this.map = map;
        }

        /* (non-Javadoc)
         * @see edu.uci.ics.jung.graph.decorators.VertexStringer#getLabel(edu.uci.ics.jung.graph.Vertex)
         */
        public String apply(V v) {
            if (isEnabled()) {
                return map.get(v);
            } else {
                return "";
            }
        }

        /**
         * @return Returns the enabled.
         */
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * @param enabled The enabled to set.
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    /**
     * create some vertices
     *
     */
    private void createGraph(List<CellTower> cellTowerTraces) {

        for (int i = 0; i < cellTowerTraces.size(); i++) {

            int cid = cellTowerTraces.get(i).getCid();
            if (i != 0) {
                int previous = cellTowerTraces.get(i - 1).getCid();
                addSingleCellTower(cid, previous);
            }
        }

    }

    // sadece applete ekliyor, unique cell'e eklemiyor.

    private void addSingleCellTower(int cid, int previous) {
        if (!graph.containsVertex(cid)) {
            graph.addVertex(cid);
        }

        if (previous >= 0) {

            boolean isAdded = addEdgeToHashMap(previous, cid, graph.getEdgeCount() + 2);
            if (isAdded) {
                graph.addEdge(graph.getEdgeCount() + 2, cid, previous, EdgeType.UNDIRECTED);
            }

        }
    }

    //        graph.addEdge(j++, 0, 1, EdgeType.UNDIRECTED);
    //        graph.addEdge(j++, 3, 0, EdgeType.UNDIRECTED);


    private void addUser(int connectedCid) {
        if (!graph.containsVertex(USER_VERTEX_NUMBER)) {
            graph.addVertex(USER_VERTEX_NUMBER);
            CellTower c = uniqueCellTowers.get(connectedCid);
            user = new User(c, 1);
        }
        graph.addEdge(user.getUserEdge(), USER_VERTEX_NUMBER, user.getConnectedCellTower().getCid(),
                EdgeType.UNDIRECTED);

    }

    private void moveUser(int targetCid) {
        if (!graph.containsVertex(USER_VERTEX_NUMBER)) {
            System.err.println("User is not connected!!");
            return;
        }

        graph.removeEdge(user.getUserEdge());
        graph.addEdge(user.getUserEdge(), USER_VERTEX_NUMBER, targetCid, EdgeType.UNDIRECTED);
    }

    /**
     * This class exists only to provide settings to turn on/off shapes and image fill
     * in this demo.
     * <p>
     * <p>For a real application, just use {@code Functions.forMap(iconMap)} to provide a
     * {@code Function<V, Icon>}.
     */
    public static class DemoVertexIconTransformer<V> implements Function<V, Icon> {
        boolean fillImages = true;
        boolean outlineImages = false;
        Map<V, Icon> iconMap = new HashMap<V, Icon>();

        public DemoVertexIconTransformer(Map<V, Icon> iconMap) {
            this.iconMap = iconMap;
        }

        /**
         * @return Returns the fillImages.
         */
        public boolean isFillImages() {
            return fillImages;
        }

        /**
         * @param fillImages The fillImages to set.
         */
        public void setFillImages(boolean fillImages) {
            this.fillImages = fillImages;
        }

        public boolean isOutlineImages() {
            return outlineImages;
        }

        public void setOutlineImages(boolean outlineImages) {
            this.outlineImages = outlineImages;
        }

        public Icon apply(V v) {
            if (fillImages) {
                return (Icon) iconMap.get(v);
            } else {
                return null;
            }
        }
    }

    /**
     * this class exists only to provide settings to turn on/off shapes and image fill
     * in this demo.
     * In a real application, use VertexIconShapeTransformer instead.
     */
    public static class DemoVertexIconShapeTransformer<V> extends VertexIconShapeTransformer<V> {

        boolean shapeImages = true;

        public DemoVertexIconShapeTransformer(Function<V, Shape> delegate) {
            super(delegate);
        }

        /**
         * @return Returns the shapeImages.
         */
        public boolean isShapeImages() {
            return shapeImages;
        }

        /**
         * @param shapeImages The shapeImages to set.
         */
        public void setShapeImages(boolean shapeImages) {
            shapeMap.clear();
            this.shapeImages = shapeImages;
        }

        public Shape transform(V v) {
            Icon icon = (Icon) iconMap.get(v);

            if (icon != null && icon instanceof ImageIcon) {

                Image image = ((ImageIcon) icon).getImage();

                Shape shape = shapeMap.get(image);
                if (shape == null) {
                    if (shapeImages) {
                        shape = ImageShapeUtils.getShape(image, 30);
                    } else {
                        shape = new Rectangle2D.Float(0, 0, image.getWidth(null), image.getHeight(null));
                    }
                    if (shape.getBounds().getWidth() > 0 && shape.getBounds().getHeight() > 0) {
                        int width = image.getWidth(null);
                        int height = image.getHeight(null);
                        AffineTransform transform = AffineTransform.getTranslateInstance(-width / 2, -height / 2);
                        shape = transform.createTransformedShape(shape);
                        shapeMap.put(image, shape);
                    }
                }
                return shape;
            } else {
                return delegate.apply(v);
            }
        }
    }

    /**
     * a special renderer that can turn outlines on and off
     * in this demo.
     * You won't need this for a real application.
     * Use BasicVertexRenderer instead
     *
     * @author Tom Nelson
     */
    class DemoRenderer<V, E> extends BasicVertexRenderer<V, E> {
        //        public void paintIconForVertex(RenderContext<V,E> rc, V v, Layout<V,E> layout) {
        //
        //            Point2D p = layout.transform(v);
        //            p = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, p);
        //            float x = (float)p.getX();
        //            float y = (float)p.getY();
        //
        //            GraphicsDecorator g = rc.getGraphicsContext();
        //            boolean outlineImages = false;
        //            Function<V,Icon> vertexIconFunction = rc.getVertexIconTransformer();
        //
        //            if(vertexIconFunction instanceof DemoVertexIconTransformer) {
        //                outlineImages = ((DemoVertexIconTransformer<V>)vertexIconFunction).isOutlineImages();
        //            }
        //            Icon icon = vertexIconFunction.transform(v);
        //            if(icon == null || outlineImages) {
        //
        //                Shape s = AffineTransform.getTranslateInstance(x,y).
        //                    createTransformedShape(rc.getVertexShapeTransformer().transform(v));
        //                paintShapeForVertex(rc, v, s);
        //            }
        //            if(icon != null) {
        //                int xLoc = (int) (x - icon.getIconWidth()/2);
        //                int yLoc = (int) (y - icon.getIconHeight()/2);
        //                icon.paintIcon(rc.getScreenDevice(), g.getDelegate(), xLoc, yLoc);
        //            }
        //        }
    }


//    private static List<CellTower> getCellTowerTraces(String csv_file_name) throws FileNotFoundException {
//
//        ArrayList<CellTower> cellTowers = new ArrayList<CellTower>();
//
//        Scanner scanner = new Scanner(new File(csv_file_name));
//        scanner.useDelimiter(",");
//
//        while (scanner.hasNextLine()) {
//
//            String temp = scanner.nextLine();
//
//            if (temp.charAt(0) != 'Y') {
//
//                String[] line = temp.split(",");
//                int cid = Integer.parseInt(line[5]);
//                float temp_lat = Float.parseFloat(line[6]);
//                float temp_lon = Float.parseFloat(line[7]);
//                CellTower c = new CellTower(cid, temp_lat, temp_lon);
//                cellTowers.add(c);
//            }
//        }
//
//        scanner.close();
//
//        return cellTowers;
//    }
    
    
    private static List<CellTower> getCellTowerTraces(List<Trace> traceList) throws FileNotFoundException {
        ArrayList<CellTower> cellTowers = new ArrayList<CellTower>();

    	for (int i = 0; i < traceList.size(); i++) {
                cellTowers.add(traceList.get(i).getCellTower());
		}
    	
        return cellTowers;
    }
    
    
    

    private static HashMap<Integer, CellTower> findUniqueCellTowers(List<CellTower> traces) {
        HashMap<Integer, CellTower> hm = new HashMap<Integer, CellTower>();
        for (int i = 0; i < traces.size(); i++) {
            hm.put(traces.get(i).getCid(), traces.get(i));
        }

        return hm;
    }

    //    public static void main(String[] args) throws FileNotFoundException {
    //        JFrame frame = new JFrame();
    ////        JPanel panel = new JPanel();
    //        Container content = frame.getContentPane();
    //        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    //
    //        Topology r = new Topology();
    //
    ////        panel.add(r);
    //        content.add(r);
    //        frame.pack();
    //        frame.setVisible(true);
    //    }

    public HashMap<Integer, CellTower> getUniqueCellTowers() {
        return uniqueCellTowers;
    }

    public void setUniqueCellTowers(HashMap<Integer, CellTower> uniqueCellTowers) {
        this.uniqueCellTowers = uniqueCellTowers;
        this.vertexCount = uniqueCellTowers.size();
    }

    private boolean addEdgeToHashMap(int s1, int s2, int edgeNumber) {
        boolean result = false;

        if (this.edgeMap.get(s1) == null) {
            this.edgeMap.put(s1, new HashMap<Integer, Integer>());
        }

        if (this.edgeMap.get(s2) == null) {
            this.edgeMap.put(s2, new HashMap<Integer, Integer>());
        }

        if (this.edgeMap.get(s1).get(s2) == null) {
            this.edgeMap.get(s1).put(s2, edgeNumber);
            result = true;
        }
        if (this.edgeMap.get(s2).get(s1) == null) {
            this.edgeMap.get(s2).put(s1, edgeNumber);
            result = true;
        }

        return result;

    }

    public HashMap<Integer, HashMap<Integer, Integer>> getEdgeMap() {
        return edgeMap;
    }

    public void setEdgeMap(HashMap<Integer, HashMap<Integer, Integer>> edgeMap) {
        this.edgeMap = edgeMap;
    }

    private float getLatitudeFromCid(int cid) {
        return (float)this.uniqueCellTowers.get(cid).getLat();
    }

    private float getLongitudeFromCid(int cid) {
        return (float)this.uniqueCellTowers.get(cid).getLon();
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    
    public void stop() {
    	super.stop();
        System.out.println("AppletApp.stop()");
    }

    public void destroy() {
    	super.destroy();
        System.out.println("AppletApp.destroy()");
    }
}
