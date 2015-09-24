package de.mixedfx.java;

import org.apache.commons.codec.digest.DigestUtils;

public class Crypto
{
	/**
	 *
	 * @param text
	 *            The String to hash. Should be in UTF-8 format!
	 * @return Returns the hash of the string as hex numbers (64 chars totally)!
	 */
	public static String getSHA256(final String text)
	{
		if ((text == null) || text.isEmpty())
			throw new IllegalArgumentException("Only not null Strings with one or more characters can be processed!");
		return DigestUtils.sha256Hex(text);
	}
}
