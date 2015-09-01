package de.mixedfx.gui;

import javafx.scene.Parent;

/**
 * Triggers once!
 * 
 * @author Jerry
 *
 */
public interface EasyModifierHandler
{
	/**
	 * Triggers once!
	 * 
	 * @param parent
	 *            The Parent who has a not null id and the matching {@link EasyModifierConfig#staticClass}!
	 * @param doIt
	 *            If true, trigger was pressed, otherwise it was released!
	 */
	public void modify(Parent parent, boolean doIt);
}
