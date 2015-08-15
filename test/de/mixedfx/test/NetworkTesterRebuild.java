package de.mixedfx.test;

import java.util.UUID;

import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;

import de.mixedfx.java.Ran;
import de.mixedfx.logging.Log;
import de.mixedfx.network.rebuild.ConnectivityManager;
import de.mixedfx.network.rebuild.MessageBus;
import de.mixedfx.network.rebuild.MessageBus.MessageReceiver;
import de.mixedfx.network.rebuild.NetworkManager;
import de.mixedfx.network.rebuild.OverlayNetwork;
import de.mixedfx.network.rebuild.TCPClient;
import de.mixedfx.network.rebuild.UDPDetected;
import de.mixedfx.network.rebuild.messages.Message;
import de.mixedfx.network.rebuild.messages.WelcomeMessage;
import de.mixedfx.network.rebuild.user.User;
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
				if (c.wasAdded())
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
					User user = c.getAddedSubList().get(0);
					Log.network.info((!c.wasReplaced() ? "New" : "Updated") + " User: " + user);
					Log.network.info("User was detected to be in this network: " + user.networks);

					user.networks.addListener(new ListChangeListener<OverlayNetwork>()
					{

						@Override
						public void onChanged(javafx.collections.ListChangeListener.Change<? extends OverlayNetwork> c)
						{
							c.next();
							if (c.wasAdded())
								Log.network.info("User joined over other network: " + c.getAddedSubList().get(0) + " so that he is now in following networks: " + user.networks);
							else
								Log.network.info("User left over this network: " + c.getRemoved().get(0) + " but is still in: " + user.networks);
						}

					});
					WelcomeMessage message = new WelcomeMessage();
					message.setReceivers(user);
					MessageBus.send(message);
				} else if (c.wasRemoved())
					Log.network.info("Removed User: " + c.getRemoved().get(0));
			}

			Log.network.info("UserManager list changed to: " + ConnectivityManager.otherUsers);
		});

		MessageBus.registerForReceival(new MessageReceiver()
		{
			@Override
			public void receive(Message message)
			{
				if (message instanceof WelcomeMessage)
					Log.network.info("YEAH! WelcomeMessage received!");
			}
		}, true);

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

			@Override
			public void mergeMe(User newUser)
			{
			}
		});
		try
		{
			Thread.sleep(Ran.dom(10000, 30000));
		} catch (InterruptedException e)
		{
		}
		ConnectivityManager.restart();
		try
		{
			Thread.sleep(Ran.dom(10000, 30000));
		} catch (InterruptedException e)
		{
		}
		ConnectivityManager.restart();
		while (true)
			;
	}

	@EventTopicSubscriber(topic = NetworkManager.NETWORK_FATALERROR)
	public void on(String topic, Exception data)
	{
		Log.network.fatal("NETWORK FATAL ERROR: " + data);
	}

}
