package de.mixedfx.network.rebuild.messages;

import java.io.Serializable;
import java.net.InetAddress;

/**
 * Master Message object which contains the information to which next hop (as IP) it is send.
 *
 * @author Jerry
 */
@SuppressWarnings("serial")
public class Message implements Serializable
{
	/**
	 * The message was sent from this one ip to avoid packet circuits.
	 */
	private InetAddress fromIP;

	/**
	 * The message is sent to this one ip to avoid packet circuits.
	 */
	private InetAddress toIP;

	/*
	 * START Message object
	 */
	public Message()
	{
	}

	public InetAddress getFromIP()
	{
		return fromIP;
	}

	public void setFromIP(InetAddress fromIP)
	{
		this.fromIP = fromIP;
	}

	/**
	 * @return The IP of the next hop!
	 */
	public InetAddress getToIP()
	{
		return this.toIP;
	}

	/**
	 * @param toIP
	 *            The IP of the next hop!
	 */
	public void setToIP(InetAddress toIP)
	{
		this.toIP = toIP;
	}
}
