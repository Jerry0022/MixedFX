package de.mixedfx.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.controlsfx.control.PopOver;

import de.mixedfx.logging.Log;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.text.Text;

/**
 * Two things have to be done first, before you can start/stop the tutorial:
 * <ol>
 * <li>Add "tutorial" to the style classes of the node!</li>
 * <li>In FXML just add userdata via the tag: <b>tutorialNr="1"</b> with 1 being the number of its introduction. In Java code add to {@link Node#getProperties()} the <b>key "tutorialNr" and the value
 * "1"</b>.</li>
 * </ol>
 * 
 * The numbers must begin from 1 and no number must not be omitted.
 * 
 * @author Jerry
 */
public class Tutorializer
{
	public static void startTutorial(Scene scene)
	{
		// Get all relevant nodes
		Set<Node> nodes = scene.getRoot().lookupAll(".tutorial");

		// Check if nodes also have the needed user data!
		List<Node> verifiedNodes = new ArrayList<>();
		for (Node node : nodes)
			if (node.getProperties().containsKey("tutorialNr"))
			{
				try
				{
					Integer.valueOf(String.valueOf(node.getProperties().get("tutorialNr")));
					verifiedNodes.add(node);
				} catch (Exception e)
				{
					Log.assets.warn("The value " + node.getProperties().get("tutorialNr")
							+ " of the key \"tutorialNr\" as user data of an element is not in correct format! It can only be processed if it is an Integer or a String representation of an Integer!");
				}
			} else
				Log.assets.warn("A \"tutorial\" element has not the user data key \"tutorialNr\"! It can't be processed!");

		// Sort the list of Nodes by index!
		Comparator<Node> comparator = new Comparator<Node>()
		{
			@Override
			public int compare(Node o1, Node o2)
			{
				int firstNr = Integer.valueOf((String) o1.getProperties().get("tutorialNr"));
				int secondNr = Integer.valueOf((String) o2.getProperties().get("tutorialNr"));
				return firstNr - secondNr;
			}
		};
		Collections.sort(verifiedNodes, comparator);

		// Start Tutorial
		PopOver popOver = new PopOver(new Button());
		popOver.setContentNode(new Text("Explanation"));
		popOver.show(new Button());
	}

	public static void stopTutorial()
	{

	}
}
