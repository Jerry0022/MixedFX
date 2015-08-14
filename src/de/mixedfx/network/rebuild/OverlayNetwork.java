package de.mixedfx.network.rebuild;

import java.net.InetAddress;

import org.apache.commons.net.util.SubnetUtils;

public abstract class OverlayNetwork
{
	/**
	 * @param ip
	 * @return Returns true if IP is in at least one range of {@link OverlayNetwork#getRange()}!
	 */
	public static boolean testInRange(InetAddress ip, Class<? extends OverlayNetwork> network)
	{
		OverlayNetwork rangeNetwork;
		try
		{
			rangeNetwork = network.newInstance();
			for (String range : rangeNetwork.getRange())
				if (rangeNetwork.isInRange(range, ip.getHostAddress()))
					return true;
		} catch (InstantiationException | IllegalAccessException e)
		{
		}
		return false;
	}

	public InetAddress ip;

	protected void setIP(InetAddress ip)
	{
		this.ip = ip;
	}

	public InetAddress getIP()
	{
		return this.ip;
	}

	/**
	 * @return Returns the network ip, e. g. "192.168.0.0/16"
	 */
	public abstract String[] getRange();

	private boolean isInRange(String rangeIP, String testIP)
	{
		if (rangeIP.isEmpty())
			return true;
		try
		{
			SubnetUtils utils = new SubnetUtils(rangeIP);
			boolean isInRange = utils.getInfo().isInRange(testIP);
			return isInRange;
		} catch (Exception e)
		{
			return false;
		}
	}

	/**
	 * @param network
	 * @return Returns true if of the same type!
	 */
	@Override
	public boolean equals(Object network)
	{
		return this.getClass().equals(network.getClass());
	}
}
