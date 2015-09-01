package de.mixedfx.gui;

import javafx.scene.Parent;

public class EasyModifierConfig
{
	/**
	 * The style class which marks all the {@link Parent} who shall be modifiable. Default is "modifiable".
	 */
	public String staticClass;

	/**
	 * The style class which shall apply to all Parents with {@link #staticClass}. Default is "modifying". Use also "DYNAMICCLASS:hover" to set up hover events while modifying!
	 */
	public String dynamicClass;

	/**
	 * If false a key typed event enables or disables the modify mode. If true as long as the key is typed the modify mode is enabled.
	 */
	public boolean keyPressed;

	public EasyModifierConfig()
	{
		this.staticClass = "modifiable";
		this.dynamicClass = "modifying";
		this.keyPressed = false;
	}
}
