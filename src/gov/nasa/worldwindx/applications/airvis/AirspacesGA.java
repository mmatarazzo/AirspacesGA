/**
 * Based on NASA World Wind Example Application - Airspaces.java
 */
package gov.nasa.worldwindx.applications.airvis;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.AirspaceLayer;
import gov.nasa.worldwind.pick.PickedObjectList;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.render.airspaces.*;
import gov.nasa.worldwind.render.airspaces.Box;
import gov.nasa.worldwind.render.airspaces.Polygon;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.view.orbit.BasicOrbitView;
import gov.nasa.worldwindx.examples.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

/**
 * @author mmatarazzo
 * 
 * Uses World Wind Shapes to draw 3-dimensional representations of the airspace infrastructure
 * of the Baltimore - Washington D.C. metropolitan area, to include the D.C. SFRA and Class B layers.
 *
 */
public class AirspacesGA extends ApplicationTemplate {
	
	public static final String ACTION_COMMAND_ANTIALIAS = "gov.nasa.worldwind.avkey.ActionCommandAntialias";
    public static final String ACTION_COMMAND_DEPTH_OFFSET = "gov.nasa.worldwind.avkey.ActionCommandDepthOffset";
    public static final String ACTION_COMMAND_DRAW_EXTENT = "gov.nasa.worldwind.avkey.ActionCommandDrawExtent";
    public static final String ACTION_COMMAND_DRAW_WIREFRAME = "gov.nasa.worldwind.avkey.ActionCommandDrawWireframe";
    public static final String ACTION_COMMAND_LOAD_DATELINE_CROSSING_AIRSPACES
        = "ActionCommandLoadDatelineCrossingAirspaces";
    public static final String ACTION_COMMAND_LOAD_DEMO_AIRSPACES = "ActionCommandLoadDemoAirspaces";
    public static final String ACTION_COMMAND_LOAD_INTERSECTING_AIRSPACES = "ActionCommandLoadIntersectingAirspaces";
    public static final String ACTION_COMMAND_ZOOM_TO_DEMO_AIRSPACES = "ActionCommandZoomToDemoAirspaces";
    public static final String ACTION_COMMAND_SAVE_AIRSPACES = "ActionCommandSaveAirspaces";
    public static final String ACTION_COMMAND_READ_AIRSPACES = "ActionCommandReadAirspaces";
    
    public static final String ACTION_COMMAND_DRAW_DCSFRA = "ActionCommandDrawDCSFRA";
    public static final String ACTION_COMMAND_DRAW_IADCLASSB = "ActionCommandDrawIADCLASSB";
	
	public static class AppFrame extends ApplicationTemplate.AppFrame {
		
		protected AirspacesController controller;
        protected AirspacesPanel airspacesPanel;
        protected FlatWorldPanel flatWorldPanel;
        
        public AppFrame()
        {
            this.controller = new AirspacesController(this);
            this.controller.actionPerformed(new ActionEvent(this, 0, ACTION_COMMAND_LOAD_DEMO_AIRSPACES));
            this.getLayerPanel().update(this.getWwd());

            this.airspacesPanel = new AirspacesPanel();
            this.airspacesPanel.addActionListener(this.controller);

            this.flatWorldPanel = new FlatWorldPanel(this.getWwd());

            javax.swing.Box box = javax.swing.Box.createVerticalBox();
            box.add(this.airspacesPanel);
            box.add(this.flatWorldPanel);
            this.getLayerPanel().add(box, BorderLayout.SOUTH);

            this.pack();
        }

        public LayerPanel getLayerPanel()
        {
            return this.layerPanel;
        }
	}
	
	public static class AirspacesPanel extends JPanel implements ActionListener {
		
		protected EventListenerList eventListeners = new EventListenerList();
		
		public AirspacesPanel()
        {
            this.makePanel();
        }
		
		protected void makePanel()
        {
            this.setLayout(new GridLayout(0, 1, 0, 5)); // rows, cols, hgap, vgap
            this.setBorder(
                new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), new TitledBorder("Airspaces")));

