/**
 * Based on NASA World Wind Example Application - Airspaces.java
 */
package gov.nasa.worldwindx.applications.airvis;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.AirspaceLayer;
import gov.nasa.worldwind.pick.PickedObjectList;
import gov.nasa.worldwind.render.Annotation;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.airspaces.Airspace;
import gov.nasa.worldwind.render.airspaces.AirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.BasicAirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.CappedCylinder;
import gov.nasa.worldwind.render.airspaces.Curtain;
import gov.nasa.worldwind.render.airspaces.PartialCappedCylinder;
import gov.nasa.worldwind.render.airspaces.PolyArc;
import gov.nasa.worldwind.render.airspaces.SphereAirspace;
import gov.nasa.worldwind.util.BasicDragger;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.view.orbit.BasicOrbitView;
import gov.nasa.worldwindx.examples.ApplicationTemplate;
import gov.nasa.worldwindx.examples.FlatWorldPanel;
import gov.nasa.worldwindx.examples.LayerPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.EventListenerList;

/**
 * @author mmatarazzo
 * 
 *         Uses World Wind Shapes to draw 3-dimensional representations of the
 *         airspace infrastructure of the Baltimore - Washington D.C.
 *         metropolitan area, to include the D.C. SFRA and Class B layers.
 * 
 */
public class AirspacesGA extends ApplicationTemplate {

	public static final String ACTION_COMMAND_ANTIALIAS = "gov.nasa.worldwind.avkey.ActionCommandAntialias";
	public static final String ACTION_COMMAND_DEPTH_OFFSET = "gov.nasa.worldwind.avkey.ActionCommandDepthOffset";
	public static final String ACTION_COMMAND_DRAW_EXTENT = "gov.nasa.worldwind.avkey.ActionCommandDrawExtent";
	public static final String ACTION_COMMAND_DRAW_WIREFRAME = "gov.nasa.worldwind.avkey.ActionCommandDrawWireframe";
	public static final String ACTION_COMMAND_LOAD_DATELINE_CROSSING_AIRSPACES = "ActionCommandLoadDatelineCrossingAirspaces";
	public static final String ACTION_COMMAND_LOAD_DEMO_AIRSPACES = "ActionCommandLoadDemoAirspaces";
	public static final String ACTION_COMMAND_LOAD_INTERSECTING_AIRSPACES = "ActionCommandLoadIntersectingAirspaces";
	public static final String ACTION_COMMAND_ZOOM_TO_DEMO_AIRSPACES = "ActionCommandZoomToDemoAirspaces";
	public static final String ACTION_COMMAND_SAVE_AIRSPACES = "ActionCommandSaveAirspaces";
	public static final String ACTION_COMMAND_READ_AIRSPACES = "ActionCommandReadAirspaces";

	public static final String ACTION_COMMAND_DRAW_DCSFRA = "ActionCommandDrawDCSFRA";
	public static final String ACTION_COMMAND_DRAW_IADCLASSB = "ActionCommandDrawIADCLASSB";
	public static final String ACTION_COMMAND_DRAW_HEFCLASSD = "ActionCommandDrawHEFCLASSD";

	public static class AppFrame extends ApplicationTemplate.AppFrame {

		protected AirspacesController controller;
		protected AirspacesPanel airspacesPanel;
		protected FlatWorldPanel flatWorldPanel;

		public AppFrame() {
			this.controller = new AirspacesController(this);
			this.controller.actionPerformed(new ActionEvent(this, 0,
					ACTION_COMMAND_LOAD_DEMO_AIRSPACES));
			this.getLayerPanel().update(this.getWwd());

			this.airspacesPanel = new AirspacesPanel();
			this.airspacesPanel.addActionListener(this.controller);

			this.flatWorldPanel = new FlatWorldPanel(this.getWwd());

			javax.swing.Box box = javax.swing.Box.createVerticalBox();
			box.add(this.airspacesPanel);
			box.add(this.flatWorldPanel);
			this.getLayerPanel().add(box, BorderLayout.SOUTH);

			this.pack();
			// this.setLocationByPlatform(true);
			this.setExtendedState(Frame.MAXIMIZED_BOTH);
			// this.setLocationRelativeTo(null);
		}

