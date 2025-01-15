package Maps;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Set;

public class WaypointToolTipHandler implements MouseMotionListener {

    private final JXMapViewer mapViewer;
    private final Set<MyWaypoint> androidWaypoints;
    private final Set<MyWaypoint> iotWaypoints;

    public WaypointToolTipHandler(JXMapViewer mapViewer, Set<MyWaypoint> androidWaypoints, Set<MyWaypoint> iotWaypoints) {
        this.mapViewer = mapViewer;
        this.androidWaypoints = androidWaypoints;
        this.iotWaypoints = iotWaypoints;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        GeoPosition position = mapViewer.convertPointToGeoPosition(e.getPoint());

        for (MyWaypoint androidWaypoint : androidWaypoints) {
            if (androidWaypoint.getPosition().equals(position)) {
                showToolTip(androidWaypoint);
                return;
            }
        }

        for (MyWaypoint iotWaypoint : iotWaypoints) {
            if (iotWaypoint.getPosition().equals(position)) {
                showToolTip(iotWaypoint);
                return;
            }
        }

        // If no waypoint is found at the mouse position, hide the tooltip
        mapViewer.setToolTipText(null);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // Empty implementation, as we don't need to handle mouse dragging
    }

    private void showToolTip(MyWaypoint waypoint) {
        // Customize this part to display the information you want in the tooltip
        mapViewer.setToolTipText("Device ID: " + waypoint.getLabel() + ", Color: " + waypoint.getColor());
    }
}
