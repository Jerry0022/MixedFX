package de.mixedfx.test;

import java.util.UUID;

import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;

import de.mixedfx.java.Ran;
import de.mixedfx.logging.Log;
import de.mixedfx.network.ConnectivityManager;
import de.mixedfx.network.MessageBus;
import de.mixedfx.network.NetworkManager;
import de.mixedfx.network.OverlayNetwork;
import de.mixedfx.network.TCPClient;
import de.mixedfx.network.UDPCoordinator;
import de.mixedfx.network.UDPDetected;
import de.mixedfx.network.messages.WelcomeMessage;
import de.mixedfx.network.user.User;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;

public class NetworkTester
{

	public static void main(final String[] args)
	{
		/*
		 * Set up logging!
		 */

		// Log.network.setLevel(Level.DEBUG);

		// Log fatal errors (network reacted already to this error)
		final NetworkTester rebuild = new NetworkTester();
		AnnotationProcessor.process(rebuild);

		Log.network.info("NOW TESTING NETWORK!");

		// Log UDP members
		UDPCoordinator.allAdresses.addListener((ListChangeListener<UDPDetected>) c ->
		{
			c.next();
			if (c.wasAdded())
				Log.network.trace("AllAddresses updated: " + c.getAddedSubList().get(0));
		});

		// Log TCP members
		NetworkManager.t.tcpClients.addListener((ListChangeListener<TCPClient>) c ->
		{
			c.next();
			if (c.wasRemoved())
				for (final TCPClient tcp1 : c.getRemoved())
					Log.network.trace("TCP removed: " + tcp1);
			else if (c.wasAdded())
				for (final TCPClient tcp2 : c.getAddedSubList())
					Log.network.debug("TCP added: " + tcp2);
		});

		final ConnectivityManager<User> con = new ConnectivityManager<User>(new User()
		{
			private String id;

			{
				this.id = UUID.randomUUID().toString();
			}

			@Override
			public Object getIdentifier()
			{
				return this.id;
			}

			@Override
			public void mergeMe(final User newUser)
			{
			}

			@Override
			public void setMeUp()
			{
			}
		});

		// Log users
		con.otherUsers.addListener((ListChangeListener<User>) c ->
		{
			while (c.next())
			{
				if (c.wasAdded())
				{
					final User user = c.getAddedSubList().get(0);
					Log.network.info((!c.wasReplaced() ? "New" : "Updated") + " User: " + user);
					Log.network.info("User was detected to be in this network: " + user.networks);

					final OverlayNetwork network = user.networks.get(0);
					Log.network.info("Network " + network + " has latency " + network.latencyProperty().get() + " ms!");
					Log.network.info("Network " + network + " has reliability of " + Math.round(network.reliablityProperty().get() * 100) + "%!");
					network.latencyProperty().addListener((ChangeListener<Number>) (observable, oldValue, newValue) -> Log.network.info("Network " + network + " has latency " + newValue.intValue() + " ms!"));
					network.reliablityProperty().addListener((ChangeListener<Number>) (observable, oldValue, newValue) -> Log.network.info("Network " + network + " has reliability of " + Math.round(newValue.doubleValue() * 100) + "%!"));

					user.networks.addListener((ListChangeListener<OverlayNetwork>) c1 ->
					{
						c1.next();
						if (c1.wasAdded())
						{
							Log.network.info("User joined over other network: " + c1.getAddedSubList().get(0) + " so that he is now in following networks: " + user.networks);
							final OverlayNetwork network1 = c1.getAddedSubList().get(0);
							network1.latencyProperty().addListener((ChangeListener<Number>) (observable, oldValue, newValue) -> Log.network.info("Network " + network1 + " has latency " + newValue.intValue() + " ms!"));
							network1.reliablityProperty().addListener((ChangeListener<Number>) (observable, oldValue, newValue) -> Log.network.info("Network " + network1 + " has reliability of " + Math.round(newValue.doubleValue() * 100) + "%!"));
						} else
							Log.network.info("User left over this network: " + c1.getRemoved().get(0) + " but is still in: " + user.networks);
					});
					final WelcomeMessage message = new WelcomeMessage();
					message.setReceivers(user);
					MessageBus.send(message);
				} else if (c.wasRemoved())
					Log.network.info("Removed User: " + c.getRemoved().get(0));
			}

			Log.network.info("UserManager list changed to: " + con.otherUsers);
		});

		MessageBus.registerForReceival(message ->
		{
			if (message instanceof WelcomeMessage)
				Log.network.info("YEAH! WelcomeMessage received!");
		} , true);

		/*
		 * Set up network!
		 */

		// Create example user
		con.start();
		try
		{
			Thread.sleep(Ran.dom(30000, 30000));
		} catch (final InterruptedException e)
		{
		}
		// ConnectivityManager.stop();
		// try
		// {
		// Thread.sleep(Ran.dom(10000, 30000));
		// } catch (InterruptedException e)
		// {
		// }
		// ConnectivityManager.restart();
		while (true)
			;
	}

	@EventTopicSubscriber(topic = NetworkManager.NETWORK_FATALERROR)
	public void on(final String topic, final Exception data)
	{
		Log.network.fatal("NETWORK FATAL ERROR: " + data);
	}

}
