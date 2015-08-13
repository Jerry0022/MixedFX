package de.mixedfx.test;

import java.net.InetAddress;
import java.util.UUID;

import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;

import de.mixedfx.logging.Log;
import de.mixedfx.network.rebuild.ConnectivityManager;
import de.mixedfx.network.rebuild.NetworkManager;
import de.mixedfx.network.rebuild.TCPClient;
import de.mixedfx.network.rebuild.UDPDetected;
import de.mixedfx.network.user.User;
import de.mixedfx.network.user.UserManager;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;

public class NetworkTesterRebuild
{

	public static void main(String[] args)
	{
		/*
		 * Set up logging!
		 */

		// Log.network.setLevel(Level.DEBUG);

		// Log fatal errors (network reacted already to this error)
		NetworkTesterRebuild rebuild = new NetworkTesterRebuild();
		AnnotationProcessor.process(rebuild);

		// Log UDP members
		NetworkManager.u.allAdresses.addListener(new ListChangeListener<UDPDetected>()
		{
			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends UDPDetected> c)
			{
				c.next();
				Log.network.trace("AllAddresses updated: " + c.getAddedSubList().get(0));
			}
		});

		// Log TCP members
		NetworkManager.t.tcpClients.addListener(new ListChangeListener<TCPClient>()
		{
			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends TCPClient> c)
			{
				c.next();
				if (c.wasRemoved())
					for (TCPClient tcp : c.getRemoved())
						Log.network.trace("TCP removed: " + tcp);
				else if (c.wasAdded())
					for (TCPClient tcp : c.getAddedSubList())
						Log.network.debug("TCP added: " + tcp);
			}
		});

		/*
		 * Set up network!
		 */

		// Create example user
		final String id = UUID.randomUUID().toString();
		final ExampleUser user = new ExampleUser(id.substring(id.length() - 7, id.length()));

		// Register UserManager services
		final UserManager<ExampleUser> userManager = new UserManager<ExampleUser>(user);
		UserManager.allUsers.addListener((ListChangeListener<User>) c ->
		{
			while (c.next())
			{
				if (c.wasAdded() && c.wasReplaced())
				{
					c.getAddedSubList().get(0).networks.addListener(new MapChangeListener<InetAddress, Long>()
					{
						@Override
						public void onChanged(javafx.collections.MapChangeListener.Change<? extends InetAddress, ? extends Long> change)
						{
							Log.network.info("Network was " + (change.wasAdded() == true ? "added" : "removed") + " for user " + c.getAddedSubList().get(0) + " with following data: " + change.getKey()
									+ " " + change.getValueAdded());
						}
					});
				}
			}

			Log.network.info("UserManager list changed to: " + UserManager.allUsers);
		});

		ConnectivityManager.start();
		try
		{
			Thread.sleep(10000);
		} catch (InterruptedException e)
		{
		}
		ConnectivityManager.stop();
		while (true)
			;
	}

	@EventTopicSubscriber(topic = NetworkManager.NETWORK_FATALERROR)
	public void on(String topic, Exception data)
	{
		Log.network.fatal("NETWORK FATAL ERROR: " + data);
	}

}
