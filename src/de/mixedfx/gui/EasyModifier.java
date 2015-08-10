package de.mixedfx.gui;

import java.util.ArrayList;
import java.util.List;

import de.mixedfx.logging.Log;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class EasyModifier
{
	/**
	 * Trigger to go in modifying mode is a boolean change!
	 * 
	 * @param root
	 *            The root to manipulate, all listeners and events of all sub nodes will still work.
	 * @param config
	 *            The config to be used!
	 * @param handler
	 *            The handler which shall apply as long as the root is in modifying node! DoIt signalizes if the Parent is in modifying mode ({@link EasyModifierConfig#trigger} is constantly pressed).
	 */
	public static void init(Parent root, BooleanProperty trigger, EasyModifierConfig config, EasyModifierHandler handler)
	{
		EasyModifierHandler styleHandler = new EasyModifierHandler()
		{
			@Override
			public void modify(Parent parent, boolean doIt)
			{
				if (doIt)
				{
					parent.getStyleClass().add(config.dynamicClass);
				} else
				{
					parent.getStyleClass().remove(config.dynamicClass);
				}
			}
		};

		trigger.addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
			{
				runOnAllSubNodes(root, config.staticClass, newValue, styleHandler, handler);
			}
		});
	}

	/**
	 * Trigger to go in modifying mode is a key pressed or released on the full root!
	 * 
	 * @param root
	 *            The root to manipulate, all listeners and events of all sub nodes will still work.
	 * @param triggerKey
	 *            The key which shall trigger this action! Null means any key.
	 * @param config
	 *            The config to be used!
	 * @param handler
	 *            The handler which shall apply as long as the root is in modifying node! DoIt signalizes if the Parent is in modifying mode ({@link EasyModifierConfig#trigger} is constantly pressed).
	 */
	public static void init(Parent root, EasyModifierConfig config, EasyModifierHandler handler, KeyCode triggerKey)
	{
		BooleanProperty trigger = new SimpleBooleanProperty();
		EasyModifier.init(root, trigger, config, handler);

		root.setOnKeyPressed(new EventHandler<KeyEvent>()
		{
			public void handle(KeyEvent ke)
			{
				if (triggerKey == null || ke.getCode().equals(triggerKey))
				{
					trigger.set(true);
				}
			}
		});
		root.setOnKeyReleased(new EventHandler<KeyEvent>()
		{
			public void handle(KeyEvent ke)
			{
				if (triggerKey == null || ke.getCode().equals(triggerKey))
				{
					trigger.set(false);
				}
			}
		});
	}

	/**
	 * Returns a list of nodes which match the style class as well as they must have an id (which id doesn't matter)!
	 * 
	 * @param root
	 *            The starting node!
	 * @param styleClass
	 *            The css style class which must be actively applied to each node as identifier which nodes shall be modified!
	 * @param doIt
	 *            This value will be forwarded to {@link EasyModifierHandler#modify(Parent, boolean)}!
	 * @param modifier
	 *            If not null, {@link EasyModifierHandler#modify(Parent, boolean)} is called on all matching Parents. If null no actions are taken, use the returned list instead.
	 * @return Returns a list of all matched childrens and childrens of childrens etc. (including the root).
	 */
	public static List<Parent> runOnAllSubNodes(Parent root, String styleClass, boolean doIt, EasyModifierHandler... modifier)
	{
		return runOnAllSubNodes(root, styleClass, doIt, new ArrayList<>(), modifier);
	}

	private static List<Parent> runOnAllSubNodes(Parent root, String styleClass, boolean doIt, List<Parent> emptyList, EasyModifierHandler... modifier)
	{
		if (root.getStyleClass().contains(styleClass))
			if (root.getId() != "")
			{
				if (modifier != null)
					for (EasyModifierHandler mod : modifier)
						mod.modify(root, doIt);
				emptyList.add(root);
			} else
			{
				Log.assets.warn("The node " + root + " has no id but has the identifying style class " + styleClass + ". A modifying operation can only perform if the id is uniquely filled.");
			}

		if (root.getChildrenUnmodifiable().size() > 0)
		{
			for (Node node : root.getChildrenUnmodifiable())
			{
				if (node instanceof Parent)
				{
					runOnAllSubNodes((Parent) node, styleClass, doIt, emptyList, modifier);
				} else if (root.getStyleClass().contains(styleClass))
					Log.assets.warn("The node " + node + " is not a Parent but has the identifying style class " + styleClass + ". Only javafx.scene.Parent are supported!");
			}
		}
		return emptyList;
	}

}
