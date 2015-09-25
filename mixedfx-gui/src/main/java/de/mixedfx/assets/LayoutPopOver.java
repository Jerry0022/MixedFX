package de.mixedfx.assets;

import javafx.scene.layout.Background;
import org.controlsfx.control.PopOver;

public class LayoutPopOver extends PopOver {
    /**
     * The last background which the {@link PopOver#getOwnerNode()} has had!
     */
    public Background lastBackground;
}
