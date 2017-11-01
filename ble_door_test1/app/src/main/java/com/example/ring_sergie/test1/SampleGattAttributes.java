package com.example.ring_sergie.test1;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    static {
        // Sample Services.
        attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        // Sample Characteristics.
        attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");

        attributes.put("9760d077-a234-4686-9e21-d087333c2d15" , "PEER_PUBLIC_KEY_WRITE");
        attributes.put("9760d077-a234-4686-9e20-d087333c2d13" , "PUBLIC_PAYLOAD_READ");
        attributes.put("9760d077-a234-4686-9e20-d087333c2c08" , "WIFI_LIST_READ");
        attributes.put("9760d077-a234-4686-9e21-d087333c2c04" , "WIFI_SSID_WRITE");
        attributes.put("9760d077-a234-4686-9e21-d087333c2c05" , "WIFI_PASS_WRITE");
        attributes.put("9760d077-a234-4686-9e21-d087333c2c09" , "START_WIFI_PAIRING");
        attributes.put("9760d077-a234-4686-9e21-d087333c2e01" , "START_ETH_PAIRING");
        attributes.put("9760d077-a234-4686-9e20-d087333c2c14" , "SERIAL_READ");
        attributes.put("9760d077-a234-4686-9e20-d087333c2c10" , "STATE_READ");
        attributes.put("9760d077-a234-4686-9e20-d087333c2c11" , "IFCONFIG_READ");
        attributes.put("9760d077-a234-4686-9e20-d087333c2c06" , "REGCODE_READ");
        attributes.put("9760d077-a234-4686-9e20-d087333c2c12" , "CONNECTIVITY_READ");
        attributes.put("9760d077-a234-4686-9e20-d087333c2c15" , "LAT_WRITE");
        attributes.put("9760d077-a234-4686-9e20-d087333c2c16" , "LNG_WRITE");
        attributes.put("9760d077-a234-4686-9e20-d087333c2c18" , "SAVE_LOC_INFO");
        attributes.put("9760d077-a234-4686-9e20-d087333c2c17" , "TZ_WRITE ");
        attributes.put("9760d077-a234-4686-9e20-d087333c2c04" , "WIFI_SSID_READ");
        attributes.put("9760d077-a234-4686-9e21-d087333c2c07" , "ZIPCODE_WRITE");
        attributes.put("9760d077-a234-4686-9e21-d087333c2d16" , "SECRET_CODE_WRITE");
        attributes.put("9760d077-a234-4686-9e21-d087333c2d17" , "MAC_ADDRESS_READ");
        attributes.put("9760d077-a234-4686-9e21-d087333c2d18" , "SETUP_ID_WRITE");

    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
