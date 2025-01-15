package com.example.uservol2;

import java.util.Random;

/* HELPFUL CLASS: Randomly selecting either the android_1.xml or android_2.xml */
public class XmlSelect {
    public static String getXmlName() {
        Random r = new Random();
        int rnum = r.nextInt(2)+1; //Gives 1 or 2

        return "android_" + rnum + ".xml";
    }

}
