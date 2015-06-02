package de.mixedfx.network;

import java.net.InetAddress;
import java.util.Date;
import java.util.function.Predicate;

public class UDPDetected
{
	public final InetAddress	address;
	public NetworkConfig.States	status;
	public Date					lastContact;
	public Date					statusSince;

	public UDPDetected(final InetAddress address, final NetworkConfig.States status, final Date firstContact, final Date statusSince)
	{
		this.address = address;
		this.status = status;
		this.lastContact = firstContact;
		this.statusSince = statusSince;
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
