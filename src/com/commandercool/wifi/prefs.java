package com.commandercool.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.widget.Toast;

public class prefs extends PreferenceActivity  {
	
	private static final String PREFS_NAME = "wi-fi preferences";
	
	private BroadcastReceiver wifistatusReceiver = null;
	private SharedPreferences preferences = null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Shared preferences
        preferences = getSharedPreferences(PREFS_NAME, 1);
        
        // Preferences
        addPreferencesFromResource(R.xml.prefs);
        CheckBoxPreference pWifiState = (CheckBoxPreference) findPreference("wifi_preference");
        pWifiState.setOnPreferenceClickListener(pWifiStateListener);
        CheckBoxPreference pHideIcon = (CheckBoxPreference) findPreference("hide_icon");
        pHideIcon.setOnPreferenceClickListener(pHideIconListener);
        
        // Auto run preference
        CheckBoxPreference pAutorun = (CheckBoxPreference) findPreference("autorun");
        pAutorun.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				// TODO Auto-generated method stub
				SharedPreferences.Editor prefsEditor = preferences.edit();
				if (((CheckBoxPreference)preference).isChecked()){
					prefsEditor.putBoolean("AUTORUN", true);
				} else {
					prefsEditor.putBoolean("AUTORUN", false);
				}
				prefsEditor.commit();
				return false;
			}
		});
        
        CheckBoxPreference pService = (CheckBoxPreference) findPreference("start_service");
        if (pService.isChecked()){
        	startService(new Intent(getBaseContext(), WifiWatcher.class));
        }
        
        WifiManager mWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        pWifiState.setChecked(mWifiManager.isWifiEnabled());
    
        CheckBoxPreference pServiceState = (CheckBoxPreference) findPreference("start_service");
        pServiceState.setOnPreferenceClickListener(pServiceListener);
        
		updateWifireference();
		
		// BroadcastReceiver on Wi-Fi status changed
		registerReceiver (wifistatusReceiver = new BroadcastReceiver(){

			@Override
			public void onReceive(Context context, Intent intent) {
				updateWifireference();
			}
      
        }, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));

    }
    
    public void onDestroy(){
    	super.onDestroy();
    	unregisterReceiver(wifistatusReceiver);
    }
    
    /** Updating references */
    private void updateWifireference(){
    	CheckBoxPreference pWifiState = (CheckBoxPreference) findPreference("wifi_preference");
    	
        WifiManager mWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        
        int state = mWifiManager.getWifiState();
        switch (state){
        case WifiManager.WIFI_STATE_ENABLED:
        	pWifiState.setSummary(R.string.turn_wifi_off);
        	pWifiState.setEnabled(true);
        	break;
        case WifiManager.WIFI_STATE_DISABLED:
        	pWifiState.setSummary(R.string.turn_wifi_on);
    		pWifiState.setEnabled(true);
    		break;
        }
    
        pWifiState.setChecked(mWifiManager.isWifiEnabled());
    }
    
    private OnPreferenceClickListener pWifiStateListener = new OnPreferenceClickListener(){
		@Override
		public boolean onPreferenceClick(Preference preference) {
			preference.setEnabled(false);
			WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
			if (wifiManager.isWifiEnabled()){
				preference.setSummary(R.string.turning_wifi_off);
			} else {
				preference.setSummary(R.string.turning_wifi_on);
			}
			try{
				wifiManager.setWifiEnabled(((CheckBoxPreference)preference).isChecked());
				updateWifireference();
			} catch (Exception ex){
				Toast.makeText(getApplicationContext(), ex.toString(), Toast.LENGTH_LONG);
			}
			return false;
		}

    	
    };
    
    private OnPreferenceClickListener pServiceListener = new OnPreferenceClickListener(){
		@Override
		public boolean onPreferenceClick(Preference preference) {
			if (((CheckBoxPreference)preference).isChecked()){
				startService(new Intent(getBaseContext(), WifiWatcher.class));
			} else {
				stopService(new Intent(getBaseContext(), WifiWatcher.class));
			}
			return false;
		}

    	
    };
    
    private OnPreferenceClickListener pHideIconListener = new OnPreferenceClickListener(){

		@Override
		public boolean onPreferenceClick(Preference preference) {
			// TODO Auto-generated method stub
			SharedPreferences.Editor prefsEditor = preferences.edit();
			if (((CheckBoxPreference)preference).isChecked()){
				prefsEditor.putBoolean("HIDE_ICON", true);
			} else {
				prefsEditor.putBoolean("HIDE_ICON", false);
			}
			prefsEditor.commit();
			
			stopService(new Intent(getBaseContext(), WifiWatcher.class));
			startService(new Intent(getBaseContext(), WifiWatcher.class));
			return false;
		}
    	
    };
}