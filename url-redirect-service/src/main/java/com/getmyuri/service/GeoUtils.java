package com.getmyuri.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getmyuri.model.Location;

public class GeoUtils {

        private static final Logger logger = LoggerFactory.getLogger(GeoUtils.class);

        public static boolean isWithinRadius(Location targetLoc, double userLat, double userLon,
                        double radiusMeters) {
                double distance = haversine(targetLoc, userLat, userLon);
                boolean within = (distance <= radiusMeters);

                logger.info("Checking radius: target=({}, {}), user=({}, {}), radius={}m → distance={}m → within={}",
                                targetLoc.getCoordinates().get(1), targetLoc.getCoordinates().get(0),
                                userLat, userLon,
                                radiusMeters, String.format("%.2f", distance), within);

                return within;
        }

        private static double haversine(Location targetLoc, double lat2, double lon2) {
                final int R = 6371000;

                double lat1 = targetLoc.getCoordinates().get(1);
                double lon1 = targetLoc.getCoordinates().get(0);

                double dLat = Math.toRadians(lat2 - lat1);
                double dLon = Math.toRadians(lon2 - lon1);

                double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                                + Math.cos(Math.toRadians(lat1))
                                                * Math.cos(Math.toRadians(lat2))
                                                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
                double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
                double distance = R * c;

                logger.debug("Haversine result: lat1={}, lon1={}, lat2={}, lon2={}, distance={}m",
                                lat1, lon1, lat2, lon2, String.format("%.2f", distance));
                return R * c;
        }

}
