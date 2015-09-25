package de.mixedfx.network;

import de.mixedfx.eventbus.EventBusExtended;
import de.mixedfx.inspector.Inspector;
import de.mixedfx.network.messages.IdentifiedMessage;
import de.mixedfx.network.messages.Message;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.util.ArrayList;

/**
 * @author Jerry
 */
@ApplicationScoped
public class MessageBus {
	public static final String MESSAGE_RECEIVE = "MESSAGE_RECEIVE";
	@Inject
	ConnectivityManager<?> cm;
	private MessageBus intermediateReceiver;
	private ArrayList<Object> receiverList = new ArrayList<>();

    /**
     * To undo this use
     * {@link MessageBus#unregisterForReceival(MessageReceiver)}. Done with
     * {@link WeakReference}.
     *
     * @param receiver
     *            Receiver will be informed asynchronously!
     * @param strongly
     *            Set true if you want to use this as an Anonymous Inner Object!
     */
    public synchronized void registerForReceival(final MessageReceiver receiver, final boolean strongly) {
	if (this.receiverList.isEmpty()) {
	    this.intermediateReceiver = new MessageBus();
	    AnnotationProcessor.process(this.intermediateReceiver);
	}

	if (strongly) {
	    this.receiverList.add(receiver);
	} else {
		this.receiverList.add(new WeakReference<>(receiver));
	}
    }

    /**
     * @param message
     *            Message to send.
     */
    public synchronized void send(final Message message) {
	final Runnable run = () -> {
	    if (message instanceof IdentifiedMessage) {
		final IdentifiedMessage idMessage = (IdentifiedMessage) message;
		synchronized (this.cm.tcp_user_map()) {
		    if (idMessage.getToUserIDs().isEmpty()) {
			for (final InetAddress ip : this.cm.tcp_user_map().keySet()) {
			    message.setToIP(ip);
			    EventBusExtended.publishAsyncSafe(Connection.MESSAGE_CHANNEL_SEND, message);
			}
		    } else {
			for (final Object id : idMessage.getToUserIDs()) {
			    FindIP: for (final InetAddress ip : this.cm.tcp_user_map().keySet()) {
				if (this.cm.tcp_user_map().get(ip).getOriginalUser().getIdentifier().equals(id)) {
				    message.setToIP(ip);
				    EventBusExtended.publishAsyncSafe(Connection.MESSAGE_CHANNEL_SEND, message);
				    break FindIP; // Avoid double sending!
				}
			    }
			}
		    }
		}
	    } else {
		EventBusExtended.publishAsyncSafe(Connection.MESSAGE_CHANNEL_SEND, message);
	    }
	};
	if (Thread.holdsLock(this.cm.tcp_user_map())) {
	    Inspector.runNowAsDaemon(run);
	} else {
	    run.run();
	}
    }

    /**
     * Works silently. If receiver is not registered this method returns without
     * throwing an exception. Also if receiver didn't subscribe strongly.
     *
     * @param receiver
     */
    public synchronized void unregisterForReceival(final MessageReceiver receiver) {
	this.receiverList.remove(receiver);

	if (this.receiverList.isEmpty()) {
	    this.intermediateReceiver = null;
	    AnnotationProcessor.unprocess(this.intermediateReceiver);
	}
    }

    @EventTopicSubscriber(topic = MESSAGE_RECEIVE)
    public void getMessage(final String topic, final Message message) {
	for (final Object receiver : this.receiverList) {
	    if (receiver instanceof MessageReceiver) {
		((MessageReceiver) receiver).receive(message);
	    } else if ((receiver instanceof WeakReference)
		    && (((WeakReference<MessageReceiver>) receiver).get() != null)) {
		((WeakReference<MessageReceiver>) receiver).get().receive(message);
	    }
	}
    }
}
