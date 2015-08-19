package de.mixedfx.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.controlsfx.control.PopOver;
import org.controlsfx.control.PopOver.ArrowLocation;

import de.mixedfx.gui.panes.MagicPane;
import de.mixedfx.logging.Log;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * 
 * @author Jerry
 */
public class Tutorializer
{
	public static BooleanProperty active = new SimpleBooleanProperty(false);

	private static int currentIndex = 0;

	/**
	 * Two things have to be done first, before you can start/stop the tutorial:
	 * <ol>
	 * <li>Add <b>tutorial</b> to the style classes of the node!</li>
	 * <li>In FXML just add userdata via the tag: <b>tutorialNr="1"</b> with 1 being the number of its introduction. In Java code add to {@link Node#getProperties()} the <b>key "tutorialNr" and the
	 * value "1"</b>.</li>
	 * </ol>
	 * 
	 * If the scene is resized or escape button was clicked the tutorial stops!
	 * 
	 * @param scene
	 *            The scene on which this tutorial shall be done. Only children of this scene are looked up for this tutorial. Nevertheless everything is blurred!
	 * @param tutorialNodes
	 *            A list of nodes which represent the content of the popover. They must be in the same order as the numbers of the marked nodes.
	 */
	public static void startTutorial(Scene scene, List<Node> tutorialNodes)
	{
		if (scene == null)
		{
			Log.assets.error("Tutorial can be only started after rootNode is part of the scene!");
			return;
		}
		if (active.get())
		{
			Log.assets.error("A tutorial is still active!");
			return;
		}
		active.set(true);

		Node rootNode = scene.getRoot();

		// Get all relevant nodes
		Set<Node> nodes = rootNode.lookupAll(".tutorial");

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

		if (verifiedNodes.size() < 1)
		{
			Log.assets.warn("Tutorial has no nodes on which a tutorial can be shown!");
			return;
		}
		if (tutorialNodes.size() < verifiedNodes.size())
		{
			Log.assets.warn("The tutorial nodes must be of the same or higher size as the marked nodes!");
			return;
		}

		// Start Tutorial
		// Consume all Events
		EventHandler<MouseEvent> mouseConsumer = new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				event.consume();
			}
		};
		EventHandler<KeyEvent> keyConsumer = new EventHandler<KeyEvent>()
		{
			@Override
			public void handle(KeyEvent event)
			{
				event.consume();
			}
		};
		rootNode.addEventFilter(MouseEvent.ANY, mouseConsumer);
		rootNode.addEventFilter(KeyEvent.ANY, keyConsumer);

		// Set up PopOver
		PopOver popOver = new PopOver();
		popOver.setArrowLocation(ArrowLocation.TOP_CENTER);
		popOver.setAutoHide(false);
		popOver.setDetachable(false);
		popOver.setConsumeAutoHidingEvents(true);

		// Set up PopOver content
		ObjectProperty<Node> popContent = new SimpleObjectProperty<>();
		MagicPane contentPane = new MagicPane(popContent);

		// Set up PopOver tutorial control
		Hyperlink backButton = new Hyperlink("Zurück!");
		Hyperlink stopButton = new Hyperlink("Tutorial beenden!");
		Hyperlink goButton = new Hyperlink("Weiter!");
		BorderPane tutorialBox = new BorderPane();
		tutorialBox.setLeft(backButton);
		tutorialBox.setCenter(stopButton);
		tutorialBox.setRight(goButton);

		// Set PopOvers content node
		VBox box = new VBox();
		VBox.setVgrow(contentPane, Priority.ALWAYS);
		VBox.setVgrow(tutorialBox, Priority.NEVER);
		box.getChildren().addAll(contentPane, tutorialBox);
		popOver.setContentNode(box);

		// Set up movements
		currentIndex = 0;
		popContent.addListener(new ChangeListener<Node>()
		{
			@Override
			public void changed(ObservableValue<? extends Node> observable, Node oldValue, Node newValue)
			{
				if (currentIndex == 0)
					backButton.setVisible(false);
				else if (currentIndex == 1)
					backButton.setVisible(true);
				if (currentIndex == verifiedNodes.size() - 2)
					goButton.setVisible(true);
				else if (currentIndex == verifiedNodes.size() - 1)
					goButton.setVisible(false);
			}
		});
		backButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				currentIndex = tutorialNodes.indexOf(popContent.get()) - 1;
				Blurrer.unBlur(verifiedNodes.get(currentIndex + 1));
				popContent.set(tutorialNodes.get(currentIndex));
				Blurrer.blur(verifiedNodes.get(currentIndex));
				popOver.setArrowLocation(calculateSpace(verifiedNodes.get(currentIndex)));
				popOver.show(verifiedNodes.get(currentIndex));
			}
		});
		goButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				currentIndex = tutorialNodes.indexOf(popContent.get()) + 1;
				Blurrer.unBlur(verifiedNodes.get(currentIndex - 1));
				popContent.set(tutorialNodes.get(currentIndex));
				Blurrer.blur(verifiedNodes.get(currentIndex));
				popOver.setArrowLocation(calculateSpace(verifiedNodes.get(currentIndex)));
				popOver.show(verifiedNodes.get(currentIndex));
			}
		});
		EventHandler<ActionEvent> stopEvent = new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				popOver.hide();
				rootNode.removeEventFilter(MouseEvent.ANY, mouseConsumer);
				rootNode.removeEventFilter(KeyEvent.ANY, keyConsumer);
				int currentIndex = tutorialNodes.indexOf(popContent.get());
				Blurrer.unBlur(verifiedNodes.get(currentIndex));
				active.set(false);
			}
		};
		popOver.addEventFilter(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>()
		{
			@Override
			public void handle(KeyEvent event)
			{
				if (event.getCode().equals(KeyCode.ESCAPE))
					stopEvent.handle(new ActionEvent());
			}
		});
		// If window is resized the PopOver automatically disappears => Stop whole tutorial!
		ChangeListener<Number> change = new ChangeListener<Number>()
		{
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
			{
				System.out.println("EVENT");
				stopEvent.handle(new ActionEvent());
				scene.widthProperty().removeListener(this);
				scene.heightProperty().removeListener(this);
			}
		};
		scene.widthProperty().addListener(change);
		scene.heightProperty().addListener(change);
		stopButton.setOnAction(stopEvent);

		// Show first
		Blurrer.blur(verifiedNodes.get(0));
		popContent.set(tutorialNodes.get(0));
		popOver.setArrowLocation(calculateSpace(verifiedNodes.get(0)));
		popOver.show(verifiedNodes.get(0));
	}

	private static ArrowLocation calculateSpace(Node spaceConsumer)
	{
		ArrowLocation result = ArrowLocation.TOP_CENTER;

		ArrowLocation[] directions =
		{ ArrowLocation.RIGHT_CENTER, ArrowLocation.LEFT_CENTER, ArrowLocation.BOTTOM_CENTER, ArrowLocation.TOP_CENTER };

		double left = 0;
		double right = 0;
		double top = 0;
		double bottom = 0;

		left = spaceConsumer.localToScene(0, 0).getX();

		double maxX = spaceConsumer.getScene().getWidth();
		double sMaxX = spaceConsumer.localToScene(0, 0).getX() + spaceConsumer.getBoundsInLocal().getWidth();
		right = maxX - sMaxX;

		top = spaceConsumer.localToScene(0, 0).getY();

		double maxY = spaceConsumer.getScene().getHeight();
		double sMaxY = spaceConsumer.localToScene(0, 0).getY() + spaceConsumer.getBoundsInLocal().getHeight();
		bottom = maxY - sMaxY;

		List<Double> values = new ArrayList<>();
		values.add(left);
		values.add(right);
		values.add(top);
		values.add(bottom);
		double max = Collections.max(values);
		for (int i = 0; i < values.size(); i++)
			if (values.get(i) == max)
				return directions[i];
		return result;
	}
}
