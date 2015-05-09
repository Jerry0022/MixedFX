package de.mixedfx.network.p2p;

import java.io.IOException;
import java.net.InetSocketAddress;

import rice.environment.Environment;

public class Main
{

	public static void main(final String[] args)
	{
		// try
		// {
		// final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		// while (interfaces.hasMoreElements())
		// {
		// final NetworkInterface networkInterface = interfaces.nextElement();
		//
		// // Don't want to broadcast to loopback interfaces or disabled interface
		// if (networkInterface.isLoopback() || !networkInterface.isUp())
		// continue;
		// System.out.println(networkInterface.getName() + new
		// String(networkInterface.getHardwareAddress()));
		// }
		// }
		// catch (final SocketException e)
		// {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		try
		{
			final Environment env = new Environment();
			// env.getParameters().setString("nat_search_policy", "never"); // For local networks
			// behind a NAT
			final P2PNode node = new P2PNode(4000, new InetSocketAddress("127.0.0.1", 4000), env);
		}
		catch (final IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (final InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
