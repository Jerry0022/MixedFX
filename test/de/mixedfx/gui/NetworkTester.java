package de.mixedfx.gui;

import java.util.UUID;

import org.apache.logging.log4j.Level;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;

import de.mixedfx.logging.Log;
import de.mixedfx.network.ConnectivityManager;
import de.mixedfx.network.NetworkManager;
import de.mixedfx.network.ParticipantManager;
import de.mixedfx.network.ServiceManager;
import de.mixedfx.network.user.User;
import de.mixedfx.network.user.UserManager;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;

public class NetworkTester
{

	public static void main(final String[] args)
	{
		// Log fatal errors (network reacted already to this error)
		AnnotationProcessor.process(new ConnectivityManager());

		// Log Participants
		ParticipantManager.PARTICIPANTS.addListener((ListChangeListener<Integer>) c ->
		{
			Log.network.info("Participants changed to: " + ParticipantManager.PARTICIPANTS);
		});

		// Log ConnectivityManager.status
		ConnectivityManager.status.addListener((ChangeListener<ConnectivityManager.Status>) (observable, oldValue, newValue) ->
		{
			Log.network.info("ConnectivityManager status changed from " + oldValue.toString().toUpperCase() + " to " + newValue.toString().toUpperCase());
		});

		// Create example user
		final String id = UUID.randomUUID().toString();
		final ExampleUser user = new ExampleUser(id.substring(id.length() - 7, id.length()));

		// Register UserManager services
		final UserManager<ExampleUser> userManager = new UserManager<ExampleUser>(user);
		UserManager.allUsers.addListener((ListChangeListener<User>) c ->
		{
			Log.network.info("UserManager list changed to: " + UserManager.allUsers);
		});
		ServiceManager.register(userManager);

		// Register example unique service
		ServiceManager.register(new ExampleUniqueService());

		// Start network activity and immediately after that start TCP as host.
		ConnectivityManager.force();

		// Don't let the application stop itself!
		while (true)
		{
			;
		}
	}

	@EventTopicSubscriber(topic = NetworkManager.NETWORK_FATALERROR)
	public void error(final String topic, final Exception exception)
	{
		Log.network.catching(Level.ERROR, exception);
	}
}
