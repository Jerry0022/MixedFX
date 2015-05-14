package de.mixedfx.network.messages;

import java.io.Serializable;

import com.google.gson.annotations.Expose;

/**
 * Master Message object which contains the information whether the message was sent by the server.
 * All sub classes must declare {@link Expose} to let the fields being serialized.
 *
 * @author Jerry
 */
@SuppressWarnings("serial")
public class Message implements Serializable
{
	/*
	 * START Message object
	 */

	@Expose
	public boolean	fromServer;

	@Expose
	public boolean	goodbye;

	public Message()
	{
		this.fromServer = false;
		this.goodbye = false;
	}
}
