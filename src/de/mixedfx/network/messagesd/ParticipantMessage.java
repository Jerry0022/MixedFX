package de.mixedfx.network.messagesd;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * There are several usages of this Message:
 *
 * <pre>
 * If uID is not empty and ids are empty - PID request.<br>
 * If uID is not empty and ids are not empty - PID response. First pid is the pid of the requesting participant.<br>
 * If uID is empty and ids are not empty - PIDs lost information.
 * </pre>
 *
 * @author Jerry
 */
@SuppressWarnings("serial")
public class ParticipantMessage extends Message
{
	public String uID;

	/**
	 * The first one, index of 0, is my PID!
	 */
	public List<Integer> ids;

	public ParticipantMessage()
	{
		this.uID = UUID.randomUUID().toString();
		this.ids = new ArrayList<>();
	}
}
