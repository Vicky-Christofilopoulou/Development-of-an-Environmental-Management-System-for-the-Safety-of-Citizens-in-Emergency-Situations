package Maps;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.WaypointRenderer;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

public class FancyWaypointRenderer implements WaypointRenderer<MyWaypoint> {

    private final Map<Color, BufferedImage> map = new HashMap<>();
    private BufferedImage origImage;

    public FancyWaypointRenderer() {
        URL resource = getClass().getResource("waypoint_white.png");

        try {
            origImage = ImageIO.read(resource);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private BufferedImage convert(BufferedImage loadImg, Color newColor) {
        int w = loadImg.getWidth();
        int h = loadImg.getHeight();
        BufferedImage imgOut = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        BufferedImage imgColor = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = imgColor.createGraphics();
        g.setColor(newColor);
        g.fillRect(0, 0, w + 1, h + 1);
        g.dispose();

        Graphics2D graphics = imgOut.createGraphics();
        graphics.drawImage(loadImg, 0, 0, null);
        graphics.setComposite(MultiplyComposite.Default);
        graphics.drawImage(imgColor, 0, 0, null);
        graphics.dispose();

        return imgOut;
    }

    @Override
    public void paintWaypoint(Graphics2D g, JXMapViewer viewer, MyWaypoint w) {
        g = (Graphics2D) g.create();

        BufferedImage myImg = map.computeIfAbsent(w.getColor(), color -> convert(origImage, color));

        Point2D point = viewer.getTileFactory().geoToPixel(w.getPosition(), viewer.getZoom());

        int x = (int) point.getX();
        int y = (int) point.getY();

        // Draw the filled circle around the waypoint with the specified radius
        double circleRadius = w.getCircleRadius();
        Color circleColor = w.getColorCircle();
        g.setColor(circleColor);
        g.fill(new Ellipse2D.Double(x - circleRadius, y - circleRadius, 2 * circleRadius, 2 * circleRadius));

        // Draw the waypoint image
        g.drawImage(myImg, x - myImg.getWidth() / 2, y - myImg.getHeight(), null);

        g.setColor(Color.black);
        String label = w.getLabel();

        FontMetrics metrics = g.getFontMetrics();
        int tw = metrics.stringWidth(label);
        int th = 1 + metrics.getAscent();

        g.drawString(label, x - tw / 2, y + th - myImg.getHeight());

        g.dispose();
    }
}
