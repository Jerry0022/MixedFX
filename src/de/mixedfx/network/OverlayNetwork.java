package de.mixedfx.network;

import java.net.InetAddress;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import org.apache.commons.net.util.SubnetUtils;
import org.icmp4j.IcmpPingRequest;
import org.icmp4j.IcmpPingResponse;
import org.icmp4j.IcmpPingUtil;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;

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

	private InetAddress ip;

	private DoubleProperty	reliability;
	private IntegerProperty	latency;

	public OverlayNetwork()
	{
		this.reliability = new SimpleDoubleProperty(0);
		this.latency = new SimpleIntegerProperty(0);
	}

	protected void setIP(InetAddress ip)
	{
		this.ip = ip;
		updateLatency();
	}

	public InetAddress getIP()
	{
		return this.ip;
	}

	protected void updateLatency()
	{
		if (this.ip != null)
		{
			ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));
			service.submit(new Callable<Integer>()
			{
				public Integer call()
				{
					int latency = 0;
					int successfullRequests = 0;

					// repeat a few times
					for (int count = 0; count <= 5; count++)
					{
						// request
						final IcmpPingRequest request = IcmpPingUtil.createIcmpPingRequest();
						request.setHost(OverlayNetwork.this.ip.getHostAddress());
						request.setTimeout(2000);

						// delegate
						final IcmpPingResponse response = IcmpPingUtil.executePingRequest(request);

						if (response.getSuccessFlag())
							successfullRequests++;

						latency += response.getDuration();

						// rest
						try
						{
							Thread.sleep(1000);
						} catch (InterruptedException e)
						{
							e.printStackTrace();
						}
					}

					OverlayNetwork.this.latency.set(latency / successfullRequests);
					OverlayNetwork.this.reliability.set(successfullRequests / 5);
					return 0;
				}
			});
		}
	}

	/**
	 * @return Returns the median of the latencies detected by 5 ICMP requests.
	 */
	public IntegerProperty latencyProperty()
	{
		return this.latency;
	}

	/**
	 * @return Returns a value from 0 to 1 in 0.2 steps. 1 means the connection is fully reliable, all of the 5 ICMP requests were answered.
	 */
	public DoubleProperty reliablityProperty()
	{
		return this.reliability;
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
