package org.dpl.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public abstract class DplAuthenticatorService extends Service {

	// Instance field that stores the authenticator object
	private DplAuthenticator mAuthenticator;

	public abstract DplAuthenticator getAutheticator();

	@Override
	public void onCreate() {
		// Create a new authenticator object
		mAuthenticator = getAutheticator();
	}

	/*
	 * When the system binds to this Service to make the RPC call
	 * return the authenticator's IBinder.
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return mAuthenticator.getIBinder();
	}
}