		@Override
		public LayerPanel getLayerPanel() {
			return this.layerPanel;
		}
	}

	public static class AirspacesPanel extends JPanel implements ActionListener {

		protected EventListenerList eventListeners = new EventListenerList();

		public AirspacesPanel() {
			this.makePanel();
		}

		protected void makePanel() {
			this.setLayout(new GridLayout(0, 1, 0, 5)); // rows, cols, hgap,
														// vgap
			this.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(
					9, 9, 9, 9), new TitledBorder("Airspaces")));

			JButton btn = new JButton("Load Demo Airspaces");
			btn.setActionCommand(ACTION_COMMAND_LOAD_DEMO_AIRSPACES);
			btn.addActionListener(this);
			// this.add(btn);

			btn = new JButton("Load Intersecting Airspaces");
			btn.setActionCommand(ACTION_COMMAND_LOAD_INTERSECTING_AIRSPACES);
			btn.addActionListener(this);
			// this.add(btn);

			btn = new JButton("Load Dateline Crossing Airspaces");
			btn.setActionCommand(ACTION_COMMAND_LOAD_DATELINE_CROSSING_AIRSPACES);
			btn.addActionListener(this);
			// this.add(btn);

			btn = new JButton("Zoom to Airspaces");
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
			
			cb = new JCheckBox("HEF CLASS D", false);
			cb.setActionCommand(ACTION_COMMAND_DRAW_HEFCLASSD);
			cb.addActionListener(this);
			this.add(cb);
			
		}

		public void addActionListener(ActionListener listener) {
			this.eventListeners.add(ActionListener.class, listener);
		}

		public void removeActionListener(ActionListener listener) {
			this.eventListeners.remove(ActionListener.class, listener);
		}

		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			this.callActionListeners(actionEvent);
		}

		protected void callActionListeners(ActionEvent actionEvent) {
			ActionListener[] actionListeners = this.eventListeners
					.getListeners(ActionListener.class);
			if (actionListeners == null)
				return;

			for (ActionListener listener : actionListeners) {
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

		public AirspacesController(AppFrame appFrame) {
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

		public WorldWindow getWwd() {
			return this.frame.getWwd();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (ACTION_COMMAND_LOAD_DEMO_AIRSPACES.equalsIgnoreCase(e
					.getActionCommand())) {
				// this.doLoadDemoAirspaces();
			} else if (ACTION_COMMAND_LOAD_DATELINE_CROSSING_AIRSPACES
					.equalsIgnoreCase(e.getActionCommand())) {
				// this.doLoadDatelineCrossingAirspaces();
			} else if (ACTION_COMMAND_LOAD_INTERSECTING_AIRSPACES
					.equalsIgnoreCase(e.getActionCommand())) {
				// this.doLoadIntersectingAirspaces();
			} else if (ACTION_COMMAND_ZOOM_TO_DEMO_AIRSPACES.equalsIgnoreCase(e
					.getActionCommand())) {
				this.doZoomToAirspaces();
			} else if (ACTION_COMMAND_SAVE_AIRSPACES.equalsIgnoreCase(e
					.getActionCommand())) {
				// this.doSaveAirspaces(e);
			} else if (ACTION_COMMAND_READ_AIRSPACES.equalsIgnoreCase(e
					.getActionCommand())) {
				// this.doReadAirspaces(e);
			} else if (ACTION_COMMAND_ANTIALIAS.equalsIgnoreCase(e
					.getActionCommand())) {
				JCheckBox cb = (JCheckBox) e.getSource();
				this.aglAirspaces.setEnableAntialiasing(cb.isSelected());
				this.amslAirspaces.setEnableAntialiasing(cb.isSelected());
				this.getWwd().redraw();
			} else if (ACTION_COMMAND_DEPTH_OFFSET.equalsIgnoreCase(e
					.getActionCommand())) {
				JCheckBox cb = (JCheckBox) e.getSource();
				this.aglAirspaces.setEnableDepthOffset(cb.isSelected());
				this.amslAirspaces.setEnableDepthOffset(cb.isSelected());
				this.getWwd().redraw();
			} else if (ACTION_COMMAND_DRAW_WIREFRAME.equalsIgnoreCase(e
					.getActionCommand())) {
				JCheckBox cb = (JCheckBox) e.getSource();
				this.aglAirspaces.setDrawWireframe(cb.isSelected());
				this.amslAirspaces.setDrawWireframe(cb.isSelected());
				this.getWwd().redraw();
			} else if (ACTION_COMMAND_DRAW_EXTENT.equalsIgnoreCase(e
					.getActionCommand())) {
				JCheckBox cb = (JCheckBox) e.getSource();
				this.aglAirspaces.setDrawExtents(cb.isSelected());
				this.amslAirspaces.setDrawExtents(cb.isSelected());
				this.getWwd().redraw();
			} else if (ACTION_COMMAND_DRAW_DCSFRA.equalsIgnoreCase(
					e.getActionCommand())) {
				JCheckBox cb = (JCheckBox) e.getSource();
				if (cb.isSelected()) {
					this.doDrawDcSfra();
				} else {
					this.doRemoveDcSfra();
				}
			} else if (ACTION_COMMAND_DRAW_IADCLASSB.equalsIgnoreCase(
					e.getActionCommand())) {
				JCheckBox cb = (JCheckBox) e.getSource();
				if (cb.isSelected()) {
					this.doDrawIadClassB();
				} else {
					this.doRemoveIadClassB();
				}
			} else if (ACTION_COMMAND_DRAW_HEFCLASSD.equalsIgnoreCase(
					e.getActionCommand())) {
				JCheckBox cb = (JCheckBox) e.getSource();
				if (cb.isSelected()) {
					this.doDrawHefClassD();
				} else {
					this.doRemoveHefClassD();
				}
			}
		}

		public void setAirspaces(Collection<Airspace> airspaces) {
			this.aglAirspaces.removeAllAirspaces();
			this.amslAirspaces.removeAllAirspaces();

			if (airspaces != null) {
				for (Airspace a : airspaces) {
					if (a == null)
						continue;

					if (a.getAltitudeDatum()[0]
							.equals(AVKey.ABOVE_MEAN_SEA_LEVEL)
							&& a.getAltitudeDatum()[1]
									.equals(AVKey.ABOVE_MEAN_SEA_LEVEL)) {
						this.amslAirspaces.addAirspace(a);
					} else {
						this.aglAirspaces.addAirspace(a);
					}
				}
			}

			this.getWwd().redraw();
		}

		public void initializeSelectionMonitoring() {
			this.dragger = new BasicDragger(this.getWwd());
			this.getWwd().addSelectListener(new SelectListener() {
				@Override
				public void selected(SelectEvent event) {
					// Have rollover events highlight the rolled-over object.
					if (event.getEventAction().equals(SelectEvent.ROLLOVER)
							&& !dragger.isDragging()) {
						if (AirspacesController.this.highlight(event
								.getTopObject()))
							AirspacesController.this.getWwd().redraw();
					}
					// Have drag events drag the selected object.
					else if (event.getEventAction()
							.equals(SelectEvent.DRAG_END)
							|| event.getEventAction().equals(SelectEvent.DRAG)) {
						// Delegate dragging computations to a dragger.
						// dragger.selected(event);

						// We missed any roll-over events while dragging, so
						// highlight any under the cursor now,
						// or de-highlight the dragged shape if it's no longer
						// under the cursor.
						if (event.getEventAction().equals(SelectEvent.DRAG_END)) {
							PickedObjectList pol = AirspacesController.this
									.getWwd().getObjectsAtCurrentPosition();
							if (pol != null) {
								AirspacesController.this.highlight(pol
										.getTopObject());
								AirspacesController.this.getWwd().redraw();
							}
						}
					}
				}
			});
		}

		protected boolean highlight(Object o) {
			if (this.lastHighlit == o)
				return false; // Same thing selected

			// Turn off highlight if on.
			if (this.lastHighlit != null) {
				this.lastHighlit.setAttributes(this.lastAttrs);
				this.lastHighlit = null;
				this.lastAttrs = null;
			}

			// Turn on highlight if selected object is a SurfaceImage.
			if (o instanceof Airspace) {
				this.lastHighlit = (Airspace) o;
				this.lastAttrs = this.lastHighlit.getAttributes();
				BasicAirspaceAttributes highlitAttrs = new BasicAirspaceAttributes(
						this.lastAttrs);
				highlitAttrs.setMaterial(Material.WHITE);
				this.lastHighlit.setAttributes(highlitAttrs);
			}

			return true;
		}

		protected void setupDefaultMaterial(Airspace a, Color color) {
			a.getAttributes().setDrawOutline(true);
			a.getAttributes().setMaterial(new Material(color));
			a.getAttributes().setOutlineMaterial(
					new Material(WWUtil.makeColorBrighter(color)));
			// Down from 0.8 for better vis
			a.getAttributes().setOpacity(0.4);
			a.getAttributes().setOutlineOpacity(0.9);
			a.getAttributes().setOutlineWidth(3.0);
		}

		public void doLoadDatelineCrossingAirspaces() {
			ArrayList<Airspace> airspaces = new ArrayList<Airspace>();

			// Curtains of different path types crossing the dateline.
			Curtain curtain = new Curtain();
			curtain.setLocations(Arrays.asList(
					LatLon.fromDegrees(27.0, -112.0),
					LatLon.fromDegrees(35.0, 138.0)));
			curtain.setAltitudes(1000.0, 100000.0);
			curtain.setTerrainConforming(false, false);
			curtain.setValue(AVKey.DISPLAY_NAME,
					"Great arc Curtain from America to Japan.");
			this.setupDefaultMaterial(curtain, Color.MAGENTA);
			airspaces.add(curtain);

			curtain = new Curtain();
			curtain.setLocations(Arrays.asList(
					LatLon.fromDegrees(27.0, -112.0),
					LatLon.fromDegrees(35.0, 138.0)));
			curtain.setPathType(AVKey.RHUMB_LINE);
			curtain.setAltitudes(1000.0, 100000.0);
			curtain.setTerrainConforming(false, false);
			curtain.setValue(AVKey.DISPLAY_NAME,
					"Rhumb Curtain from America to Japan.");
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
			 * Cake cake = new Cake(); cake.setLayers(Arrays.asList( new
			 * Cake.Layer(LatLon.fromDegrees(36, -121), 10000.0,
			 * Angle.fromDegrees(0.0), Angle.fromDegrees(360.0), 10000.0,
			 * 15000.0), new Cake.Layer(LatLon.fromDegrees(36.1, -121.1),
			 * 15000.0, Angle.fromDegrees(0.0), Angle.fromDegrees(360.0),
			 * 16000.0, 21000.0), new Cake.Layer(LatLon.fromDegrees(35.9,
			 * -120.9), 12500.0, Angle.fromDegrees(0.0),
			 * Angle.fromDegrees(360.0), 22000.0, 27000.0)));
			 * cake.getLayers().get(0).setTerrainConforming(false, false);
			 * cake.getLayers().get(1).setTerrainConforming(false, false);
			 * cake.getLayers().get(2).setTerrainConforming(false, false);
			 * cake.setValue(AVKey.DISPLAY_NAME,
			 * "3 layer Cake. With disjoint layers.");
			 * this.setupDefaultMaterial(cake, Color.MAGENTA);
			 * airspaces.add(cake);
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
			partCyl.setValue(AVKey.DISPLAY_NAME,
					"Partial Cylinder from 90 to 0 degrees.");
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
			polyArc.setAzimuths(Angle.fromDegrees(-45.0),
					Angle.fromDegrees(135.0));
			polyArc.setTerrainConforming(false, false);
			this.setupDefaultMaterial(polyArc, Color.GRAY);
			airspaces.add(polyArc);

			this.setAirspaces(airspaces);
		}

		public void doDrawIadClassB() {
			// Dulles Class B Inner
			CappedCylinder iadInnerCyl = new CappedCylinder();
			iadInnerCyl.setCenter(LatLon.fromDegrees(38.934722, -77.466667));
			iadInnerCyl.setRadius(12964.0);
			iadInnerCyl.setAltitudes(0.0, 3048.0);
			iadInnerCyl.setTerrainConforming(true, true);
			iadInnerCyl.setValue(AVKey.DISPLAY_NAME, "Dulles Class B Inner - SFC to 10,000 ft. MSL");
			this.setupDefaultMaterial(iadInnerCyl, Color.BLUE);
			airspaces.put("IAD-I", iadInnerCyl);

			// Dulles Class B Mid 1
			PartialCappedCylinder iadMid1PartCyl = new PartialCappedCylinder();
			iadMid1PartCyl.setCenter(LatLon.fromDegrees(38.934722, -77.466667));
			iadMid1PartCyl.setAltitudes(457.2, 3048.0);
			iadMid1PartCyl.setTerrainConforming(false, false);
			iadMid1PartCyl.setRadii(12964.0, 22224.0);
			// TODO use method
			iadMid1PartCyl.setAzimuths(Angle.fromDegrees(144.0), Angle.fromDegrees(39.0));
			iadMid1PartCyl.setValue(AVKey.DISPLAY_NAME, "Dulles Class B Middle - 1,500 to 10,000 ft. MSL.");
			this.setupDefaultMaterial(iadMid1PartCyl, Color.BLUE);
			airspaces.put("IAD-M1", iadMid1PartCyl);

			// Dulles Class B Mid 2
			PolyArc iadMid2PolyArc = new PolyArc();
			iadMid2PolyArc.setLocations(Arrays.asList(
					LatLon.fromDegrees(38.934722, -77.466667),
					LatLon.fromDegrees(39.09, -77.304722),
					LatLon.fromDegrees(38.772778, -77.316111)));
			iadMid2PolyArc.setAltitudes(457.2, 3048.0);
			iadMid2PolyArc.setRadius(12964.0);
			// TODO use method
			iadMid2PolyArc.setAzimuths(Angle.fromDegrees(39.0), Angle.fromDegrees(144.0));
			iadMid2PolyArc.setTerrainConforming(false, false);
			iadMid2PolyArc.setValue(AVKey.DISPLAY_NAME, "Dulles Class B Middle - 1,500 to 10,000 ft. MSL.");
			this.setupDefaultMaterial(iadMid2PolyArc, Color.BLUE);
			airspaces.put("IAD-M2", iadMid2PolyArc);

			// Dulles Class B Outer 1A
			PartialCappedCylinder iadOuter1APartCyl = new PartialCappedCylinder();
			iadOuter1APartCyl.setCenter(LatLon.fromDegrees(38.934722, -77.466667));
			iadOuter1APartCyl.setAltitudes(762.0, 3048.0);
			iadOuter1APartCyl.setTerrainConforming(false, false);
			iadOuter1APartCyl.setRadii(22224.0, 27780.0);
			// TODO use methods
			iadOuter1APartCyl.setAzimuths(Angle.fromDegrees(160.0), Angle.fromDegrees(30.7));
			iadOuter1APartCyl.setValue(AVKey.DISPLAY_NAME, "Dulles Class B Outer - 2,500 to 10,000 ft. MSL.");
			this.setupDefaultMaterial(iadOuter1APartCyl, Color.BLUE);
			airspaces.put("IAD-O1A", iadOuter1APartCyl);

			// Dulles Class B Outer 1B
			PolyArc iadOuter1BPolyArc = new PolyArc();
			iadOuter1BPolyArc.setLocations(Arrays.asList(
					LatLon.fromDegrees(38.934722, -77.466667),
					LatLon.fromDegrees(39.149722, -77.302778)));
			iadOuter1BPolyArc.setAltitudes(762.0, 3048.0);
			iadOuter1BPolyArc.setRadius(22224.0);
			// TODO use method
			iadOuter1BPolyArc.setAzimuths(Angle.fromDegrees(30.7), Angle.fromDegrees(39.0));
			iadOuter1BPolyArc.setTerrainConforming(false, false);
			iadOuter1BPolyArc.setValue(AVKey.DISPLAY_NAME, "Dulles Class B Outer - 2,500 to 10,000 ft. MSL.");
			this.setupDefaultMaterial(iadOuter1BPolyArc, Color.BLUE);
			airspaces.put("IAD-O1B", iadOuter1BPolyArc);

			// Dulles Class B Outer 2A
			PartialCappedCylinder iadOuter2APartCyl = new PartialCappedCylinder();
			iadOuter2APartCyl.setCenter(LatLon.fromDegrees(38.934722, -77.466667));
			iadOuter2APartCyl.setAltitudes(1371.6, 3048.0);
			iadOuter2APartCyl.setTerrainConforming(false, false);
			iadOuter2APartCyl.setRadii(27780.0, 37040.0);
			// TODO use method
			iadOuter2APartCyl.setAzimuths(Angle.fromDegrees(makeAngle(38.722222, -77.636111, 38.651389, -77.691944)), 
					Angle.fromDegrees(337.0));
			iadOuter2APartCyl.setValue(AVKey.DISPLAY_NAME, "Dulles Class B Outer - 4,500 to 10,000 ft. MSL.");
			this.setupDefaultMaterial(iadOuter2APartCyl, Color.BLUE);
			airspaces.put("IAD-O2A", iadOuter2APartCyl);

			// Dulles Class B Outer 2B
			PartialCappedCylinder iadOuter2BPartCyl = new PartialCappedCylinder();
			iadOuter2BPartCyl.setCenter(LatLon.fromDegrees(38.934722, -77.466667));
			iadOuter2BPartCyl.setAltitudes(914.4, 3048.0);
			iadOuter2BPartCyl.setTerrainConforming(false, false);
			iadOuter2BPartCyl.setRadii(27780.0, 37040.0);
			iadOuter2BPartCyl.setAzimuths(Angle.fromDegrees(makeAngle(38.934722, -77.4666673, 38.610556, -77.568333)), 
					Angle.fromDegrees(makeAngle(38.722222, -77.636111, 38.651389, -77.691944)));
			iadOuter2BPartCyl.setValue(AVKey.DISPLAY_NAME, "Dulles Class B Outer - 3,000 to 10,000 ft. MSL.");
			this.setupDefaultMaterial(iadOuter2BPartCyl, Color.BLUE);
			airspaces.put("IAD-O2B", iadOuter2BPartCyl);

			// Dulles Class B Outer 2C
			PartialCappedCylinder iadOuter2CPartCyl = new PartialCappedCylinder();
			iadOuter2CPartCyl.setCenter(LatLon.fromDegrees(38.934722, -77.466667));
			iadOuter2CPartCyl.setAltitudes(914.4, 3048.0);
			iadOuter2CPartCyl.setTerrainConforming(false, false);
			iadOuter2CPartCyl.setRadii(27780.0, 37040.0);
			// TODO use method
			iadOuter2CPartCyl.setAzimuths(Angle.fromDegrees(337.0),
					Angle.fromDegrees(009.5));
			iadOuter2CPartCyl.setValue(AVKey.DISPLAY_NAME, "Dulles Class B Outer - 3,000 to 10,000 ft. MSL.");
			this.setupDefaultMaterial(iadOuter2CPartCyl, Color.BLUE);
			airspaces.put("IAD-O2C", iadOuter2CPartCyl);

			// Dulles Class B Outer 2D
			PolyArc iadOuter2DPolyArc = new PolyArc();
			iadOuter2DPolyArc.setLocations(Arrays.asList(
					LatLon.fromDegrees(38.934722, -77.466667),
					LatLon.fromDegrees(39.263611, -77.395833),
					LatLon.fromDegrees(39.275556, -77.347222)));
			iadOuter2DPolyArc.setAltitudes(914.4, 3048.0);
			iadOuter2DPolyArc.setRadius(27780.0);
			// TODO use method
			iadOuter2DPolyArc.setAzimuths(Angle.fromDegrees(009.5),
					Angle.fromDegrees(30.7));
			iadOuter2DPolyArc.setTerrainConforming(false, false);
			iadOuter2DPolyArc.setValue(AVKey.DISPLAY_NAME, "Dulles Class B Outer - 3,000 to 10,000 ft. MSL.");
			this.setupDefaultMaterial(iadOuter2DPolyArc, Color.BLUE);
			airspaces.put("IAD-O2D", iadOuter2DPolyArc);
			
			// Dulles Class B Outer 2E
			PartialCappedCylinder iadOuter2EPartCyl = new PartialCappedCylinder();
			iadOuter2EPartCyl.setCenter(LatLon.fromDegrees(38.934722, -77.466667));
			iadOuter2EPartCyl.setAltitudes(914.4, 3048.0);
			iadOuter2EPartCyl.setTerrainConforming(false, false);
			iadOuter2EPartCyl.setRadii(27780.0, 35188.0);
			iadOuter2EPartCyl.setAzimuths(Angle.fromDegrees(makeAngle(38.934722, -77.4666673, 38.603056, -77.418611)),
					Angle.fromDegrees(makeAngle(38.934722, -77.4666673, 38.610556, -77.568333)));
			iadOuter2EPartCyl.setValue(AVKey.DISPLAY_NAME, "Dulles Class B Outer - 3,000 to 10,000 ft. MSL.");
			this.setupDefaultMaterial(iadOuter2EPartCyl, Color.BLUE);
			airspaces.put("IAD-O2E", iadOuter2EPartCyl);
			
			//F, G, H, I
			// Dulles Class B Outer 2F
			PartialCappedCylinder iadOuter2FPartCyl = new PartialCappedCylinder();	
			iadOuter2FPartCyl.setCenter(LatLon.fromDegrees(38.934722, -77.466667));
			iadOuter2FPartCyl.setAltitudes(914.4, 3048.0);
			iadOuter2FPartCyl.setTerrainConforming(false, false);
			iadOuter2FPartCyl.setRadii(27780.0, 37040.0);
			iadOuter2FPartCyl.setAzimuths(Angle.fromDegrees(makeAngle(38.934722,-77.4666673,38.618333,-77.330833)),
					Angle.fromDegrees(makeAngle(38.934722, -77.4666673, 38.603056, -77.418611)));
			iadOuter2FPartCyl.setValue(AVKey.DISPLAY_NAME, "Dulles Class B Outer - 3,000 to 10,000 ft. MSL.");
			this.setupDefaultMaterial(iadOuter2FPartCyl, Color.BLUE);
			airspaces.put("IAD-O2F", iadOuter2FPartCyl);
			
			this.setAirspaces(airspaces.values());
		}

		public void doRemoveIadClassB() {
			Iterator<Map.Entry<String, Airspace>> iterator = airspaces
					.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, Airspace> entry = iterator.next();
				if (entry.getKey().contains("IAD")) {
					iterator.remove();
				}
			}

			this.setAirspaces(airspaces.values());
		}

		public void doDrawDcSfra() {
			// DC SFRA main
			CappedCylinder sfraCyl = new CappedCylinder();
			sfraCyl.setCenter(LatLon.fromDegrees(38.8522, -77.0378));
			sfraCyl.setRadius(55560.0);
			sfraCyl.setAltitudes(0.0, 5486.4);
			sfraCyl.setTerrainConforming(true, true);
			sfraCyl.setValue(AVKey.DISPLAY_NAME, "DC SFRA. SFC - 18,000FT MSL.");
			this.setupDefaultMaterial(sfraCyl, Color.RED);
			airspaces.put("DCSFRA-MAIN", sfraCyl);

			this.setAirspaces(airspaces.values());
		}

		public void doRemoveDcSfra() {
			Iterator<Map.Entry<String, Airspace>> iterator = airspaces
					.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, Airspace> entry = iterator.next();
				if (entry.getKey().contains("DCSFRA")) {
					iterator.remove();
				}
			}

			this.setAirspaces(airspaces.values());
		}
		
		public void doDrawHefClassD() {
			// Manassas Regional Class D
			CappedCylinder hefClassDCyl = new CappedCylinder();
			hefClassDCyl.setCenter(LatLon.fromDegrees(38.7277, -77.5188));
			hefClassDCyl.setRadius(7408.0);
			hefClassDCyl.setAltitudes(0.0, 609.5999);
			hefClassDCyl.setTerrainConforming(true, true);
			hefClassDCyl.setValue(AVKey.DISPLAY_NAME, "Manassass Regional (HEF) Class D. SFC - (not including) 2,000FT MSL.");
			this.setupDefaultMaterial(hefClassDCyl, Color.decode("#4B0082"));
			airspaces.put("HEF-D", hefClassDCyl);
			
			this.setAirspaces(airspaces.values());	
		}
		
		public void doRemoveHefClassD() {
			Iterator<Map.Entry<String, Airspace>> iterator = airspaces
					.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, Airspace> entry = iterator.next();
				if (entry.getKey().contains("HEF")) {
					iterator.remove();
				}
			}

			this.setAirspaces(airspaces.values());
		}

		public void doZoomToAirspaces() {
			BasicOrbitView view = (BasicOrbitView) this.getWwd().getView();
			Position center = Position.fromDegrees(38.8522, -77.0378, 0.0);
			Angle heading = Angle.fromDegrees(0.0);
			Angle pitch = Angle.fromDegrees(30.0);
			double zoom = 150000.0;
			view.addPanToAnimator(center, heading, pitch, zoom, true);
		}

	}

	public static void main(String[] args) {
		start("General Aviation Airspaces", AppFrame.class);
	}

	protected static Iterable<LatLon> makeLatLon(double[] src, int offset,
			int length) {
		int numCoords = (int) Math.floor(length / 2.0);
		LatLon[] dest = new LatLon[numCoords];
		for (int i = 0; i < numCoords; i++) {
			double lonDegrees = src[offset + 2 * i];
			double latDegrees = src[offset + 2 * i + 1];
			dest[i] = LatLon.fromDegrees(latDegrees, lonDegrees);
		}
		return Arrays.asList(dest);
	}

	protected static Iterable<LatLon> makeLatLon(double[] src) {
		return makeLatLon(src, 0, src.length);
	}
	
	// Official airspace description does not specify azimuths needed for polyarcs
	protected static double makeAngle(double lat1, double lon1, double lat2, double lon2) {
		double x = Math.cos(lat1)*Math.sin(lat2) - Math.sin(lat1)*Math.cos(lat2) * Math.cos(lon2-lon1);
		double y = Math.sin(lon2-lon1)*Math.cos(lat2);
		
		double theta = Math.atan2(y, x);
		double degrees = Math.toDegrees(theta);
		
		return (degrees < 0.0 ? degrees+360.0 : degrees);
	}

}
