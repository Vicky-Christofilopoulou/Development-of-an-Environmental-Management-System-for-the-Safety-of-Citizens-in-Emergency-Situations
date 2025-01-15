package Maps;

import java.awt.*;
import java.io.File;
import java.util.*;

import javax.swing.*;
import javax.swing.event.MouseInputListener;

import org.jxmapviewer.JXMapKit;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.VirtualEarthTileFactoryInfo;
import org.jxmapviewer.cache.FileBasedLocalCache;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCenter;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.jxmapviewer.viewer.WaypointPainter;
import java.awt.geom.Point2D;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JToolTip;

import static java.lang.Thread.sleep;

public class Maps {

    private static Set<MyWaypoint> androidWaypoints = new HashSet<MyWaypoint>();
    private static Set<MyWaypoint> iotWaypoints = new HashSet<MyWaypoint>();
    private static Set<MyWaypoint> iotWaypoints2 = new HashSet<MyWaypoint>();
    private static JXMapViewer mapViewer;
    static final JXMapKit jXMapKit = new JXMapKit();
    private static  List<GeoPosition> track = new ArrayList<>();

    private static WaypointPainter<MyWaypoint> waypointPainter = new WaypointPainter<MyWaypoint>();

    private static Map<String, Double> dangerThresholds = new HashMap<>();
    static {
        //Thresholds for every IOT
        dangerThresholds.put("smoke", 0.14);
        dangerThresholds.put("gas",  9.15);
        dangerThresholds.put("temperature", 50.0);
        dangerThresholds.put("uv", 6.0);
    }

    private static int calculateDangerLevel(double smoke, double gas, double temperature, double uv) {
        if (smoke > dangerThresholds.get("smoke") && gas > dangerThresholds.get("gas")) {
            return 3;
            //2nd periptosi -> den valame not smoke & gas giati tha embaine sto proigoumeno if
        } else if (temperature > dangerThresholds.get("temperature") && uv > dangerThresholds.get("uv")) {
            return 2;
        } else if (gas > dangerThresholds.get("gas")) {
            return 3;
        } else if (smoke > dangerThresholds.get("smoke") && gas > dangerThresholds.get("gas")
                && temperature > dangerThresholds.get("temperature") && uv > dangerThresholds.get("uv")) {
            return 3;
        } else {
            return 1;
        }
    }

    public Maps() throws InterruptedException {
        // Initialize mapViewer and set tileFactory
        initializeMapViewer();

        // Display the viewer in a JFrame
        JFrame frame = new JFrame("Projet");
        frame.getContentPane().add(mapViewer);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);


        TileFactoryInfo info = new OSMTileFactoryInfo();
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        jXMapKit.setTileFactory(tileFactory);
    }

    public static void initializeMapViewer() {
        // Create a TileFactoryInfo for Virtual Earth
        TileFactoryInfo info = new VirtualEarthTileFactoryInfo(VirtualEarthTileFactoryInfo.MAP);
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);

        // Setup local file cache
        File cacheDir = new File(System.getProperty("user.home") + File.separator + ".jxmapviewer2");
        tileFactory.setLocalCache(new FileBasedLocalCache(cacheDir, false));

        // Setup JXMapViewer
        mapViewer = new JXMapViewer();
        mapViewer.setTileFactory(tileFactory);

        // Set the focus
//        mapViewer.setZoom(4);
        GeoPosition campus = new GeoPosition(37.96846, 23.76671);
        mapViewer.setAddressLocation(campus);

        // Add interactions
        MouseInputListener mia = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(mia);
        mapViewer.addMouseMotionListener(mia);
        mapViewer.addMouseListener(new CenterMapListener(mapViewer));
        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCenter(mapViewer));
        mapViewer.addKeyListener(new PanKeyListener(mapViewer));

        // Create waypoint painters for Android and IoT
        waypointPainter = new WaypointPainter<MyWaypoint>();
        waypointPainter.setRenderer(new FancyWaypointRenderer());

        ToolTipManager.sharedInstance().registerComponent(mapViewer);
        mapViewer.setOverlayPainter(waypointPainter);

        mapViewer.addMouseMotionListener(new WaypointToolTipHandler(mapViewer, androidWaypoints, iotWaypoints));
