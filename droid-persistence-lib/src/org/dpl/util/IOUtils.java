package org.dpl.util;

import java.io.IOException;
import java.io.InputStream;

import android.util.Log;

public class IOUtils {

	private static final String TAG = IOUtils.class.getSimpleName();

	/**
	 * Lê um arquivo (input) e retorna esse arquivo em forma de string.
	 * 
	 * @param input
	 * @return
	 * @throws IOException
	 */
	public static String toString(InputStream input) throws IOException {
		StringBuffer stream = new StringBuffer();
		byte[] b = new byte[4096];
		for (int n; (n = input.read(b)) != -1;) {
			stream.append(new String(b, 0, n));
		}
		return stream.toString();
	}

	public static void closeQuietly(InputStream input) {
		try {
			input.close();
		} catch (IOException e) {
			Log.e(TAG, null, e);
		}
	}
}
