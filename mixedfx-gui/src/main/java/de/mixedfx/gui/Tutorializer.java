package de.mixedfx.gui;

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
import org.controlsfx.control.PopOver;
import org.controlsfx.control.PopOver.ArrowLocation;

import java.util.*;

/**
 * @author Jerry
 */
public class Tutorializer {
    public static BooleanProperty active = new SimpleBooleanProperty(false);

    private static int currentIndex = 0;

    private static ArrowLocation calculateSpace(final Node spaceConsumer) {
        final ArrowLocation result = ArrowLocation.TOP_CENTER;

        final ArrowLocation[] directions =
                {ArrowLocation.RIGHT_CENTER, ArrowLocation.LEFT_CENTER, ArrowLocation.BOTTOM_CENTER, ArrowLocation.TOP_CENTER};

        double left = 0;
        double right = 0;
        double top = 0;
        double bottom = 0;

        left = spaceConsumer.localToScene(0, 0).getX();

        final double maxX = spaceConsumer.getScene().getWidth();
        final double sMaxX = spaceConsumer.localToScene(0, 0).getX() + spaceConsumer.getBoundsInLocal().getWidth();
        right = maxX - sMaxX;

        top = spaceConsumer.localToScene(0, 0).getY();

        final double maxY = spaceConsumer.getScene().getHeight();
        final double sMaxY = spaceConsumer.localToScene(0, 0).getY() + spaceConsumer.getBoundsInLocal().getHeight();
        bottom = maxY - sMaxY;

        final List<Double> values = new ArrayList<>();
        values.add(left);
        values.add(right);
        values.add(top);
        values.add(bottom);
        final double max = Collections.max(values);
        for (int i = 0; i < values.size(); i++)
            if (values.get(i) == max)
                return directions[i];
        return result;
    }

    public static void startTutorial(final Scene scene, final List<Node> tutorialNodes) {
        Tutorializer.startTutorial(scene, tutorialNodes, null);
    }

