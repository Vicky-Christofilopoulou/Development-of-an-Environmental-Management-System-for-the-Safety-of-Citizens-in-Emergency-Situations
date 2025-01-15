package Distance;

import java.lang.*;

public class Distance {
    public static class Point{
        private double lat;
        private double lon;

        public Point(double lat,double lon )
        {
            this.lat=lat;
            this.lon=lon;
        }
        public double getLan()
        {
            return lat;
        }

        public double getLon()
        {
            return lon;
        }
    }
    public static void main (String[] args) throws java.lang.Exception
    {}

    public static double calculate(Point android,Point Iot1,Point Iot2)
    {
        double dist;
        if( Iot2 == null)// if 1 iot and 1 android
        {
            dist = distance(android.getLan(),android.getLon(),Iot1.getLan(),Iot1.getLon(),"K");
            return dist;
        }
        else //2 iot and 1 android
        {
            double lanCentral = (Iot1.getLan()+ Iot2.getLan())/ 2;
            double lonCentral = (Iot1.getLon()+ Iot2.getLon())/ 2 ;
            Point p = new Point(lanCentral,lonCentral);
            dist = distance(android.getLan(),android.getLon(),p.getLan(),p.getLon(),"K");
            return dist;
        }

    }

    private static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        }
        else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            if (unit.equals("K")) {
                dist = dist * 1.609344;
            } else if (unit.equals("N")) {
                dist = dist * 0.8684;
            }
            return (dist);
        }
    }
}

