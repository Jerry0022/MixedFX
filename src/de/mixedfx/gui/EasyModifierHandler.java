package de.mixedfx.gui;

import javafx.scene.Parent;

/**
 * Triggers once if {@link EasyModifierConfig#trigger} is pressed and released!
 * 
 * @author Jerry
 *
 */
public interface EasyModifierHandler
{
	/**
	 * Triggers once if {@link EasyModifierConfig#trigger}! If released triggers again.
	 * 
	 * @param parent
	 *            The Parent who has a not null id and the matching {@link EasyModifierConfig#staticClass}!
	 * @param doIt
	 *            If true, trigger was pressed, otherwise it was released!
	 */
	public void modify(Parent parent, boolean doIt);
}
