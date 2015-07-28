package de.mixedfx.network;

import java.net.InetAddress;
import java.util.Date;
import java.util.function.Predicate;

public class UDPDetected
{
	public final InetAddress	address;
	public NetworkConfig.States	status;
	public Date					timeStamp;
	public Date					statusSince;
	public int					pid;

	public UDPDetected(final InetAddress address, final Date timeStamp, final NetworkConfig.States status, final Date statusSince, int pid)
	{
		this.address = address;
		this.status = status;
		this.timeStamp = timeStamp;
		this.statusSince = statusSince;
		this.pid = pid;
	}

	public void update(final NetworkConfig.States status, final Date lastContact)
	{
		this.status = status;
		this.timeStamp = lastContact;
	}

	public static Predicate<UDPDetected> getByAddress(final InetAddress address)
	{
		return t -> t.address.getHostAddress().equals(address.getHostAddress());
	}
}
