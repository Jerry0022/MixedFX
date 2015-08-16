package de.mixedfx.gui;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

public class Blurrer
{
	private final static String				STYLECLASS_EXCEPT	= Blurrer.class.getName().replace(".", "-").concat("EXCEPTME");
	private final static String				STYLECLASS_BLURRED	= Blurrer.class.getName().replace(".", "-").concat("EFFECT");
	private static EventHandler<MouseEvent>	mouseCatcher;

	static
	{
		Blurrer.mouseCatcher = new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				event.consume();
			}
		};
	}

	public static void blur(Node exceptMe)
	{
		// Catch all Mouse Events!
		exceptMe.getScene().addEventFilter(MouseEvent.ANY, mouseCatcher);

		// Blur and darken effects
		final BoxBlur effect = new BoxBlur();
		effect.setIterations(3);
		ColorAdjust darker = new ColorAdjust();
		darker.setBrightness(-0.6);
		effect.setInput(darker);

		// Apply effects on all elements except the one to show
		exceptMe.getStyleClass().add(STYLECLASS_EXCEPT);
		Node parent = exceptMe.getParent();
		while (parent != null)
		{
			if (parent instanceof Parent)
			{
				for (Node child : ((Parent) parent).getChildrenUnmodifiable())
				{
					if (!(child instanceof StackPane) && child.lookup("." + STYLECLASS_EXCEPT) == null)
					{
						child.getStyleClass().add(STYLECLASS_BLURRED);
						child.setEffect(effect);
					}
				}
			}
			parent = parent.getParent();
		}
		exceptMe.getStyleClass().remove(STYLECLASS_EXCEPT);
	}

	public static void unBlur(Node exceptMe)
	{
		// Catch all Mouse Events!
		exceptMe.getScene().removeEventFilter(MouseEvent.ANY, mouseCatcher);

		Node parent = exceptMe.getParent();
		while (parent != null)
		{
			if (parent instanceof Parent)
			{
				for (Node child : ((Parent) parent).getChildrenUnmodifiable())
				{
					if (!(child instanceof StackPane) && child.getStyleClass().contains(STYLECLASS_BLURRED))
					{
						child.setEffect(null);
					}
				}
			}
			parent = parent.getParent();
		}
	}
}
