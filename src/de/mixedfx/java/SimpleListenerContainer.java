package de.mixedfx.java;

import java.util.LinkedList;
import java.util.List;

public class SimpleListenerContainer<EventObject> {
	private List<SimpleListener<EventObject>> listeners;

	public SimpleListenerContainer() {
		this.listeners = new LinkedList<SimpleListener<EventObject>>();
	}

	public void addListener(SimpleListener<EventObject> listener) {
		this.listeners.add(listener);
	}

	public void removeListener(SimpleListener<EventObject> listener) {
		this.listeners.remove(listener);
	}

	public void invokeListeners(EventObject event) {
		for (SimpleListener<EventObject> listener : this.listeners) {
			listener.action(event);
		}
	}

}
