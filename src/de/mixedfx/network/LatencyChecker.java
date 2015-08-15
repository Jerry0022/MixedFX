package de.mixedfx.network;

import org.icmp4j.IcmpPingRequest;
import org.icmp4j.IcmpPingResponse;
import org.icmp4j.IcmpPingUtil;

public class LatencyChecker
{
	public static void register(OverlayNetwork network)
	{
		// request
		final IcmpPingRequest request = IcmpPingUtil.createIcmpPingRequest();
		request.setHost(network.getIP().getHostAddress());
		request.setTimeout(2000);
		while (true)
		{
			// delegate
			final IcmpPingResponse response = IcmpPingUtil.executePingRequest(request);
			System.out.println(response.getSuccessFlag() + "!" + response.getDuration());

			// log
			final String formattedResponse = IcmpPingUtil.formatResponse(response);
			System.out.println(formattedResponse);

			// rest
			try
			{
				Thread.sleep(NetworkConfig.UDP_BROADCAST_INTERVAL);
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}
}
