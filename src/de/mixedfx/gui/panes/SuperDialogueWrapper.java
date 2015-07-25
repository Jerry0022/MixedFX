package de.mixedfx.gui.panes;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;

/**
 * Just to identify a dialogue.
 * 
 * @author Jerry
 *
 */
public class SuperDialogueWrapper extends StackPane implements Dynamic
{
	private Node child;

	public SuperDialogueWrapper(Node child)
	{
		this.child = child;
		this.getChildren().add(this.child);
	}

	@Override
	public void start()
	{
		if (this.child instanceof Dynamic)
			((Dynamic) this.child).start();
	}

	@Override
	public void stop()
	{
		if (this.child instanceof Dynamic)
			((Dynamic) this.child).stop();
	}
}
