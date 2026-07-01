package com.echostudy.utils;

import java.math.BigDecimal;

public class LocationUtils {

    private static final double EARTH_RADIUS_METERS = 6371000.0;

    private LocationUtils() {
    }

    public static double distanceMeters(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        double latitude1 = Math.toRadians(lat1.doubleValue());
        double latitude2 = Math.toRadians(lat2.doubleValue());
        double deltaLat = Math.toRadians(lat2.subtract(lat1).doubleValue());
        double deltaLon = Math.toRadians(lon2.subtract(lon1).doubleValue());
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(latitude1) * Math.cos(latitude2)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_METERS * c;
    }
}
