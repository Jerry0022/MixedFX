package de.mixedfx.network;

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
	 * Set on receive
	 */
	public InetAddress address;

	/*
	 * Set on sending
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
		return "UDPDetected with ip " + address + " with lastContact on " + getTimeStamp() + "!";
	}

	@Override
	public boolean equals(Object object) {
		return object instanceof UDPDetected && ((UDPDetected) object).address.equals(this.address);
	}

	/*
	 * GETTER
	 */

	public Date getTimeStamp()
	{
		return new Date(timeStamp);
	}
}
