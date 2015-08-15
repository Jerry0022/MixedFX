package de.mixedfx.java;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Crypto
{
	/**
	 *
	 * @param text
	 *            The String to hash. Should be in UTF-8 format!
	 * @return Returns the hash of the string.
	 */
	public static String getSHA256(final String text)
	{
		String result = "";
		try
		{
			final MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(text.getBytes("UTF-8"));
			final byte[] digest = md.digest();
			result = new String(digest);
		} catch (final UnsupportedEncodingException | NoSuchAlgorithmException e)
		{
		}
		return result;
	}
}
