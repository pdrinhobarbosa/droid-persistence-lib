package org.dpl.sync;

import android.content.Context;

public abstract class DplBaseSyncService {

	protected final Context mContext;

	protected DplBaseSyncService(Context context) {
		super();
		mContext = context;
	}

	/**
	 * Test server communication if you app need
	 * 
	 * @return true
	 * 
	 *         Default value true
	 */
	public boolean isServerComminication() {
		return true;
	}

	/**
	 * Notify with a broadcast that your sync service started
	 */
	protected void sendStartSyncReceiver() {}

	/**
	 * Notify with a broadcast that your sync service finish
	 */
	protected void sendFinishSyncReceiver() {}

	public abstract void upload();

	public abstract void download();

	public abstract void cleanDatabase();
}
