package com.walletv2.services;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import com.walletv2.entity.AlarmDetails;

/**
 * This is our service client, it is the 'middle-man' between the service and any activity that wants to connect to the service
 * 
 */
public class ScheduleClient {
	// The hook into our service
	private ScheduleService mBoundService;
	// The mActivity to start the service in
	private final Context mContext;
	// A flag if we are connected to the service or not
	private boolean mIsBound;
	
	public ScheduleClient(Context context) {
		mContext = context;
	}
	
	/**
	 * Call this to connect your activity to your service
	 */
	public void doBindService() {
		// Establish a connection with our service
		mContext.bindService(new Intent(mContext, ScheduleService.class), mConnection, Context.BIND_AUTO_CREATE);
		mIsBound = true;
	}
	
	/**
	 * When you attempt to connect to the service, this connection will be called with the result. If we have successfully connected we instantiate our service object so that we can call methods on
	 * it.
	 */
	private final ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			// This is called when the connection with our service has been established,
			// giving us the service object we can use to interact with our service.
			mBoundService = ((ScheduleService.ServiceBinder) service).getService();
		}
		
		@Override
		public void onServiceDisconnected(ComponentName className) {
			mBoundService = null;
		}
	};
	
	/**
	 * Tell our service to set an alarm for the given date
	 * 
	 * @param alarmDetails
	 *            a date to set the notification for
	 */
	public void setAlarmForNotification(AlarmDetails alarmDetails) {
		mBoundService.setAlarm(alarmDetails);
	}
	
	/**
	 * When you have finished with the service call this method to stop it releasing your connection and resources
	 */
	public void doUnbindService() {
		if (mIsBound) {
			// Detach our existing connection.
			mContext.unbindService(mConnection);
			mIsBound = false;
		}
	}
}