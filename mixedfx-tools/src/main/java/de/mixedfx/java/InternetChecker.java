package de.mixedfx.java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.icmp4j.IcmpPingRequest;
import org.icmp4j.IcmpPingResponse;
import org.icmp4j.IcmpPingUtil;

public class InternetChecker
{
	public static boolean isOnlineICMP()
	{
		// request
		final IcmpPingRequest request = IcmpPingUtil.createIcmpPingRequest();
		request.setHost("http://www.google.de");
		request.setTimeout(2000);
		// delegate
		final IcmpPingResponse response = IcmpPingUtil.executePingRequest(request);
		return response.getSuccessFlag();
	}

	public static boolean isOnlineHTTP()
	{
		try
		{
			final URL url = new URL("http://www.google.com");
			final URLConnection conn = url.openConnection();
			conn.connect();
			return true;
		} catch (MalformedURLException e)
		{
			throw new RuntimeException(e);
		} catch (IOException e)
		{
			return false;
		}
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
