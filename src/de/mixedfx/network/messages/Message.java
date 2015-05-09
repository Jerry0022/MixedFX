package de.mixedfx.network.messages;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Message implements Serializable
{
	public boolean	fromServer;

	public Message()
	{
		this.fromServer = false;
	}
}
