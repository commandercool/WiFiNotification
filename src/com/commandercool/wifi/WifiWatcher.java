package com.commandercool.wifi;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.text.format.Formatter;
import android.widget.RemoteViews;

public class WifiWatcher extends Service {
	
	/** Strings and constants */
	private static final String PREFS_NAME = "wi-fi preferences";
	private int LAST_STATE = -1;
	
	/** Notification */
	private NotificationManager mNotificationManager = null;
	private final static int NOTIF_ID = 1300;
	int icon = 0;
	
	/** Receivers */
	BroadcastReceiver connectivityReceiver;
	BroadcastReceiver wifistatusReceiver;
	
	/** Preferences */
	private boolean hideIcon = false;
	
	public void onCreate(){
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
		SharedPreferences preferences = getSharedPreferences(PREFS_NAME, 1);
		hideIcon = preferences.getBoolean("HIDE_ICON", false);
		
		
		// Network state broadcast receiver
        registerReceiver(connectivityReceiver = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {
		        setNotification();
			}
        	
        }, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));//(ConnectivityManager.CONNECTIVITY_ACTION));
        
        // Wi-Fi state Broadcast Listener
        registerReceiver (wifistatusReceiver = new BroadcastReceiver(){
      
			@Override
			public void onReceive(Context context, Intent intent) {
		        setNotification();
			}
        	
        }, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
        
	}
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void onDestroy(){
		mNotificationManager.cancel(NOTIF_ID);
		unregisterReceiver(connectivityReceiver);
		unregisterReceiver(wifistatusReceiver);
	}
	
    /** Forming and setting notification */
    private synchronized void setNotification(){
    	
    	ConnectivityManager mConnectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo.DetailedState networkState = mNetworkInfo.getDetailedState();
        if (LAST_STATE == networkState.hashCode()) return;
        
        // Getting WiFi manager and info
        WifiManager mWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
		
		
		RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.notify);
		if (mWifiManager.isWifiEnabled()){
			icon = R.drawable.icon_on;
			contentView.setImageViewResource(R.id.image, R.drawable.icon_on);
			contentView.setTextViewText(R.id.ipInfo, getText(R.string.wifi_on));
		} else {
			if (hideIcon){
				mNotificationManager.cancel(NOTIF_ID);
				return;
			}
			icon = R.drawable.icon_off;
			contentView.setImageViewResource(R.id.image, R.drawable.icon_off);
			contentView.setTextViewText(R.id.ipInfo, getText(R.string.wifi_off));
		}
		// instead of canceling notification
		contentView.setTextViewText(R.id.speedInfo, "");
		CharSequence tickerText = "";
		
		boolean showNotify = true;
		
		switch (networkState){
		case AUTHENTICATING:
			tickerText = getText(R.string.authentication);
	        contentView.setTextViewText(R.id.netId, getText(R.string.authentication));
	        break;
		case CONNECTING:
			tickerText = getText(R.string.connecting);
	        contentView.setTextViewText(R.id.netId, getText(R.string.connecting) + " " + mWifiInfo.getSSID());
			break;
		case CONNECTED:
			tickerText = getText(R.string.connected)+ " " + mWifiInfo.getSSID();
	        contentView.setTextViewText(R.id.netId, getText(R.string.connected)+ " " + mWifiInfo.getSSID());
			contentView.setTextViewText(R.id.ipInfo, getText(R.string.local_ip)+ " " + Formatter.formatIpAddress(mWifiInfo.getIpAddress()));
			contentView.setTextViewText(R.id.speedInfo, getText(R.string.speed)+ " " + mWifiInfo.getLinkSpeed() + " "
						+ WifiInfo.LINK_SPEED_UNITS);
			break;
		case DISCONNECTING:
			tickerText = getText(R.string.disconnecting);
	        contentView.setTextViewText(R.id.netId, getText(R.string.disconnecting));
			break;
		case DISCONNECTED:
			tickerText = getText(R.string.disconnected);
			contentView.setTextViewText(R.id.netId, getText(R.string.disconnected));
			break;
		case FAILED:
			tickerText = getText(R.string.failed);
			contentView.setTextViewText(R.id.netId, getText(R.string.failed) + mWifiInfo.getSSID());
			break;
		case IDLE:
			tickerText = getText(R.string.wifi_off);
			contentView.setTextViewText(R.id.netId, getText(R.string.disconnected));
			break;
		case OBTAINING_IPADDR:
			tickerText = getText(R.string.getIp);
	        contentView.setTextViewText(R.id.netId, getText(R.string.getIp));
	        break;
		case SCANNING:
			tickerText = getText(R.string.scanning);
	        contentView.setTextViewText(R.id.netId, getText(R.string.scanning));
	        break;
		case SUSPENDED:
			tickerText = getText(R.string.suspended);
			contentView.setTextViewText(R.id.netId, getText(R.string.suspended));
			break;
		default:
			showNotify = false;
			break;	
		}
		if (showNotify){
			// Setting notification time
			long when = System.currentTimeMillis();
		
			// Creating notification message
			Notification notification = new Notification(icon, tickerText, when);
			notification.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
			notification.contentView = contentView;
        
			// Setting notification Intent
			Intent notificationIntent = new Intent(this, prefs.class);
			PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(), 0, notificationIntent, 0);
			notification.contentIntent = contentIntent;
			mNotificationManager.notify(NOTIF_ID, notification);
			LAST_STATE = networkState.hashCode();
		}
    }

}
