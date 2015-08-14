package de.mixedfx.test;

import java.util.UUID;

import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;

import de.mixedfx.logging.Log;
import de.mixedfx.network.rebuild.ConnectivityManager;
import de.mixedfx.network.rebuild.NetworkManager;
import de.mixedfx.network.rebuild.TCPClient;
import de.mixedfx.network.rebuild.UDPDetected;
import de.mixedfx.network.rebuild.user.User;
import de.mixedfx.network.user.UserManager;
import javafx.collections.ListChangeListener;

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

		Log.network.info("NOW TESTING NETWORK!");

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

		// Log users
		ConnectivityManager.otherUsers.addListener((ListChangeListener<User>) c ->
		{
			while (c.next())
			{
				if (c.wasAdded())
				{
					Log.network.info((!c.wasReplaced() ? "New" : "Updated") + " User: " + c.getAddedSubList().get(0));

					// c.getAddedSubList().get(0).networks.addListener(new MapChangeListener<InetAddress, Long>()
					// {
					// @Override
					// public void onChanged(javafx.collections.MapChangeListener.Change<? extends InetAddress, ? extends Long> change)
					// {
					// Log.network.info("Network was " + (change.wasAdded() == true ? "added" : "removed") + " for user " + c.getAddedSubList().get(0) + " with following data: " + change.getKey()
					// + " " + change.getValueAdded());
					// }
					// });
				} else if (c.wasRemoved())
					Log.network.info("Removed User: " + c.getRemoved().get(0));
			}

			Log.network.info("UserManager list changed to: " + UserManager.allUsers);
		});

		/*
		 * Set up network!
		 */

		// Create example user
		ConnectivityManager.start(new User()
		{
			private String id;

			{
				this.id = UUID.randomUUID().toString();
			}

			@Override
			public Object getIdentifier()
			{
				return id;
			}
		});
		try
		{
			Thread.sleep(20000);
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
