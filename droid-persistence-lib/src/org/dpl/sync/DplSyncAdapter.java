package org.dpl.sync;

import java.util.LinkedHashMap;

import org.dpl.DplProvider;
import org.dpl.interfaces.EnumInterface;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

public abstract class DplSyncAdapter extends AbstractThreadedSyncAdapter {

	public static final int SYNC_NOTIFICATION_ID = 1;

	public static final String SYNC_ACTIONS = "syncActions";
	public static final String SYNC_ACTIONS_LENGTH = "syncActionsLength";

	protected final Context mContext;

	protected final LinkedHashMap<Integer, DplBaseSyncService> mSyncs = new LinkedHashMap<Integer, DplBaseSyncService>();

	/**
	 * Set up the sync adapter
	 */
	public DplSyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
		mContext = context;
		init();
	}

	/**
	 * Set up the sync adapter. This form of the
	 * constructor maintains compatibility with Android 3.0
	 * and later platform versions
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public DplSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
		super(context, autoInitialize, allowParallelSyncs);
		mContext = context;
		init();
	}

	/**
	 * Called when this SyncAdapter is created.
	 * 
	 * You must be fill the "LinkedHashMap mSyncs" here, registering your services to run on this
	 * sync.
	 */
	protected abstract void init();

	@Override
	public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
		Log.i(getClass().getSimpleName(), "SyncAdapter running onPerformSync");
		if (isServerComminication()) {
			sendStartSyncReceiver();

			String uriString = extras.getString(Uri.class.getSimpleName());

			int actionsLength = extras.getInt(SYNC_ACTIONS_LENGTH, 0);
			int[] actions = new int[actionsLength];
			for (int i = 0; i < actionsLength; i++) {
				actions[i] = extras.getInt(SYNC_ACTIONS + i);
			}

			if (uriString != null) {
				Uri uri = Uri.parse(uriString);
				DplProvider dplProvider = (DplProvider) provider.getLocalContentProvider();

				runAction(mSyncs.get(dplProvider.match(uri)), actions);
			} else {
				downloadAll();
				uploadAll();
			}

			sendFinishSyncReceiver();

		}

		syncAdapterFinishReceiver(account, authority);
	}

	private void downloadAll() {
		for (Integer key : mSyncs.keySet()) {
			mSyncs.get(key).download();
		}
	}

	private void uploadAll() {
		for (Integer key : mSyncs.keySet()) {
			mSyncs.get(key).upload();
		}
	}

	public void cleanAll() {
		for (Integer code : mSyncs.keySet()) {
			mSyncs.get(code).cleanDatabase();
		}
	}

	private void runAction(DplBaseSyncService syncService, int[] actions) {
		for (int action : actions) {
			if (action == SyncAction.UPLOAD.getId()) {
				syncService.upload();
			} else if (action == SyncAction.DOWNLOAD.getId()) {
				syncService.download();
			} else if (action == SyncAction.CLEAN.getId()) {
				/**
				 * TODO - tirar obj do clean, ele deve rodar independente
				 */

			}
		}
	}

	/**
	 * Test server communication if you application need
	 * 
	 * @return true
	 * 
	 *         Default value true
	 */
	public boolean isServerComminication() {
		return true;
	}

	/**
	 * Notify with a broadcast that your sync adapter started
	 */
	protected void sendStartSyncReceiver() {}

	/**
	 * Notify with a broadcast that your sync adapter finish
	 */
	protected void sendFinishSyncReceiver() {}

	/**
	 * Send message that this sync finish.
	 * 
	 * @param account
	 * @param authority
	 */
	protected void syncAdapterFinishReceiver(Account account, String authority) {
		Intent intent = new Intent(mContext, DplSyncAdapterFinishReceiver.class);
		intent.putExtra(DplSyncAdapterFinishReceiver.ACCOUNT, account);
		intent.putExtra(DplSyncAdapterFinishReceiver.AUTHORITY, authority);
		mContext.sendBroadcast(intent);
	}

	public enum SyncAction implements EnumInterface {
		DOWNLOAD(0), UPLOAD(1), CLEAN(2);

		private int id;

		private SyncAction(int id) {
			this.id = id;
		}

		@Override
		public int getId() {
			return id;
		}

		@Override
		public int getI18nKey() {
			return 0;
		}
	}
}