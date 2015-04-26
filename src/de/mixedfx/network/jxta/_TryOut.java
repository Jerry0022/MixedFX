package de.mixedfx.network.jxta;

import java.io.IOException;
import java.text.MessageFormat;

import net.jxta.exception.PeerGroupException;
import net.jxta.platform.NetworkManager;
import net.jxta.platform.NetworkManager.ConfigMode;

public class _TryOut
{
	public static void main(final String[] args)
	{
		new _TryOut();
	}

	_TryOut()
	{
		NetworkManager manager = null;
		try
		{
			manager = new NetworkManager(ConfigMode.ADHOC, "HelloWorld");
		}
		catch (final IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try
		{
			manager.startNetwork();
			final boolean connected = manager.waitForRendezvousConnection(2 * 1000);
			System.out.println(MessageFormat.format("Connected :{0}", connected));
		}
		catch (final PeerGroupException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		catch (final IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