            JButton btn = new JButton("Load Demo Airspaces");
            btn.setActionCommand(ACTION_COMMAND_LOAD_DEMO_AIRSPACES);
            btn.addActionListener(this);
            this.add(btn);

            btn = new JButton("Load Intersecting Airspaces");
            btn.setActionCommand(ACTION_COMMAND_LOAD_INTERSECTING_AIRSPACES);
            btn.addActionListener(this);
            this.add(btn);

            btn = new JButton("Load Dateline Crossing Airspaces");
            btn.setActionCommand(ACTION_COMMAND_LOAD_DATELINE_CROSSING_AIRSPACES);
            btn.addActionListener(this);
            this.add(btn);

            btn = new JButton("Zoom to Demo Airspaces");
            btn.setActionCommand(ACTION_COMMAND_ZOOM_TO_DEMO_AIRSPACES);
            btn.addActionListener(this);
            this.add(btn);

            btn = new JButton("Save Airspaces");
            btn.setActionCommand(ACTION_COMMAND_SAVE_AIRSPACES);
            btn.addActionListener(this);
            this.add(btn);

            btn = new JButton("Read Airspaces");
            btn.setActionCommand(ACTION_COMMAND_READ_AIRSPACES);
            btn.addActionListener(this);
            this.add(btn);

            JCheckBox cb = new JCheckBox("Antialias", false);
            cb.setActionCommand(ACTION_COMMAND_ANTIALIAS);
            cb.addActionListener(this);
            this.add(cb);

            cb = new JCheckBox("Fix Z-Fighting", false);
            cb.setActionCommand(ACTION_COMMAND_DEPTH_OFFSET);
            cb.addActionListener(this);
            this.add(cb);

            cb = new JCheckBox("Show Wireframe", false);
            cb.setActionCommand(ACTION_COMMAND_DRAW_WIREFRAME);
            cb.addActionListener(this);
            this.add(cb);

            cb = new JCheckBox("Show Bounds", false);
            cb.setActionCommand(ACTION_COMMAND_DRAW_EXTENT);
            cb.addActionListener(this);
            this.add(cb);
            
            cb = new JCheckBox("DC SFRA", false);
            cb.setActionCommand(ACTION_COMMAND_DRAW_DCSFRA);
            cb.addActionListener(this);
            this.add(cb);
            
