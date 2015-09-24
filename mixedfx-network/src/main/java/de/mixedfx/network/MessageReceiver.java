package de.mixedfx.network;

import de.mixedfx.network.messages.Message;

@FunctionalInterface
public interface MessageReceiver {
    /**
     * Is called synchronized if a message was received. Therefore Messages
     * will be received through this method in the same order they were
     * received from the network.
     *
     * @param message
     */
    public void receive(Message message);
}