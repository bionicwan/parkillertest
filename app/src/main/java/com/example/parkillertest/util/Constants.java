package com.example.parkillertest.util;

/**
 * Created by JuanCarlos on 20/02/16.
 */
public final class Constants {
    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final String PACKAGE_NAME = "com.example.parkillertest";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String ADDRESS_RESULT_DATA_KEY = PACKAGE_NAME + ".ADDRESS_RESULT_DATA_KEY";
    public static final String ADDRESS_LOCATION_DATA_EXTRA = PACKAGE_NAME + ".ADDRESS_LOCATION_DATA_EXTRA";
    public static final String LOCATION_INTENT_ACTION = PACKAGE_NAME + ".LOCATION_RESULT";
    public static final String LOCATION_RESULT_DATA_KEY = PACKAGE_NAME + ".LOCATION_RESULT_DATA_KEY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";

    public static final int DISTANCE_200M = 200;
    public static final int DISTANCE_100M = 100;
    public static final int DISTANCE_50M = 50;
    public static final int DISTANCE_10M = 10;

    public static final String KEY_DESTINATION_LAT = "dest_lat";
    public static final String KEY_DESTINATION_LON = "dest_lon";
    public static final String KEY_LATEST_DISTANCE = "latest_distance";
    public static final String KEY_200M_MESSAGE_SHOWED = "message_200m_showed";
    public static final String KEY_100M_MESSAGE_SHOWED = "message_100m_showed";
    public static final String KEY_50M_MESSAGE_SHOWED = "message_50m_showed";
    public static final String KEY_10M_MESSAGE_SHOWED = "message_10m_showed";
    public static final String KEY_ARRIVED_MESSAGE_SHOWED = "message_arrived_showed";
}
