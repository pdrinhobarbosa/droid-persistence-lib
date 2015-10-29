package org.dpl.sync;

import org.dpl.DplProvider;
import org.dpl.sync.DplSyncAdapter.SyncAction;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

public abstract class DplTableObserver extends ContentObserver {

	protected final Context mContext;

	public DplTableObserver(Context context, Handler handler) {
		super(handler);
		mContext = context;
	}

	/*
	 * Define a method that's called when data in the
	 * observed content provider changes.
	 */
	@Override
	public void onChange(boolean selfChange, Uri changeUri) {
		Bundle bundle = new Bundle();
		bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
		bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

		bundle.putString(Uri.class.getSimpleName(), changeUri.toString());

		int[] actions = new int[] {SyncAction.UPLOAD.getId()};
		bundle.putInt(DplSyncAdapter.SYNC_ACTIONS_LENGTH, actions.length);
		for (int i = 0; i < actions.length; i++) {
			bundle.putInt(DplSyncAdapter.SYNC_ACTIONS + i, actions[i]);
		}

		ContentResolver.requestSync(DplAuthenticator.CreateSyncAccount(mContext), DplProvider.getAuthority(mContext), bundle);
	}
}