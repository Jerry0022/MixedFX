package de.mixedfx.gui;

import org.controlsfx.control.PopOver;

import javafx.scene.layout.Background;

public class LayoutPopOver extends PopOver
{
	/**
	 * The last background which the {@link PopOver#getOwnerNode()} has had!
	 */
	public Background lastBackground;
}
