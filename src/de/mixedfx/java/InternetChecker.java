package de.mixedfx.java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.icmp4j.IcmpPingRequest;
import org.icmp4j.IcmpPingResponse;
import org.icmp4j.IcmpPingUtil;

public class InternetChecker
{
	public static boolean isOnline()
	{
		// request
		final IcmpPingRequest request = IcmpPingUtil.createIcmpPingRequest();
		request.setHost("www.google.de");
		request.setTimeout(2000);
		// delegate
		final IcmpPingResponse response = IcmpPingUtil.executePingRequest(request);
		return response.getSuccessFlag();
	}

	public static String getExternalIP()
	{
		URL whatismyip;
		try
		{
			whatismyip = new URL("http://checkip.amazonaws.com");
			BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
			String ip = in.readLine(); // you get the IP as a String
			return ip;
		} catch (IOException e)
		{
			return "";
		}
	}
}
