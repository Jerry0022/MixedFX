package de.mixedfx.gui;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

public class EasyModifierEventHandler implements EventHandler<MouseEvent> {
    private final EventHandler<? super MouseEvent> oldMouseEventHandler;
    private final EventHandler<MouseEvent> currentMouseEventHandler;

    public EasyModifierEventHandler(EventHandler<? super MouseEvent> oldMouseEventHandler, EventHandler<MouseEvent> currentMouseEventHandler) {
        this.oldMouseEventHandler = oldMouseEventHandler;
        this.currentMouseEventHandler = currentMouseEventHandler;
    }

    public EventHandler<? super MouseEvent> getOldEventHandler() {
        return oldMouseEventHandler;
    }

    @Override
    public void handle(MouseEvent event) {
        this.currentMouseEventHandler.handle(event);
    }
}