    /**
     * Two things have to be done first, before you can start/stop the tutorial:
     * <ol>
     * <li>Add <b>tutorial</b> to the style classes of the node!</li>
     * <li>In FXML just add userdata via the tag: <b>tutorialNr="1"</b> with 1 being the number of its introduction. In Java code add to {@link Node#getProperties()} the <b>key "tutorialNr" and the
     * value "1"</b>.</li>
     * </ol>
     * <p>
     * If the scene is resized or escape button was clicked the tutorial stops!
     *
     * @param scene         The scene on which this tutorial shall be done. Only children of this scene are looked up for this tutorial. Nevertheless everything is blurred!
     * @param tutorialNodes A list of nodes which represent the content of the popover. They must be in the same order as the numbers of the marked nodes.
     * @param tutorialDone  If tutorial stopped this Runnable is called from FXThread! Stopping can be initiated by clicking stop hyperlink, resizing the window or pressing escape
     */
    public static void startTutorial(final Scene scene, final List<Node> tutorialNodes, final Runnable tutorialDone) {
        if (scene == null) {
            Log.assets.error("Tutorial can be only started after rootNode is part of the scene!");
            return;
        }
        if (Tutorializer.active.get()) {
            Log.assets.error("A tutorial is still active!");
            return;
        }
        Tutorializer.active.set(true);

        final Node rootNode = scene.getRoot();

        // Get all relevant nodes
        final Set<Node> nodes = rootNode.lookupAll(".tutorial");

        // Check if nodes also have the needed user data!
        final List<Node> verifiedNodes = new ArrayList<>();
        for (final Node node : nodes)
            if (node.getProperties().containsKey("tutorialNr")) {
                try {
                    Integer.valueOf(String.valueOf(node.getProperties().get("tutorialNr")));
                    verifiedNodes.add(node);
                } catch (final Exception e) {
                    Log.assets.warn("The value " + node.getProperties().get("tutorialNr")
                            + " of the key \"tutorialNr\" as user data of an element is not in correct format! It can only be processed if it is an Integer or a String representation of an Integer!");
                }
            }
            else
                Log.assets.warn("A \"tutorial\" element has not the user data key \"tutorialNr\"! It can't be processed!");

        // Sort the list of Nodes by index!
        final Comparator<Node> comparator = (o1, o2) ->
        {
            final int firstNr = Integer.valueOf((String) o1.getProperties().get("tutorialNr"));
            final int secondNr = Integer.valueOf((String) o2.getProperties().get("tutorialNr"));
            return firstNr - secondNr;
        };
        Collections.sort(verifiedNodes, comparator);

        if (verifiedNodes.size() < 1) {
            Log.assets.warn("Tutorial has no nodes on which a tutorial can be shown!");
            return;
        }
        if (tutorialNodes.size() < verifiedNodes.size()) {
            Log.assets.warn("The tutorial nodes must be of the same or higher size as the marked nodes!");
            return;
        }

        // Start Tutorial
        // Consume all Events
        final EventHandler<MouseEvent> mouseConsumer = event -> event.consume();
        final EventHandler<KeyEvent> keyConsumer = event -> event.consume();
        rootNode.addEventFilter(MouseEvent.ANY, mouseConsumer);
        rootNode.addEventFilter(KeyEvent.ANY, keyConsumer);

        // Set up PopOver
        final PopOver popOver = new PopOver();
        popOver.setArrowLocation(ArrowLocation.TOP_CENTER);
        popOver.setAutoHide(false);
        popOver.setDetachable(false);
        popOver.setConsumeAutoHidingEvents(true);

        // Set up PopOver content
        final ObjectProperty<Node> popContent = new SimpleObjectProperty<>();
        final MagicPane contentPane = new MagicPane(popContent);

        // Set up PopOver tutorial control
        final Hyperlink backButton = new Hyperlink("Zur�ck!");
        final Hyperlink stopButton = new Hyperlink("Tutorial beenden!");
        final Hyperlink goButton = new Hyperlink("Weiter!");
        final BorderPane tutorialBox = new BorderPane();
        tutorialBox.setLeft(backButton);
        tutorialBox.setCenter(stopButton);
        tutorialBox.setRight(goButton);

        // Set PopOvers content node
        final VBox box = new VBox();
        VBox.setVgrow(contentPane, Priority.ALWAYS);
        VBox.setVgrow(tutorialBox, Priority.NEVER);
        box.getChildren().addAll(contentPane, tutorialBox);
        popOver.setContentNode(box);

        // Set up movements
        Tutorializer.currentIndex = 0;
        popContent.addListener((observable, oldValue, newValue) ->
        {
            if (Tutorializer.currentIndex == 0)
                backButton.setVisible(false);
            else if (Tutorializer.currentIndex == 1)
                backButton.setVisible(true);
            if (Tutorializer.currentIndex == (verifiedNodes.size() - 2))
                goButton.setVisible(true);
            else if (Tutorializer.currentIndex == (verifiedNodes.size() - 1))
                goButton.setVisible(false);
        });
        backButton.setOnAction(event ->
        {
            Tutorializer.currentIndex = tutorialNodes.indexOf(popContent.get()) - 1;
            Blurrer.unBlur(verifiedNodes.get(Tutorializer.currentIndex + 1));
            popContent.set(tutorialNodes.get(Tutorializer.currentIndex));
            Blurrer.blur(verifiedNodes.get(Tutorializer.currentIndex));
            popOver.setArrowLocation(Tutorializer.calculateSpace(verifiedNodes.get(Tutorializer.currentIndex)));
            popOver.show(verifiedNodes.get(Tutorializer.currentIndex));
        });
        goButton.setOnAction(event ->
        {
            Tutorializer.currentIndex = tutorialNodes.indexOf(popContent.get()) + 1;
            Blurrer.unBlur(verifiedNodes.get(Tutorializer.currentIndex - 1));
            popContent.set(tutorialNodes.get(Tutorializer.currentIndex));
            Blurrer.blur(verifiedNodes.get(Tutorializer.currentIndex));
            popOver.setArrowLocation(Tutorializer.calculateSpace(verifiedNodes.get(Tutorializer.currentIndex)));
            popOver.show(verifiedNodes.get(Tutorializer.currentIndex));
        });
        final EventHandler<ActionEvent> stopEvent = event ->
        {
            popOver.hide();
            rootNode.removeEventFilter(MouseEvent.ANY, mouseConsumer);
            rootNode.removeEventFilter(KeyEvent.ANY, keyConsumer);
            final int currentIndex = tutorialNodes.indexOf(popContent.get());
            Blurrer.unBlur(verifiedNodes.get(currentIndex));
            Tutorializer.active.set(false);
            if (tutorialDone != null)
                tutorialDone.run();
        };
        popOver.addEventFilter(KeyEvent.KEY_RELEASED, event ->
        {
            if (event.getCode().equals(KeyCode.ESCAPE))
                stopEvent.handle(new ActionEvent());
        });
        // If window is resized the PopOver automatically disappears => Stop whole tutorial!
        final ChangeListener<Number> change = new ChangeListener<Number>() {
            @Override
            public void changed(final ObservableValue<? extends Number> observable, final Number oldValue, final Number newValue) {
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
        popOver.setArrowLocation(Tutorializer.calculateSpace(verifiedNodes.get(0)));
        popOver.show(verifiedNodes.get(0));
    }
}
