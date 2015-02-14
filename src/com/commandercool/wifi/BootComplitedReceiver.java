package com.commandercool.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class BootComplitedReceiver extends BroadcastReceiver {
	private static final String PREFS_NAME = "wi-fi preferences";
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, 1);
		if (preferences.getBoolean("AUTORUN", false)){
			context.startService(new Intent(context, WifiWatcher.class));
		}
		
	}

}
