package de.mixedfx.network;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ListChangeListener;

import org.apache.commons.collections.CollectionUtils;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;

import de.mixedfx.network.SyncedManager.SyncedInterface;
import de.mixedfx.network.messages.Message;
import de.mixedfx.network.messages.UserMessage;

/**
 * In contrary to the {@link ParticipantManager#PARTICIPANTS} this class manages real persons, not
 * only applications connected to the network. Therefore each attribute should be seen as user
 * related.
 *
 * @author Jerry
 *
 */
public class UserManager implements SyncedInterface
{
	public static User						myUser;
	public static final ListProperty<User>	list	= new SimpleListProperty<>();

	public UserManager()
	{
		// TODO Auto-generated constructor stub
	}

	@Override
	public void client()
	{
		// TODO Fill myUser
		// Send message with my profile to UserManager host
		MessageBus.send(new UserMessage(UserManager.myUser));
		AnnotationProcessor.process(this);
	}

	private ListChangeListener<Integer>	participants;

	@Override
	public void host()
	{
		// TODO Add me?
		this.participants = (ListChangeListener<Integer>) c ->
		{
			while (c.next())
			{
				// Add all newcomers as unknown
				for (final Integer i : c.getAddedSubList())
					if (!CollectionUtils.exists(UserManager.list, UserPredicates.toApachePredicate(UserPredicates.getByPID(i))))
						System.out.println("User, die ich noch nicht habe: " + i);
				// TODO Add as unknown user!

				// Remove all lost ones
				for (final Integer i : c.getRemoved())
					UserManager.list.removeIf(UserPredicates.getByPID(i));
			}
		};

		ParticipantManager.PARTICIPANTS.addListener(this.participants);
		AnnotationProcessor.process(this);
	}

	@EventTopicSubscriber(topic = MessageBus.MESSAGE_RECEIVE)
	public void get(final String topic, final Message message)
	{
		if (message instanceof UserMessage)
		{

		}
	}

	@Override
	public void stop()
	{
		UserManager.list.clear();
		ParticipantManager.PARTICIPANTS.removeListener(this.participants);
		AnnotationProcessor.unprocess(this);
	}
}
