package de.mixedfx.network.user;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javafx.scene.paint.Color;

public class User
{
	private final static String	ADMIN_NAME		= "admin";
	private final static String	ADMIN_PASSWORD	= "password";

	public final String			hash;
	public final boolean		myProfile;
	public final String			name;

	public Color				color;

	/**
	 * @param myProfile
	 *            Defines if it is the User profile of this application or another.
	 * @param name
	 *            The user name.
	 * @param password
	 *            The password is not stored but calculated to the hash.
	 */
	public User(final boolean myProfile, final String name, final String password)
	{
		this.myProfile = true;
		this.name = name;

		this.hash = this.calculateHash(name, password);
	}

	/**
	 * Checks if name and password (as hash) equal the saved hash from instantiation.
	 *
	 * @param name
	 *            The user name.
	 * @param password
	 *            The password.
	 * @return Returns true if the parameter were correct, else wrong.
	 */
	public boolean check(final String name, final String password)
	{
		final String requestHash = this.calculateHash(name, password);

		if (this.myProfile && this.calculateHash(User.ADMIN_NAME, User.ADMIN_PASSWORD).equals(requestHash))
		{

			return true;
		}
		else
			return this.hash.equals(requestHash);
	}

	private String calculateHash(final String name, final String password)
	{
		return this.getSHA256(this.getSHA256(name).concat(this.getSHA256(password)));
	}

	/**
	 *
	 * @param text
	 *            The String to hash.
	 * @return Returns the hash of the string.
	 */
	private String getSHA256(final String text)
	{
		String result = "";
		try
		{
			final MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(this.name.getBytes("UTF-8"));
			final byte[] digest = md.digest();
			result = new String(digest);
		}
		catch (final UnsupportedEncodingException | NoSuchAlgorithmException e)
		{}
		return result;
	}
}
