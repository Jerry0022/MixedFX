package de.mixedfx.network;

import java.util.Date;

import de.mixedfx.logging.Log;
import de.mixedfx.network.MessageBus.MessageReceiver;
import de.mixedfx.network.ServiceManager.UniqueService;
import de.mixedfx.network.messages.NetworkSinceMessage;
import de.mixedfx.network.messages.RegisteredMessage;

public class NetworkSinceService implements UniqueService, MessageReceiver
{

	@Override
	public void stop()
	{
		NetworkConfig.networkExistsSince.set(null);
	}

	@Override
	public void client()
	{
		MessageBus.registerForReceival(this);
		NetworkSinceMessage message = new NetworkSinceMessage();
		message.receivers.add(ParticipantManager.PARTICIPANT_NUMBER_SERVER);
		MessageBus.send(message);
	}

	@Override
	public void host()
	{
		NetworkConfig.networkExistsSince.set(new Date());
		Log.network.debug("NetworkExistsSince date was set to: " + NetworkConfig.networkExistsSince.get());
	}

	@Override
	public RegisteredMessage hostReceive(RegisteredMessage message)
	{
		if (message instanceof NetworkSinceMessage)
		{
			message.receivers.clear();
			message.receivers.add(message.sender);
			((NetworkSinceMessage) message).networkStartTime = NetworkConfig.networkExistsSince.get();
			return message;
		}
		else
			return null;
	}

	@Override
	public void receive(RegisteredMessage message)
	{
		if (message instanceof NetworkSinceMessage)
		{
			NetworkConfig.networkExistsSince.set(((NetworkSinceMessage) message).networkStartTime);
			Log.network.debug("NetworkExistsSince date was set to: " + NetworkConfig.networkExistsSince.get());
			MessageBus.unregisterForReceival(this);
		}
	}

}
