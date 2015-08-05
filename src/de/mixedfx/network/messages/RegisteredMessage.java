package de.mixedfx.network.messages;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.annotations.Expose;

import de.mixedfx.network.ParticipantManager;

/**
 * All NOT network messages shall inherit this class for increased performance! It is a message from an identified user!
 *
 * @author Jerry
 *
 */
@SuppressWarnings("serial")
public abstract class RegisteredMessage extends Message
{
	/**
	 * <p>
	 * Initially empty = broadcast.
	 * </p>
	 * <p>
	 * If receivers contains no values then this message is a broadcast to everyone else except me.
	 * <br>
	 * If receivers contains one value or more the message is received by these participants.
	 * </p>
	 */
	@Expose
	public final List<Integer>	receivers;

	/**
	 * Senders PID, which is automatically set!
	 */
	@Expose
	public int					sender;

	/**
	 * The time the message was instantiated!
	 */
	@Expose
	public final Date			creationTime;

	public RegisteredMessage()
	{
		this.receivers = new ArrayList<>();
		this.sender = ParticipantManager.MY_PID.get();
		this.creationTime = new Date();
	}
}
