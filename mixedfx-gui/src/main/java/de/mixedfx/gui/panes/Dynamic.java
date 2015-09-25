package de.mixedfx.gui.panes;

public interface Dynamic {
    /**
     * Is called synchronously from FXThread if the node starts to be visible.
     */
    public void start();

    /**
     * Is called synchronously from FXThread if the node ends up in invisiblity.
     */
    public void stop();
}
