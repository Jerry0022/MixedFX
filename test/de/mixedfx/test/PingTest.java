package de.mixedfx.test;

import org.icmp4j.IcmpPingRequest;
import org.icmp4j.IcmpPingResponse;
import org.icmp4j.IcmpPingUtil;

public class PingTest
{

	public static void main(String[] args)
	{
		// repeat a few times
		for (int count = 1; count <= 5; count++)
		{
			// request
			final IcmpPingRequest request = IcmpPingUtil.createIcmpPingRequest();
			request.setHost("www.google.de");
			request.setTimeout(2000);

			// delegate
			final IcmpPingResponse response = IcmpPingUtil.executePingRequest(request);
			System.out.println(response.getSuccessFlag() + "!" + response.getDuration());
			// log
			final String formattedResponse = IcmpPingUtil.formatResponse(response);
			System.out.println(formattedResponse);

			// rest
			try
			{
				Thread.sleep(1000);
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}
}
