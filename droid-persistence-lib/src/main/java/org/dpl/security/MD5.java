package org.dpl.security;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MD5 {

	private static MD5 instance;
	private static final String MESSAGE_DIGEST = "MD5";
	private static Lock lock = new ReentrantLock();
	
	private MD5() {}
	
	public static MD5 getInstance() {
		if (instance == null) {
			try {
				// Tenta obter o lock para instanciar o objeto
				lock.lock();
				if (instance == null) {
					instance = new MD5();
				}
			} finally {
				lock.unlock();
			}
		}
		return instance;
	}
	
	private char[] hexCodes(byte[] text) {

		char[] hexOutput = new char[text.length * 2];

		String hexString;

		for (int i = 0; i < text.length; i++) {

			hexString = "00" + Integer.toHexString(text[i]);

			hexString.getChars(hexString.length() - 2,

			hexString.length(), hexOutput, i * 2);

		}

		return hexOutput;
	}
	
	/**
	 * Criptograva uma String através do algoritmo MD5
	 * @param entrada
	 * @return O resultado da criptografia
	 */
	public String encrypt(String pwd) throws NoSuchAlgorithmException {
		
		MessageDigest md = MessageDigest.getInstance(MESSAGE_DIGEST);
		if (md != null) {

			return new String(hexCodes(md.digest(pwd.getBytes())));

		}
		return null;
	}  
}