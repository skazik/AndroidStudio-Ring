package com.example.ring_sergie.test1;

import java.util.HashMap;
import java.util.Objects;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    public static final String SET_PUBLIC_KEY = "SET_PUBLIC_KEY";
    public static final String GET_PUBLIC_PAYLOAD = "GET_PUBLIC_PAYLOAD";
    public static final String GET_NETWORKS = "GET_NETWORKS";
    public static final String SET_NETWORK = "SET_NETWORK";
    public static final String GET_PAIRING_STATE = "GET_PAIRING_STATE";
    public static final String SET_LANGUAGE = "SET_LANGUAGE";
    public static final String SET_ZIPCODE = "SET_ZIPCODE";
    public static final String GET_WIFI_STATUS = "GET_WIFI_STATUS";
    public static final String GET_SSID_WIFI = "GET_SSID_WIFI";
    public static final String GET_SERIAL_NUMBER = "GET_SERIAL_NUMBER";
    public static final String GET_MAC_ADDRESS = "GET_MAC_ADDRESS";

    static {
        // Sample Services.
        attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        // Sample Characteristics.
        attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
        attributes.put("0000FACE-0000-1000-8000-00805F9B34FB", "RING_PAIRING_SVC");

        attributes.put("0000FACE-0000-1000-8000-00805F9B3501", "SET_PUBLIC_KEY");
        attributes.put("0000FACE-0000-1000-8000-00805F9B3502", "GET_PUBLIC_PAYLOAD");
        attributes.put("0000FACE-0000-1000-8000-00805F9B3503", "GET_NETWORKS");
        attributes.put("0000FACE-0000-1000-8000-00805F9B3504", "SET_NETWORK");
        attributes.put("0000FACE-0000-1000-8000-00805F9B3507", "GET_PAIRING_STATE");
        attributes.put("0000FACE-0000-1000-8000-00805F9B3508", "SET_LANGUAGE");
        attributes.put("0000FACE-0000-1000-8000-00805F9B3509", "SET_ZIPCODE");
        attributes.put("0000FACE-0000-1000-8000-00805F9B350A", "GET_WIFI_STATUS");
        attributes.put("0000FACE-0000-1000-8000-00805F9B350B", "GET_SSID_WIFI");
        attributes.put("0000FACE-0000-1000-8000-00805F9B350C", "GET_SERIAL_NUMBER");
        attributes.put("0000FACE-0000-1000-8000-00805F9B350D", "GET_MAC_ADDRESS");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid.toUpperCase());
        return name == null ? defaultName : name;
    }


    public static String lookup(String uuid) {
        return attributes.get(uuid.toUpperCase());
    }


    public static String uuidByVal(String val) {
        for (HashMap.Entry<String, String> entry : attributes.entrySet()) {
            if (entry.getValue().equals(val)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