            cb = new JCheckBox("IAD CLASS B", false);
            cb.setActionCommand(ACTION_COMMAND_DRAW_IADCLASSB);
            cb.addActionListener(this);
            this.add(cb);
        }
		
		public void addActionListener(ActionListener listener)
        {
            this.eventListeners.add(ActionListener.class, listener);
        }

        public void removeActionListener(ActionListener listener)
        {
            this.eventListeners.remove(ActionListener.class, listener);
        }
		
		public void actionPerformed(ActionEvent actionEvent)
        {
            this.callActionListeners(actionEvent);
        }
		
		protected void callActionListeners(ActionEvent actionEvent)
        {
            ActionListener[] actionListeners = this.eventListeners.getListeners(ActionListener.class);
            if (actionListeners == null)
                return;

            for (ActionListener listener : actionListeners)
            {
                listener.actionPerformed(actionEvent);
            }
        }
	}
	
	public static class AirspacesController implements ActionListener {
		
		protected AppFrame frame;
        // AWT/Swing stuff.
        protected JFileChooser fileChooser;
        // World Wind stuff.
        protected AirspaceLayer aglAirspaces;
        protected AirspaceLayer amslAirspaces;
        protected Airspace lastHighlit;
        protected AirspaceAttributes lastAttrs;
        protected Annotation lastAnnotation;
        protected BasicDragger dragger;
        
        protected HashMap<String, Airspace> airspaces;
        
        public AirspacesController(AppFrame appFrame)
        {
            this.frame = appFrame;

            // Construct a layer that will hold the airspaces and annotations.
            this.aglAirspaces = new AirspaceLayer();
            this.amslAirspaces = new AirspaceLayer();
            this.aglAirspaces.setName("AGL Airspaces");
            this.amslAirspaces.setName("AMSL Airspaces");
            this.aglAirspaces.setEnableBatchPicking(false);
            this.amslAirspaces.setEnableBatchPicking(false);
            insertBeforePlacenames(this.frame.getWwd(), this.aglAirspaces);
            insertBeforePlacenames(this.frame.getWwd(), this.amslAirspaces);
            
            this.airspaces = new HashMap<String, Airspace>();

            this.initializeSelectionMonitoring();
        }

        public WorldWindow getWwd()
        {
            return this.frame.getWwd();
        }
        
        public void actionPerformed(ActionEvent e)
        {
            if (ACTION_COMMAND_LOAD_DEMO_AIRSPACES.equalsIgnoreCase(e.getActionCommand()))
            {
                this.doLoadDemoAirspaces();
            }
            else if (ACTION_COMMAND_LOAD_DATELINE_CROSSING_AIRSPACES.equalsIgnoreCase(e.getActionCommand()))
            {
                this.doLoadDatelineCrossingAirspaces();
            }
            else if (ACTION_COMMAND_LOAD_INTERSECTING_AIRSPACES.equalsIgnoreCase(e.getActionCommand()))
            {
                //this.doLoadIntersectingAirspaces();
            }
            else if (ACTION_COMMAND_ZOOM_TO_DEMO_AIRSPACES.equalsIgnoreCase(e.getActionCommand()))
            {
                this.doZoomToAirspaces();
            }
            else if (ACTION_COMMAND_SAVE_AIRSPACES.equalsIgnoreCase(e.getActionCommand()))
            {
                //this.doSaveAirspaces(e);
            }
            else if (ACTION_COMMAND_READ_AIRSPACES.equalsIgnoreCase(e.getActionCommand()))
            {
                //this.doReadAirspaces(e);
            }
            else if (ACTION_COMMAND_ANTIALIAS.equalsIgnoreCase(e.getActionCommand()))
            {
                JCheckBox cb = (JCheckBox) e.getSource();
                this.aglAirspaces.setEnableAntialiasing(cb.isSelected());
                this.amslAirspaces.setEnableAntialiasing(cb.isSelected());
                this.getWwd().redraw();
            }
            else if (ACTION_COMMAND_DEPTH_OFFSET.equalsIgnoreCase(e.getActionCommand()))
            {
                JCheckBox cb = (JCheckBox) e.getSource();
                this.aglAirspaces.setEnableDepthOffset(cb.isSelected());
                this.amslAirspaces.setEnableDepthOffset(cb.isSelected());
                this.getWwd().redraw();
            }
            else if (ACTION_COMMAND_DRAW_WIREFRAME.equalsIgnoreCase(e.getActionCommand()))
            {
                JCheckBox cb = (JCheckBox) e.getSource();
                this.aglAirspaces.setDrawWireframe(cb.isSelected());
                this.amslAirspaces.setDrawWireframe(cb.isSelected());
                this.getWwd().redraw();
            }
            else if (ACTION_COMMAND_DRAW_EXTENT.equalsIgnoreCase(e.getActionCommand()))
            {
                JCheckBox cb = (JCheckBox) e.getSource();
                this.aglAirspaces.setDrawExtents(cb.isSelected());
                this.amslAirspaces.setDrawExtents(cb.isSelected());
                this.getWwd().redraw();
            }
            else if (ACTION_COMMAND_DRAW_DCSFRA.equalsIgnoreCase(e.getActionCommand()))
            {
            	JCheckBox cb = (JCheckBox) e.getSource();
            	if (cb.isSelected())
            	{
            		this.doDrawDcSfra();
            	}
            	else
            	{
            		this.doRemoveDcSfra();
            	}
            }
            else if (ACTION_COMMAND_DRAW_IADCLASSB.equalsIgnoreCase(e.getActionCommand()))
            {
            	JCheckBox cb = (JCheckBox) e.getSource();
            	if (cb.isSelected())
            	{
            		this.doDrawIadClassB();
            	}
            	else
            	{
            		this.doRemoveIadClassB();
            	}
            }
        }
        
        public void setAirspaces(Collection<Airspace> airspaces)
        {
            this.aglAirspaces.removeAllAirspaces();
            this.amslAirspaces.removeAllAirspaces();

            if (airspaces != null)
            {
                for (Airspace a : airspaces)
                {
                    if (a == null)
                        continue;

                    if (a.getAltitudeDatum()[0].equals(AVKey.ABOVE_MEAN_SEA_LEVEL) &&
                        a.getAltitudeDatum()[1].equals(AVKey.ABOVE_MEAN_SEA_LEVEL))
                    {
                        this.amslAirspaces.addAirspace(a);
                    }
                    else
                    {
                        this.aglAirspaces.addAirspace(a);
                    }
                }
            }

            this.getWwd().redraw();
        }
        
        public void initializeSelectionMonitoring()
        {
            this.dragger = new BasicDragger(this.getWwd());
            this.getWwd().addSelectListener(new SelectListener()
            {
                public void selected(SelectEvent event)
                {
                    // Have rollover events highlight the rolled-over object.
                    if (event.getEventAction().equals(SelectEvent.ROLLOVER) && !dragger.isDragging())
                    {
                        if (AirspacesController.this.highlight(event.getTopObject()))
                            AirspacesController.this.getWwd().redraw();
                    }
                    // Have drag events drag the selected object.
                    else if (event.getEventAction().equals(SelectEvent.DRAG_END)
                        || event.getEventAction().equals(SelectEvent.DRAG))
                    {
                        // Delegate dragging computations to a dragger.
                        dragger.selected(event);

                        // We missed any roll-over events while dragging, so highlight any under the cursor now,
                        // or de-highlight the dragged shape if it's no longer under the cursor.
                        if (event.getEventAction().equals(SelectEvent.DRAG_END))
                        {
                            PickedObjectList pol = AirspacesController.this.getWwd().getObjectsAtCurrentPosition();
                            if (pol != null)
                            {
                                AirspacesController.this.highlight(pol.getTopObject());
                                AirspacesController.this.getWwd().redraw();
                            }
                        }
                    }
                }
            });
        }
        
        protected boolean highlight(Object o)
        {
            if (this.lastHighlit == o)
                return false; // Same thing selected

            // Turn off highlight if on.
            if (this.lastHighlit != null)
            {
                this.lastHighlit.setAttributes(this.lastAttrs);
                this.lastHighlit = null;
                this.lastAttrs = null;
            }

            // Turn on highlight if selected object is a SurfaceImage.
            if (o instanceof Airspace)
            {
                this.lastHighlit = (Airspace) o;
                this.lastAttrs = this.lastHighlit.getAttributes();
                BasicAirspaceAttributes highlitAttrs = new BasicAirspaceAttributes(this.lastAttrs);
                highlitAttrs.setMaterial(Material.WHITE);
                this.lastHighlit.setAttributes(highlitAttrs);
            }

            return true;
        }
        
        protected void setupDefaultMaterial(Airspace a, Color color)
        {
            a.getAttributes().setDrawOutline(true);
            a.getAttributes().setMaterial(new Material(color));
            a.getAttributes().setOutlineMaterial(new Material(WWUtil.makeColorBrighter(color)));
            a.getAttributes().setOpacity(0.8);
            a.getAttributes().setOutlineOpacity(0.9);
            a.getAttributes().setOutlineWidth(3.0);
        }

        public void doLoadDatelineCrossingAirspaces()
        {
            ArrayList<Airspace> airspaces = new ArrayList<Airspace>();

            // Curtains of different path types crossing the dateline.
            Curtain curtain = new Curtain();
            curtain.setLocations(Arrays.asList(LatLon.fromDegrees(27.0, -112.0), LatLon.fromDegrees(35.0, 138.0)));
            curtain.setAltitudes(1000.0, 100000.0);
            curtain.setTerrainConforming(false, false);
            curtain.setValue(AVKey.DISPLAY_NAME, "Great arc Curtain from America to Japan.");
            this.setupDefaultMaterial(curtain, Color.MAGENTA);
            airspaces.add(curtain);

            curtain = new Curtain();
            curtain.setLocations(Arrays.asList(LatLon.fromDegrees(27.0, -112.0), LatLon.fromDegrees(35.0, 138.0)));
            curtain.setPathType(AVKey.RHUMB_LINE);
            curtain.setAltitudes(1000.0, 100000.0);
            curtain.setTerrainConforming(false, false);
            curtain.setValue(AVKey.DISPLAY_NAME, "Rhumb Curtain from America to Japan.");
            this.setupDefaultMaterial(curtain, Color.CYAN);
            airspaces.add(curtain);

            // Continent sized sphere
            SphereAirspace sphere = new SphereAirspace();
            sphere.setLocation(LatLon.fromDegrees(0.0, -180.0));
            sphere.setAltitude(0.0);
            sphere.setTerrainConforming(false);
            sphere.setRadius(1000000.0);
            this.setupDefaultMaterial(sphere, Color.RED);
            airspaces.add(sphere);

            this.setAirspaces(airspaces);
        }
        
        public void doLoadDemoAirspaces() {
        	
        	ArrayList<Airspace> airspaces = new ArrayList<Airspace>();
        	double minAltitude = 1000;
            double maxAltitude = 10000;
            
            /**
            Cake cake = new Cake();
            cake.setLayers(Arrays.asList(
                new Cake.Layer(LatLon.fromDegrees(36, -121), 10000.0, Angle.fromDegrees(0.0),
                    Angle.fromDegrees(360.0), 10000.0, 15000.0),
                new Cake.Layer(LatLon.fromDegrees(36.1, -121.1), 15000.0, Angle.fromDegrees(0.0),
                    Angle.fromDegrees(360.0), 16000.0, 21000.0),
                new Cake.Layer(LatLon.fromDegrees(35.9, -120.9), 12500.0, Angle.fromDegrees(0.0),
                    Angle.fromDegrees(360.0), 22000.0, 27000.0)));
            cake.getLayers().get(0).setTerrainConforming(false, false);
            cake.getLayers().get(1).setTerrainConforming(false, false);
            cake.getLayers().get(2).setTerrainConforming(false, false);
            cake.setValue(AVKey.DISPLAY_NAME, "3 layer Cake. With disjoint layers.");
            this.setupDefaultMaterial(cake, Color.MAGENTA);
            airspaces.add(cake);
            **/
            
         // Radarc
            PartialCappedCylinder partCyl = new PartialCappedCylinder();
            partCyl.setCenter(LatLon.fromDegrees(46.7477, -122.6372));
            partCyl.setAltitudes(minAltitude, maxAltitude);
            partCyl.setTerrainConforming(false, false);
            // To render a Radarc,
            // (1) Specify inner radius and outer radius.
            // (2) Specify start and stop azimuth.
            partCyl.setRadii(15000.0, 30000.0);
            partCyl.setAzimuths(Angle.fromDegrees(90.0), Angle.fromDegrees(0.0));
            partCyl.setValue(AVKey.DISPLAY_NAME, "Partial Cylinder from 90 to 0 degrees.");
            this.setupDefaultMaterial(partCyl, Color.GRAY);
            airspaces.add(partCyl);
            
         // PolyArc
            PolyArc polyArc = new PolyArc();
            polyArc.setLocations(Arrays.asList(
                LatLon.fromDegrees(45.5, -122.0),
                LatLon.fromDegrees(46.0, -122.0),
                LatLon.fromDegrees(46.0, -121.0),
                LatLon.fromDegrees(45.5, -121.0)));
            polyArc.setAltitudes(5000.0, 10000.0);
            polyArc.setRadius(30000.0);
            polyArc.setAzimuths(Angle.fromDegrees(-45.0), Angle.fromDegrees(135.0));
            polyArc.setTerrainConforming(false, false);
            this.setupDefaultMaterial(polyArc, Color.GRAY);
            airspaces.add(polyArc);
            
            this.setAirspaces(airspaces);
        }
        
        public void doDrawIadClassB() {
        	// Dulles Class B Inner
        	CappedCylinder cyl = new CappedCylinder();
        	cyl.setCenter(LatLon.fromDegrees(38.9345964, -77.4666953));
        	cyl.setRadius(12964.0);
        	cyl.setAltitudes(0.0, 3048.0);
        	cyl.setTerrainConforming(true, true);
        	cyl.setValue(AVKey.DISPLAY_NAME, "Dulles Class B Inner - SFC to 10,000 ft. MSL");
            this.setupDefaultMaterial(cyl, Color.BLUE);
            airspaces.put("IAD-I", cyl);
            
            // Dulles Class B Mid
            PartialCappedCylinder partCyl = new PartialCappedCylinder();
            partCyl.setCenter(LatLon.fromDegrees(38.9345964, -77.4666953));
            partCyl.setAltitudes(457.2, 3048.0);
            partCyl.setTerrainConforming(false, false);
            // To render a Radarc,
            // (1) Specify inner radius and outer radius.
            // (2) Specify start and stop azimuth.
            partCyl.setRadii(12964.0, 22224.0);
            partCyl.setAzimuths(Angle.fromDegrees(153.0), Angle.fromDegrees(47.0));
            partCyl.setValue(AVKey.DISPLAY_NAME, "Dulles Class B Middle - 1,500 to 10,000 ft. MSL.");
            this.setupDefaultMaterial(partCyl, Color.BLUE);
            airspaces.put("IAD-M1", partCyl);
                        
            // Dulles Class B Mid2
            PolyArc polyArc = new PolyArc();
            polyArc.setLocations(Arrays.asList(
                LatLon.fromDegrees(38.9345964, -77.4666953),
                LatLon.fromDegrees(39.07095, -77.27885),
                LatLon.fromDegrees(38.75615, -77.35055)));
            polyArc.setAltitudes(457.2, 3048.0);
            polyArc.setRadius(12964.0);
            polyArc.setAzimuths(Angle.fromDegrees(47.0), Angle.fromDegrees(153.0));
            polyArc.setTerrainConforming(false, false);
            polyArc.setValue(AVKey.DISPLAY_NAME, "Dulles Class B Middle - 1,500 to 10,000 ft. MSL.");
            this.setupDefaultMaterial(polyArc, Color.BLUE);
            airspaces.put("IAD-M2", polyArc);
            
            //Dulles Class B Outer
            PartialCappedCylinder partCylOuter = new PartialCappedCylinder();
            partCylOuter.setCenter(LatLon.fromDegrees(38.9345964, -77.4666953));
            partCylOuter.setAltitudes(762.0, 3048.0);
            partCylOuter.setTerrainConforming(false, false);
            partCylOuter.setRadii(22224.0, 27780.0);
            partCylOuter.setAzimuths(Angle.fromDegrees(160.0), Angle.fromDegrees(39.0));
            partCylOuter.setValue(AVKey.DISPLAY_NAME, "Dulles Class B Middle - 2,500 to 10,000 ft. MSL.");
            this.setupDefaultMaterial(partCylOuter, Color.BLUE);
            airspaces.put("IAD-O1", partCylOuter);
            
            // Dulles Class B Outer 2
            PartialCappedCylinder partCylOuter2 = new PartialCappedCylinder();
            partCylOuter2.setCenter(LatLon.fromDegrees(38.9345964, -77.4666953));
            partCylOuter2.setAltitudes(1371.6, 3048.0);
            partCylOuter2.setTerrainConforming(false, false);
            partCylOuter2.setRadii(27780.0, 37040.0);
            partCylOuter2.setAzimuths(Angle.fromDegrees(220.0), Angle.fromDegrees(337.0));
            partCylOuter2.setValue(AVKey.DISPLAY_NAME, "Dulles Class B Outer - 4,500 to 10,000 ft. MSL.");
            this.setupDefaultMaterial(partCylOuter2, Color.BLUE);
            airspaces.put("IAD-O2", partCylOuter2);
            
            // Dulles Class B Outer 2B
            PartialCappedCylinder partCylOuter2B = new PartialCappedCylinder();
            partCylOuter2B.setCenter(LatLon.fromDegrees(38.9345964, -77.4666953));
            partCylOuter2B.setAltitudes(914.4, 3048.0);
            partCylOuter2B.setTerrainConforming(false, false);
            partCylOuter2B.setRadii(27780.0, 37040.0);
            partCylOuter2B.setAzimuths(Angle.fromDegrees(170.0), Angle.fromDegrees(220.0));
            partCylOuter2B.setValue(AVKey.DISPLAY_NAME, "Dulles Class B Outer - 3,000 to 10,000 ft. MSL.");
            this.setupDefaultMaterial(partCylOuter2B, Color.BLUE);
            airspaces.put("IAD-O2B", partCylOuter2B);
            
            // Dulles Class B Outer 2B
            PartialCappedCylinder partCylOuter2C = new PartialCappedCylinder();
            partCylOuter2C.setCenter(LatLon.fromDegrees(38.9345964, -77.4666953));
            partCylOuter2C.setAltitudes(914.4, 3048.0);
            partCylOuter2C.setTerrainConforming(false, false);
            partCylOuter2C.setRadii(27780.0, 37040.0);
            partCylOuter2C.setAzimuths(Angle.fromDegrees(337.0), Angle.fromDegrees(017.0));
            partCylOuter2C.setValue(AVKey.DISPLAY_NAME, "Dulles Class B Outer - 3,000 to 10,000 ft. MSL.");
            this.setupDefaultMaterial(partCylOuter2C, Color.BLUE);
            airspaces.put("IAD-O2C", partCylOuter2C);
            
            this.setAirspaces(airspaces.values());
        }
        
        public void doRemoveIadClassB() {
        	Iterator<Map.Entry<String, Airspace>> iterator = airspaces.entrySet().iterator();
        	while (iterator.hasNext()) {
        	     Map.Entry<String, Airspace> entry = iterator.next();
        	     if (entry.getKey().contains("IAD")) {
        	          iterator.remove();
        	     }
        	}
        	
        	this.setAirspaces(airspaces.values());
        }
        
        public void doDrawDcSfra()
        {       	
        	// DC SFRA main
        	CappedCylinder cyl = new CappedCylinder();
            cyl.setCenter(LatLon.fromDegrees(38.8522, -77.0378));
            cyl.setRadius(55560.0);
            cyl.setAltitudes(0.0, 5486.4);
            cyl.setTerrainConforming(true, true);
            cyl.setValue(AVKey.DISPLAY_NAME, "DC SFRA. SFC - 18,000FT MSL.");
            this.setupDefaultMaterial(cyl, Color.RED);
            airspaces.put("DCSFRA-MAIN", cyl);
            
            this.setAirspaces(airspaces.values());
        }
        
        public void doRemoveDcSfra()
        {
        	Iterator<Map.Entry<String, Airspace>> iterator = airspaces.entrySet().iterator();
        	while (iterator.hasNext()) {
        	     Map.Entry<String, Airspace> entry = iterator.next();
        	     if (entry.getKey().contains("DCSFRA")) {
        	          iterator.remove();
        	     }
        	}
        	
        	this.setAirspaces(airspaces.values());
        }
        
        public void doZoomToAirspaces()
        {
            BasicOrbitView view = (BasicOrbitView) this.getWwd().getView();
            Position center = Position.fromDegrees(38.8522, -77.0378, 0.0);
            Angle heading = Angle.fromDegrees(0.0);
            Angle pitch = Angle.fromDegrees(30.0);
            double zoom = 600000.0;
            view.addPanToAnimator(center, heading, pitch, zoom, true);
        }
        
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		start("General Aviation Airspaces", AppFrame.class);
	}
	
	protected static Iterable<LatLon> makeLatLon(double[] src, int offset, int length)
    {
        int numCoords = (int) Math.floor(length / 2.0);
        LatLon[] dest = new LatLon[numCoords];
        for (int i = 0; i < numCoords; i++)
        {
            double lonDegrees = src[offset + 2 * i];
            double latDegrees = src[offset + 2 * i + 1];
            dest[i] = LatLon.fromDegrees(latDegrees, lonDegrees);
        }
        return Arrays.asList(dest);
    }

    protected static Iterable<LatLon> makeLatLon(double[] src)
    {
        return makeLatLon(src, 0, src.length);
    }

}
