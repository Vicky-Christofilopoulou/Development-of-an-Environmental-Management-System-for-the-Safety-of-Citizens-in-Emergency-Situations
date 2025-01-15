package Maps;

import java.awt.Color;

import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;

import javax.swing.*;

/**
 * A waypoint that also has a color and a label
 * @author Martin Steiger
 */
public class MyWaypoint extends DefaultWaypoint
{
    private String label;
    private Color color;
    private JToolTip tooltipText;
    private double circleRadius;
    private Color circleCol;
    private int danger;


    /**
     * Constructor for MyWaypoint
     * @param label the text
     * @param color the color
     * @param coord the coordinate
     */
    public MyWaypoint(String label, Color color, GeoPosition coord,double circleRadius, Color circleCol)
    {
        super(coord);
        this.label = label;
        this.color = color;
        this.circleRadius = circleRadius;
        this.circleCol = circleCol;
        this.danger = 0;

    }

    /**
     * @return the label text
     */
    public String getLabel()
    {
        return label;
    }

    /**
     * @return the color
     */
    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * Get the tooltip text
     * @return The tooltip text
     */
    public JToolTip getTooltipText() {
        return tooltipText;
    }

    public void setTooltipText(JToolTip tooltipText)
    {

        this.tooltipText = tooltipText;
    }

    public Color getColorCircle() {
        return circleCol;
    }

    public void setColorCircle(Color color) {
        this.circleCol = color;
    }

    public double getCircleRadius() {
        return circleRadius;
    }

    public void setCircleRadius(double radius) {
        this.circleRadius = radius;
    }

    public int getdanger()
    {
        return danger;
    }

    /**
     * @return the color
     */
    public void setdanger(int danger) {
        this.danger =danger;
    }
}
