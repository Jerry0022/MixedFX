package de.mixedfx.network.messages;

import java.util.ArrayList;

import com.google.gson.annotations.Expose;

/**
 * All NOT network messages shall inherit this class for increased performance!
 *
 * @author Jerry
 *
 */
@SuppressWarnings("serial")
public class RegisteredMessage extends Message
{
	/**
	 * <p>
	 * Initially empty => broadcast.
	 * </p>
	 * <p>
	 * If receivers contains no values then this message is a broadcast to everyone else except me.
	 * <br>
	 * If receivers contains one value or more the message is received by these participants. <br>
	 * If receivers contains no value it does not make sense to send this message ;)
	 * </p>
	 */
	@Expose
	public ArrayList<Integer>	receivers;

	public RegisteredMessage()
	{
		this.receivers = new ArrayList<>();
	}
}
