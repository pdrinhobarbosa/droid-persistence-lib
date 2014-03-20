package org.dpl.sync;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.os.Bundle;

public abstract class DplAuthenticator extends AbstractAccountAuthenticator {

	// Account
	public static String ACCOUNT;

	// Account type
	public static String ACCOUNT_TYPE;

	public abstract String getAccountType();

	public abstract String getAccountName();

	// Simple constructor
	public DplAuthenticator(Context context) {
		super(context);
		ACCOUNT = getAccountName();
		ACCOUNT_TYPE = getAccountType();
	}

	// Editing properties is not supported
	@Override
	public Bundle editProperties(AccountAuthenticatorResponse r, String s) {
		throw new UnsupportedOperationException();
	}

	// Don't add additional accounts
	@Override
	public Bundle addAccount(AccountAuthenticatorResponse r, String s, String s2, String[] strings, Bundle bundle) throws NetworkErrorException {
		return null;
	}

	// Ignore attempts to confirm credentials
	@Override
	public Bundle confirmCredentials(AccountAuthenticatorResponse r, Account account, Bundle bundle) throws NetworkErrorException {
		return null;
	}

	// Getting an authentication token is not supported
	@Override
	public Bundle getAuthToken(AccountAuthenticatorResponse r, Account account, String s, Bundle bundle) throws NetworkErrorException {
		throw new UnsupportedOperationException();
	}

	// Getting a label for the auth token is not supported
	@Override
	public String getAuthTokenLabel(String s) {
		throw new UnsupportedOperationException();
	}

	// Updating user credentials is not supported
	@Override
	public Bundle updateCredentials(AccountAuthenticatorResponse r, Account account, String s, Bundle bundle) throws NetworkErrorException {
		throw new UnsupportedOperationException();
	}

	// Checking features for the account is not supported
	@Override
	public Bundle hasFeatures(AccountAuthenticatorResponse r, Account account, String[] strings) throws NetworkErrorException {
		throw new UnsupportedOperationException();
	}

	/**
	 * Create a new dummy account for the sync adapter
	 * 
	 * @param context
	 *            The application context
	 */
	public static Account CreateSyncAccount(Context context) {
		// Create the account type and default account
		Account newAccount = new Account(ACCOUNT, ACCOUNT_TYPE);

		// Get an instance of the Android account manager
		AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

		/*
		 * Add the account and account type, no password or user data
		 * If successful, return the Account object, otherwise report an error.
		 */
		if (accountManager.addAccountExplicitly(newAccount, null, null)) {
			/*
			 * If you don't set android:syncable="true" in
			 * in your <provider> element in the manifest,
			 * then call context.setIsSyncable(account, AUTHORITY, 1)
			 * here.
			 */
		} else {
			/*
			 * The account exists or some other error occurred. Log this, report it,
			 * or handle it internally.
			 */
		}

		return newAccount;
	}
}