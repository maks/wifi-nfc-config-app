package com.manichord.nfc_wifi_config;

import java.util.Arrays;

import android.net.wifi.WifiConfiguration;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;

/**
 * Pulled straight out of AOSP:
 * https://android.googlesource.com/platform/packages/apps/Nfc/+/master/src/com/android/nfc/NfcWifiProtectedSetup.java
 */

public class NfcUtils {

    public static final String NFC_TOKEN_MIME_TYPE = "application/vnd.wfa.wsc";
    
    /*
     * ID into configuration record for SSID and Network Key in hex.
     * Obtained from WFA Wifi Simple Configuration Technical Specification v2.0.2.1.
     */
    private static final String SSID_ID = "1045";
    private static final String NETWORK_KEY_ID = "1027";
    private static final int SIZE_FIELD_WIDTH = 4;
    private static final int MAX_SSID_SIZE_BYTES = 32;
    private static final int MAX_NETWORK_KEY_SIZE_BYTES = 64;
    private static final int HEX_CHARS_PER_BYTE = 2;
    
    public static WifiConfiguration parse(NdefMessage message) {
        NdefRecord[] records = message.getRecords();
        for (int i = 0; i < records.length; ++i) {
            NdefRecord record = records[i];
            if (new String(record.getType()).equals(NFC_TOKEN_MIME_TYPE)) {
                String hexStringPayload = bytesToHex(record.getPayload());
                int ssidStringIndex = hexStringPayload.indexOf(SSID_ID);
                if (ssidStringIndex > 0) {
                    int networkKeyStringIndex = hexStringPayload.indexOf(NETWORK_KEY_ID);
                    if (networkKeyStringIndex > 0) {
                        ssidStringIndex += SSID_ID.length();
                        networkKeyStringIndex += NETWORK_KEY_ID.length();
                        String ssidSize;
                        try {
                            ssidSize = hexStringPayload.substring(ssidStringIndex, ssidStringIndex + SIZE_FIELD_WIDTH);
                        } catch (IndexOutOfBoundsException ex) {
                            return null;
                        }
                        int ssidSizeBytes = hexStringToInt(ssidSize);
                        if (ssidSizeBytes > MAX_SSID_SIZE_BYTES) {
                            return null;
                        }
                        String networkKeySize;
                        try {
                            networkKeySize = hexStringPayload.substring(networkKeyStringIndex, networkKeyStringIndex
                                    + SIZE_FIELD_WIDTH);
                        } catch (IndexOutOfBoundsException ex) {
                            return null;
                        }
                        int networkKeySizeBytes = hexStringToInt(networkKeySize);
                        if (networkKeySizeBytes > MAX_NETWORK_KEY_SIZE_BYTES) {
                            return null;
                        }
                        ssidStringIndex += SIZE_FIELD_WIDTH;
                        networkKeyStringIndex += SIZE_FIELD_WIDTH;
                        String ssid;
                        String networkKey;
                        try {
                            int ssidByteIndex = ssidStringIndex / HEX_CHARS_PER_BYTE;
                            ssid = new String(Arrays.copyOfRange(record.getPayload(), ssidByteIndex, ssidByteIndex
                                    + ssidSizeBytes));
                            int networkKeyByteIndex = networkKeyStringIndex / HEX_CHARS_PER_BYTE;
                            networkKey = new String(Arrays.copyOfRange(record.getPayload(), networkKeyByteIndex,
                                    networkKeyByteIndex + networkKeySizeBytes));
                        } catch (ArrayIndexOutOfBoundsException ex) {
                            return null;
                        }
                        WifiConfiguration configuration = new WifiConfiguration();
                        configuration.preSharedKey = '"' + networkKey + '"';
                        configuration.SSID = '"' + ssid + '"';
                        return configuration;
                    }
                }
            }
        }
        return null;
    }
    
    private static int hexStringToInt(String bigEndianHexString) {
        int val = 0;
        for (int i = 0; i < bigEndianHexString.length(); ++i) {
            val = (val | Character.digit(bigEndianHexString.charAt(i), 16));
            if (i < bigEndianHexString.length() - 1) {
                val <<= 4;
            }
        }
        return val;
    }
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++) {
            int value = bytes[j] & 0xFF;
            hexChars[j * 2] = Character.forDigit(value >>> 4, 16);
            hexChars[j * 2 + 1] = Character.forDigit(value & 0x0F, 16);
        }
        return new String(hexChars);
    }

}