//        mapViewer.setOverlayPainter(androidWaypointPainter);
    }

    public static void addAndroidpoint(Double device_id, double lat, double lon) {
        for (MyWaypoint existingWaypoint : androidWaypoints) {
            if (existingWaypoint.getLabel().equals(String.valueOf(device_id))) {

                // Update the existing marker's position
                GeoPosition gp = new GeoPosition(lat, lon);
                existingWaypoint.setPosition(gp);

                // Return the updated marker
                final JToolTip tooltip = new JToolTip();
                tooltip.setTipText("<html>Device Id: " + device_id + "<br>Latitude: "+ lat + "<br>Longitude: "+ lon + "</html>");
                tooltip.setComponent(mapViewer);
                mapViewer.add(tooltip); // Add tooltip to mapViewer

                existingWaypoint.setTooltipText(tooltip);

                mapViewer.addMouseMotionListener(new MouseMotionListener() {
                    @Override
                    public void mouseDragged(MouseEvent e) {
                        // ignore
                    }

                    @Override
                    public void mouseMoved(MouseEvent e) {
                        // convert to world bitmap
                        Point2D worldPos = mapViewer.getTileFactory().geoToPixel(gp, mapViewer.getZoom());

                        // convert to screen
                        Rectangle rect = mapViewer.getViewportBounds();
                        int sx = (int) worldPos.getX() - rect.x;
                        int sy = (int) worldPos.getY() - rect.y;
                        Point screenPos = new Point(sx, sy);

                        // check if near the mouse
                        if (screenPos.distance(e.getPoint()) < 20) {
                            screenPos.x -= tooltip.getWidth() / 2;

                            tooltip.setLocation(screenPos);
                            tooltip.setVisible(true);
                        } else {
                            tooltip.setVisible(false);
                        }
                    }
                });

                mapViewer.repaint(); // Repaint the map to reflect the change
                return;
            }
        }
        MyWaypoint newWaypoint = new MyWaypoint(String.valueOf(device_id), Color.pink, new GeoPosition(lat, lon), 0, null);
        GeoPosition gp = new GeoPosition(lat, lon);
        androidWaypoints.add(newWaypoint);
        updateWaypointPainter(); // Update the waypoints in the painter

        final JToolTip tooltip = new JToolTip();
        tooltip.setTipText("<html>Device Id: " + device_id + "<br>Latitude: "+ lat + "<br>Longitude: "+ lon + "</html>");
        tooltip.setComponent(mapViewer);
        mapViewer.add(tooltip); // Add tooltip to mapViewer

        newWaypoint.setTooltipText(tooltip);

        mapViewer.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                // ignore
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                // convert to world bitmap
                Point2D worldPos = mapViewer.getTileFactory().geoToPixel(gp, mapViewer.getZoom());

                // convert to screen
                Rectangle rect = mapViewer.getViewportBounds();
                int sx = (int) worldPos.getX() - rect.x;
                int sy = (int) worldPos.getY() - rect.y;
                Point screenPos = new Point(sx, sy);

                // check if near the mouse
                if (screenPos.distance(e.getPoint()) < 20) {
                    screenPos.x -= tooltip.getWidth() / 2;

                    tooltip.setLocation(screenPos);
                    tooltip.setVisible(true);
                } else {
                    tooltip.setVisible(false);
                }
            }
        });
    }

    public static void addIoTpoint(Double device_id, double lat, double lon, boolean iot2_flag, double battery, double smoke, double gas, double temperature, double UV) {
        int riskLevel =1;
        for (MyWaypoint existingWaypoint : iotWaypoints) {
            if (existingWaypoint.getLabel().equals(String.valueOf(device_id))) {

                // Update the existing marker's position
                GeoPosition gp = new GeoPosition(lat, lon);
                existingWaypoint.setPosition(gp);

                // Calculate risk level
                riskLevel = calculateDangerLevel(smoke, gas, temperature, UV);
                Color circleCol = determineCircleColor( smoke, gas, temperature, UV);
                existingWaypoint.setdanger(riskLevel);

                // Change the color based on risk level
                if (riskLevel == 1) {
                    existingWaypoint.setColor(Color.blue);
                    existingWaypoint.setCircleRadius(15);
                    existingWaypoint.setColorCircle(circleCol);
                    deleteRectangle();
                } else if (riskLevel == 2) {
                    existingWaypoint.setColor(Color.yellow);
                    existingWaypoint.setCircleRadius(15);
                    existingWaypoint.setColorCircle(circleCol);
                }
                else {
                    existingWaypoint.setColor(Color.red);
                    existingWaypoint.setCircleRadius(15);
                    existingWaypoint.setColorCircle(circleCol);
                }

                // Update the info window content
                final JToolTip tooltip = new JToolTip();
                tooltip.setTipText("<html>Device Id: " + device_id + "<br>Latitude: "+ lat + "<br>Longitude: "+ lon + "<br>Battery: " + battery + "<br>Smoke: " +smoke + "<br>Gas: " + gas + "<br>Temperature: " + temperature + "<br>UV :" + UV + "</html>");
                tooltip.setComponent(mapViewer);
                mapViewer.add(tooltip); // Add tooltip to mapViewer

                existingWaypoint.setTooltipText(tooltip);

                mapViewer.addMouseMotionListener(new MouseMotionListener() {
                    @Override
                    public void mouseDragged(MouseEvent e) {
                        // ignore
                    }

                    @Override
                    public void mouseMoved(MouseEvent e) {
                        // convert to world bitmap
                        Point2D worldPos = mapViewer.getTileFactory().geoToPixel(gp, mapViewer.getZoom());

                        // convert to screen
                        Rectangle rect = mapViewer.getViewportBounds();
                        int sx = (int) worldPos.getX() - rect.x;
                        int sy = (int) worldPos.getY() - rect.y;
                        Point screenPos = new Point(sx, sy);

                        // check if near the mouse
                        if (screenPos.distance(e.getPoint()) < 20) {
                            screenPos.x -= tooltip.getWidth() / 2;

                            tooltip.setLocation(screenPos);
                            tooltip.setVisible(true);
                        } else {
                            tooltip.setVisible(false);
                        }
                    }
                });

                //check for second iot
                if (iot2_flag && riskLevel == 2 || riskLevel == 3) {
                    Set<MyWaypoint> iotWaypoints2 = Maps.getIotWaypoints2();
                    for (MyWaypoint waypoint : iotWaypoints2) {
                        GeoPosition coordinates = waypoint.getPosition();
                        double lat2 = coordinates.getLatitude();
                        double lon2 = coordinates.getLongitude();
                        int risk2 = waypoint.getdanger();

//                        System.out.println("Latitude: " + lat2);
//                        System.out.println("Longitude: " + lon2);
//                        System.out.println("risk2: " + risk2);

                        if (risk2 == 2 || risk2 == 3)   //second iot is in danger
                        {
                            createRectangle(lat, lon, lat2, lon2);
                        } else {
                            System.out.println("Second IoT in not in danger.");
                        }

                    }
                }
                // Return the updated marker
                mapViewer.repaint();
                return;
            }
        }

        // Create a new marker if it doesn't exist
        riskLevel = calculateDangerLevel(smoke, gas, temperature, UV);
        Color circleCol = determineCircleColor( smoke, gas, temperature, UV);

        GeoPosition gp = new GeoPosition(lat, lon);
        final JToolTip tooltip = new JToolTip();
        tooltip.setTipText("<html>Device Id: " + device_id + "<br>Latitude: "+ lat + "<br>Longitude: "+ lon + "<br>Battery: " + battery + "<br>Smoke: " +smoke + "<br>Gas: " + gas + "<br>Temperature: " + temperature + "<br>UV :" + UV + "</html>");
        tooltip.setComponent(mapViewer);
        mapViewer.add(tooltip); // Add tooltip to mapViewer

        mapViewer.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                // ignore
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                // convert to world bitmap
                Point2D worldPos = mapViewer.getTileFactory().geoToPixel(gp, mapViewer.getZoom());

                // convert to screen
                Rectangle rect = mapViewer.getViewportBounds();
                int sx = (int) worldPos.getX() - rect.x;
                int sy = (int) worldPos.getY() - rect.y;
                Point screenPos = new Point(sx, sy);

                // check if near the mouse
                if (screenPos.distance(e.getPoint()) < 20) {
                    screenPos.x -= tooltip.getWidth() / 2;

                    tooltip.setLocation(screenPos);
                    tooltip.setVisible(true);
                } else {
                    tooltip.setVisible(false);
                }
            }
        });

        // Change the color based on risk level
        if (riskLevel == 1)
        {
            MyWaypoint newWaypoint = new MyWaypoint(String.valueOf(device_id), Color.blue, gp,15, circleCol);
            newWaypoint.setdanger(riskLevel);
            iotWaypoints.add(newWaypoint);
            updateWaypointPainter();
            mapViewer.repaint(); // Repaint the map to reflect the changes
            newWaypoint.setTooltipText(tooltip);
        } else if (riskLevel == 2)
        {
            MyWaypoint newWaypoint = new MyWaypoint(String.valueOf(device_id), Color.yellow, gp, 15, circleCol);
            newWaypoint.setdanger(riskLevel);
            iotWaypoints.add(newWaypoint);
            updateWaypointPainter();
            newWaypoint.setTooltipText(tooltip);
            mapViewer.repaint(); // Repaint the map to reflect the changes
        }
        else {
            MyWaypoint newWaypoint = new MyWaypoint(String.valueOf(device_id), Color.red, gp, 15, circleCol);
            newWaypoint.setdanger(riskLevel);
            iotWaypoints.add(newWaypoint);
            updateWaypointPainter();
            newWaypoint.setTooltipText(tooltip);
            mapViewer.repaint(); // Repaint the map to reflect the changes

        }

        if (iot2_flag && riskLevel == 2 || riskLevel == 3) {
            Set<MyWaypoint> iotWaypoints2 = Maps.getIotWaypoints2();
            //System.out.println("HERERERRERERR");
            for (MyWaypoint waypoint : iotWaypoints2) {
                GeoPosition coordinates = waypoint.getPosition();
                double lat2 = coordinates.getLatitude();
                double lon2 = coordinates.getLongitude();
                int risk2 = waypoint.getdanger();

//                System.out.println("Latitude: " + lat2);
//                System.out.println("Longitude: " + lon2);
//                System.out.println("risk2: " + risk2);

                if (risk2 == 2 || risk2 == 3)   //second iot is in danger
                {
                    createRectangle(lat,lon,lat2,lon2);
                }
                else{
                    System.out.println("Second IoT in not in danger.");
                }

            }
        }

    }

    public static Set<MyWaypoint> getIotWaypoints1() {
        return iotWaypoints;
    }

    public static Set<MyWaypoint> getIotWaypoints2() {
        return iotWaypoints2;
    }


    public static void addIoTpoint2(Double device_id, double lat, double lon, boolean iot1_flag, double battery, double smoke, double gas, double temperature, double UV) {
        for (MyWaypoint existingWaypoint : iotWaypoints2) {
            if (existingWaypoint.getLabel().equals(String.valueOf(device_id))) {

                // Update the existing marker's position
                GeoPosition gp = new GeoPosition(lat, lon);
                existingWaypoint.setPosition(gp);

                // Calculate risk level
                int riskLevel = calculateDangerLevel(smoke, gas, temperature, UV);
                Color circleCol = determineCircleColor( smoke, gas, temperature, UV);
                existingWaypoint.setdanger(riskLevel);


                // Change the color based on risk level
                if (riskLevel == 1) {
                    existingWaypoint.setColor(Color.blue);
                    existingWaypoint.setCircleRadius(15);
                    existingWaypoint.setColorCircle(circleCol);
                    deleteRectangle();
                } else if (riskLevel == 2) {
                    existingWaypoint.setColor(Color.yellow);
                    existingWaypoint.setCircleRadius(15);
                    existingWaypoint.setColorCircle(circleCol);
                }
                else {
                    existingWaypoint.setColor(Color.red);
                    existingWaypoint.setCircleRadius(15);
                    existingWaypoint.setColorCircle(circleCol);
                }

                // Update the info window content
                final JToolTip tooltip = new JToolTip();
                tooltip.setTipText("<html>Device Id: " + device_id + "<br>Latitude: "+ lat + "<br>Longitude: "+ lon + "<br>Battery: " + battery + "<br>Smoke: " +smoke + "<br>Gas: " + gas + "<br>Temperature: " + temperature + "<br>UV :" + UV + "</html>");
                tooltip.setComponent(mapViewer);
                mapViewer.add(tooltip); // Add tooltip to mapViewer

                existingWaypoint.setTooltipText(tooltip);

                mapViewer.addMouseMotionListener(new MouseMotionListener() {
                    @Override
                    public void mouseDragged(MouseEvent e) {
                        // ignore
                    }

                    @Override
                    public void mouseMoved(MouseEvent e) {
                        // convert to world bitmap
                        Point2D worldPos = mapViewer.getTileFactory().geoToPixel(gp, mapViewer.getZoom());

                        // convert to screen
                        Rectangle rect = mapViewer.getViewportBounds();
                        int sx = (int) worldPos.getX() - rect.x;
                        int sy = (int) worldPos.getY() - rect.y;
                        Point screenPos = new Point(sx, sy);

                        // check if near the mouse
                        if (screenPos.distance(e.getPoint()) < 20) {
                            screenPos.x -= tooltip.getWidth() / 2;

                            tooltip.setLocation(screenPos);
                            tooltip.setVisible(true);
                        } else {
                            tooltip.setVisible(false);
                        }
                    }
                });

                //check for second iot
                if (iot1_flag && riskLevel == 2 || riskLevel == 3) {
                    Set<MyWaypoint> iotWaypoints = Maps.getIotWaypoints1();
                    for (MyWaypoint waypoint : iotWaypoints) {
                        GeoPosition coordinates = waypoint.getPosition();
                        double lat2 = coordinates.getLatitude();
                        double lon2 = coordinates.getLongitude();
                        int risk2 = waypoint.getdanger();

//                        System.out.println("Latitude1: " + lat);
//                        System.out.println("Longitude1: " + lon);
//                        System.out.println("Latitude: " + lat2);
//                        System.out.println("Longitude: " + lon2);
//                        System.out.println("risk2: " + risk2);

                        if (risk2 == 2 || risk2 == 3)   //second iot is in danger
                        {
                            createRectangle(lat, lon, lat2, lon2);
                        } else {
                            System.out.println("First IoT in not in danger.");
                        }

                    }
                }

                // Return the updated marker
                mapViewer.repaint(); // Repaint the map to reflect the change
                return;

            }
        }

        // Create a new marker if it doesn't exist
        int riskLevel = calculateDangerLevel(smoke, gas, temperature, UV);
        Color circleCol = determineCircleColor( smoke, gas, temperature, UV);

        GeoPosition gp = new GeoPosition(lat, lon);
        final JToolTip tooltip = new JToolTip();
        tooltip.setTipText("<html>Device Id: " + device_id + "<br>Latitude: "+ lat + "<br>Longitude: "+ lon + "<br>Battery: " + battery + "<br>Smoke: " +smoke + "<br>Gas: " + gas + "<br>Temperature: " + temperature + "<br>UV :" + UV + "</html>");
        tooltip.setComponent(mapViewer);
        mapViewer.add(tooltip); // Add tooltip to mapViewer

        mapViewer.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                // ignore
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                // convert to world bitmap
                Point2D worldPos = mapViewer.getTileFactory().geoToPixel(gp, mapViewer.getZoom());

                // convert to screen
                Rectangle rect = mapViewer.getViewportBounds();
                int sx = (int) worldPos.getX() - rect.x;
                int sy = (int) worldPos.getY() - rect.y;
                Point screenPos = new Point(sx, sy);

                // check if near the mouse
                if (screenPos.distance(e.getPoint()) < 20) {
                    screenPos.x -= tooltip.getWidth() / 2;

                    tooltip.setLocation(screenPos);
                    tooltip.setVisible(true);
                } else {
                    tooltip.setVisible(false);
                }
            }
        });


        // Change the color based on risk level
        if (riskLevel == 1)
        {
            MyWaypoint newWaypoint = new MyWaypoint(String.valueOf(device_id), Color.blue, gp,15, circleCol);
            newWaypoint.setdanger(riskLevel);
            iotWaypoints2.add(newWaypoint);
            updateWaypointPainter();
            mapViewer.repaint(); // Repaint the map to reflect the changes
            newWaypoint.setTooltipText(tooltip);
        } else if (riskLevel == 2)
        {
            MyWaypoint newWaypoint = new MyWaypoint(String.valueOf(device_id), Color.yellow, gp, 15, circleCol);
            newWaypoint.setdanger(riskLevel);
            iotWaypoints2.add(newWaypoint);
            updateWaypointPainter();
            newWaypoint.setTooltipText(tooltip);
            mapViewer.repaint(); // Repaint the map to reflect the changes
        }
        else {
            MyWaypoint newWaypoint = new MyWaypoint(String.valueOf(device_id), Color.red, gp, 15, circleCol);
            newWaypoint.setdanger(riskLevel);
            iotWaypoints2.add(newWaypoint);
            updateWaypointPainter();
            newWaypoint.setTooltipText(tooltip);
            mapViewer.repaint(); // Repaint the map to reflect the changes
        }

        if (iot1_flag && riskLevel == 2 || riskLevel == 3) {
            Set<MyWaypoint> iotWaypoints2 = Maps.getIotWaypoints2();
            //System.out.println("HERERERRERERR");
            for (MyWaypoint waypoint : iotWaypoints2) {
                GeoPosition coordinates = waypoint.getPosition();
                double lat2 = coordinates.getLatitude();
                double lon2 = coordinates.getLongitude();
                int risk2 = waypoint.getdanger();

//                System.out.println("Latitude1: " + lat);
//                System.out.println("Longitude1: " + lon);
//                System.out.println("Latitude: " + lat2);
//                System.out.println("Longitude: " + lon2);
//                System.out.println("risk2: " + risk2);

                if (risk2 == 2 || risk2 == 3)   //second iot is in danger
                {
                    createRectangle(lat, lon, lat2, lon2);
                } else {
                    System.out.println("First IoT in not in danger.");
                }

            }
        }
    }

    private static void updateWaypointPainter() {
        Set<MyWaypoint> mergedWaypoints = new HashSet<>(androidWaypoints);
        mergedWaypoints.addAll(iotWaypoints);
        mergedWaypoints.addAll(iotWaypoints2);
        waypointPainter.setWaypoints(mergedWaypoints);

    }

    //Check if the iot device is on/off
    private static Color determineCircleColor( double smoke, double gas, double temperature, double UV) {
        // Determine the color based on device activity
        if (smoke == -100 && gas == -100 && temperature == -100 && UV == -100) {
            // Device is inactive (use red color)
            return Color.red;
        } else {
            // Device is inactive (use red color)
            return Color.green;
        }
    }

    public static void  createRectangle(double lat,double lon,double lat2, double lon2)
    {
        //Delete the previous rectangle
        track.clear();

        // Add GeoPosition instances to the list
        GeoPosition position2 = new GeoPosition(lat2, lon2); //b
        GeoPosition position4 = new GeoPosition(lat, lon);//d

        GeoPosition position1 = new GeoPosition(lat2, lon); //a
        GeoPosition position3 = new GeoPosition(lat, lon2); //c

        track.addAll(Arrays.asList(position1,position2,position3,position4,position1));

        // Create a RoutePainter with the track
        RoutePainter routePainter = new RoutePainter(track);

        // Create a compound painter that uses both the route-painter and the waypoint-painter
        List<org.jxmapviewer.painter.Painter<JXMapViewer>> painters = new ArrayList<Painter<JXMapViewer>>();
        painters.add(routePainter);
        painters.add(waypointPainter);

        CompoundPainter<JXMapViewer> painter = new CompoundPainter<JXMapViewer>(painters);
        mapViewer.setOverlayPainter(painter);

    }

    public static void  deleteRectangle()
    {
        //Delete the previous rectangle
        track.clear();

        // Create a compound painter that uses both the route-painter and the waypoint-painter
        List<org.jxmapviewer.painter.Painter<JXMapViewer>> painters = new ArrayList<Painter<JXMapViewer>>();
        painters.add(waypointPainter);

        CompoundPainter<JXMapViewer> painter = new CompoundPainter<JXMapViewer>(painters);
        mapViewer.setOverlayPainter(painter);


    }
}
