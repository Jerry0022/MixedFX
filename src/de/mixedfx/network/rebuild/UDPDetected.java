package de.mixedfx.network.rebuild;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Date;
import java.util.function.Predicate;

public class UDPDetected implements Serializable
{
	public static Predicate<UDPDetected> getByAddress(final InetAddress address)
	{
		return t -> t.address.getHostAddress().equals(address.getHostAddress());
	}

	/*
	 * Set on receival
	 */
	public InetAddress address;

	/*
	 * Set on sending.
	 */
	private long timeStamp;

	public UDPDetected(final Date timeStamp)
	{
		this.timeStamp = timeStamp.getTime();
	}

	public void update(final Date lastContact)
	{
		this.timeStamp = lastContact.getTime();
	}

	@Override
	public String toString()
	{
		return "UDPDetected with lastContact on " + getTimeStamp() + "!";
	}

	/*
	 * GETTER
	 */

	public Date getTimeStamp()
	{
		return new Date(timeStamp);
	}
}
