package de.mixedfx.network.messagesd;

import java.io.Serializable;

import com.google.gson.annotations.Expose;

/**
 * Master Message object which contains the information whether the message was sent by the server. All sub classes must declare {@link Expose} to let
 * the fields being serialized.
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
	public boolean fromServer;

	public Message()
	{
		this.fromServer = false;
	}
}
