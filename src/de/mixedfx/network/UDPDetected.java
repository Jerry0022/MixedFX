package de.mixedfx.network;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Date;
import java.util.function.Predicate;

public class UDPDetected implements Serializable
{
	protected static int getEstimatedMaxSize()
	{
		return 500;
	}

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
	private long	timeStamp;
	private String	status;
	private long	statusSince;
	private long	networkSince;
	private int		pid;

	public UDPDetected(final Date timeStamp, final NetworkConfig.States status, Date networkExistsSince, int pid)
	{
		this.timeStamp = timeStamp.getTime();
		this.status = status.toString();
		this.statusSince = status.stateSince;
		if (networkExistsSince != null)
			this.networkSince = networkExistsSince.getTime();
		this.pid = pid;
	}

	public void update(final Date lastContact, final NetworkConfig.States status)
	{
		this.timeStamp = lastContact.getTime();
		this.status = status.toString();
	}

	@Override
	public String toString()
	{
		return "UDP member message sent " + getTimeStamp() + " with status " + getStatus() + " since " + getStatus().stateSince + " with PID " + pid;
	}

	/*
	 * GETTER
	 */

	public Date getTimeStamp()
	{
		return new Date(timeStamp);
	}

	public NetworkConfig.States getStatus()
	{
		NetworkConfig.States status = NetworkConfig.States.valueOf(this.status);
		status.stateSince = statusSince;
		return status;
	}

	/**
	 * @return Default is null!
	 */
	public Date getNetworkSince()
	{
		return new Date(networkSince);
	}

	public int getPid()
	{
		return pid;
	}
}
