package de.mixedfx.network.messages;

import java.io.Serializable;

import de.mixedfx.network.NetworkConfig;

/**
 * Master Message object which contains the information whether the message was sent by the
 * {@link NetworkConfig.States#Server}.
 *
 * @author Jerry
 */
@SuppressWarnings("serial")
public class Message implements Serializable
{
	public boolean	fromServer;

	public Message()
	{
		this.fromServer = false;
	}
}
