package com.manichord.nfc_wifi_config;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String LOGTAG = "NFCWifiConfig";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    // ref: http://stackoverflow.com/questions/9871762/android-turning-on-wifi-programmatically
    // http://gabrielaradu.com/?p=711

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOGTAG, "Started with Intent:" + getIntent());
        Log.d(LOGTAG, "Intent MIME:" + getIntent().getType());

        Parcelable[] rawMsgs = getIntent().getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        WifiConfiguration wifiConf = null;
        if (rawMsgs != null) {
            NdefMessage[] msgs = new NdefMessage[rawMsgs.length];
            for (int i = 0; i < rawMsgs.length; i++) {
                msgs[i] = (NdefMessage) rawMsgs[i];
                Log.d(LOGTAG, "ndef mesg:" + msgs[i]);
                wifiConf = NfcUtils.parse(msgs[i]);
                Log.d(LOGTAG, "Parsed mesg:" + wifiConf);
            }
        }
        if (wifiConf != null) {
            String mesg = String.format("Got SSID:%s \nKey:%s", wifiConf.SSID, wifiConf.preSharedKey);
            Log.d(LOGTAG, mesg);
            addWifiConfig(wifiConf);
            Toast.makeText(this.getApplicationContext(), mesg, Toast.LENGTH_LONG).show();
            this.finish();
        }
    }

    /**
     * Add Wifi Configuration, ensuring also that Wifi is turned on
     * @param wifiConf
     */
    private void addWifiConfig(WifiConfiguration wifiConf) {
        WifiManager wifi = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifi.setWifiEnabled(true);
        removeExistingSSID(wifiConf.SSID, wifi);//clear out any existing config for same SSID
        int added = wifi.addNetwork(wifiConf);
        Log.i(LOGTAG, "added network ID: " + added);
        boolean enabled = wifi.enableNetwork(added, true);
        if (!wifi.saveConfiguration()) {
            Log.e(LOGTAG, "error with wpa supplicant persisting Wifi config");
        }
        Log.i(LOGTAG, "enableNetwork returned: " + enabled);
    }

    /**
     * Manually create a Wifi Configuration
     * @param password
     * @param networkSSID
     * @return
     */
    private WifiConfiguration mkWPAConfig(String password, String networkSSID) {
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + networkSSID + "\"";
        conf.preSharedKey = "\"" + password + "\"";
        conf.status = WifiConfiguration.Status.ENABLED;
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        return conf;
    }
    
    /**
     * Remove Network which matches the given SSID
     * based on: http://stackoverflow.com/questions/22670299/android-remove-network-with-certain-ssids
     * @param ssid
     * @param wifiManager
     */
    private void removeExistingSSID(String ssid, WifiManager wifiManager) {
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        if (list == null) {
            return;
        }
        for(WifiConfiguration k : list)
        {
            if(k.SSID.equals(ssid))
            {
                int networkId = wifiManager.getConnectionInfo().getNetworkId();
                if (!wifiManager.removeNetwork(networkId)) {
                    Log.e(LOGTAG, "["+networkId+"] Error trying to remove existing network config SSID:"+ssid);
                } else {
                    Log.d(LOGTAG, "["+networkId+"] removed existing network config SSID:"+ssid);
                }
                if (!wifiManager.saveConfiguration()) {
                    Log.e(LOGTAG, "error with wpa supplicant persisting Wifi config after removal");
                } else {
                    Log.d(LOGTAG, "wpa supplicant persisted Wifi config after removal");
                }
            }
        }
    }
}
