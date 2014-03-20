package org.dpl.sync;

import android.accounts.Account;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;

public class DplSyncAdapterFinishReceiver extends BroadcastReceiver {

	public static final String ACCOUNT = "account";
	public static final String AUTHORITY = "authority";

	@Override
	public void onReceive(Context context, Intent intent) {
		Account account = (Account) intent.getParcelableExtra(ACCOUNT);
		String authority = intent.getStringExtra(AUTHORITY);

		ContentResolver.setSyncAutomatically(account, authority, true);
	}
}