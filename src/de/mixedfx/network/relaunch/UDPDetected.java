package de.mixedfx.network.relaunch;

import java.net.InetAddress;
import java.util.Date;
import java.util.function.Predicate;

public class UDPDetected
{
	public final InetAddress	address;
	public NetworkConfig.States	status;
	public Date					lastContact;

	public UDPDetected(final InetAddress address, final NetworkConfig.States status, final Date firstContact)
	{
		this.address = address;
		this.status = status;
		this.lastContact = firstContact;
	}

	public void update(final NetworkConfig.States status, final Date lastContact)
	{
		this.status = status;
		this.lastContact = lastContact;
	}

	public static Predicate<UDPDetected> getByAddress(final InetAddress address)
	{
		return t -> t.address.getHostAddress().equals(address.getHostAddress());
	}
}
