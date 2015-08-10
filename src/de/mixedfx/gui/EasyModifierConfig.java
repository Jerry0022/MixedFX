package de.mixedfx.gui;

import javafx.scene.Parent;

public class EasyModifierConfig
{
	/**
	 * The style class which marks all the {@link Parent} who shall be modifiable. Default is "modifiable".
	 */
	public String staticClass;

	/**
	 * The style class which shall apply to all Parents with {@link #staticClass} as long as {@link #trigger} is pressed. Default is "modifying". Use also "DYNAMICCLASS:hover" to set up hover events
	 * while modifying!
	 */
	public String dynamicClass;

	public EasyModifierConfig()
	{
		this.staticClass = "modifiable";
		this.dynamicClass = "modifying";
	}
}
