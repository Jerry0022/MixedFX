package de.mixedfx.test;

import java.util.ArrayList;
import java.util.List;

import org.controlsfx.control.PopOver;
import org.controlsfx.control.PopOver.ArrowLocation;

import de.mixedfx.assets.ImageHandler;
import de.mixedfx.assets.ImageProducer;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class LayoutGUI extends Application {
	private PopOver popOver = new PopOver();
	private FileChooser imageChooser = new FileChooser();

	@Override
	public void start(Stage primaryStage) throws Exception {

		HBox box = new HBox();

		HBox colouredPane = ImageHandler.getPane(ImageProducer.getMonoColored(Color.RED));
		colouredPane.setPrefSize(100, 100);
		colouredPane.getStyleClass().add("lay");
		colouredPane.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				System.out.println("Usual MouseEvent of the node!");
			}
		});

		HBox subBox = new HBox();
		HBox.setHgrow(subBox, Priority.ALWAYS);
		subBox.setPrefSize(50, 50);
		HBox subColouredPane = ImageHandler.getPane(ImageProducer.getMonoColored(Color.BLUE));
		subColouredPane.getStyleClass().add("lay");
		subColouredPane.setPrefSize(40, 40);
		subBox.getChildren().addAll(subColouredPane, new Label("COOL"));

		box.getChildren().addAll(colouredPane, subBox);
		StackPane root = new StackPane();
		root.getChildren().add(box);

		/*
		 * TODO Input must be a Stage! TODO Save all events on scene and trigger them after or before my events!
		 */
		Scene scene = new Scene(root, 600, 400);
		scene.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (popOver.isShowing())
					popOver.hide();
			}
		});
		scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
			public void handle(KeyEvent ke) {
				List<Parent> layoutables = getLayoutables(root, new ArrayList<>(), "lay");
				for (Parent node : layoutables) {
					node.getStyleClass().add("layoutable");
					EventHandler<?> mouseEvent = node.getOnMouseClicked();
					// TODO save this to hashmap with node id
					node.setOnMouseClicked(new EventHandler<MouseEvent>() {
						@Override
						public void handle(MouseEvent event) {
							System.out.println("CLICK!");

							HBox toolBox = new HBox();
							ColorPicker picker = new ColorPicker();
							picker.valueProperty().addListener(new ChangeListener<Color>() {
								@Override
								public void changed(ObservableValue<? extends Color> value, Color oldColor, Color newColor) {
									((Region) node).setBackground(new Background(new BackgroundFill(newColor, CornerRadii.EMPTY, Insets.EMPTY)));
									// TODO Save to Layout!
								}
							});
							imageChooser.setTitle("Öffne Bilddatei");
							Button button = new Button("Bilddatei auswählen!");
							button.setOnAction(new EventHandler<ActionEvent>() {
								@Override
								public void handle(ActionEvent event) {
									popOver.setAutoHide(false);
									imageChooser.showOpenDialog(popOver);
									popOver.setAutoHide(true);
								}
							});
							toolBox.getChildren().addAll(picker, button);
							popOver.setContentNode(toolBox);
							popOver.setArrowLocation(ArrowLocation.TOP_CENTER);
							popOver.setAutoHide(true);
							popOver.setDetachable(false);
							popOver.setConsumeAutoHidingEvents(true);
							popOver.show(root, event.getScreenX(), event.getScreenY());

							event.consume();
						}
					});
				}
			}
		});
		scene.setOnKeyReleased(new EventHandler<KeyEvent>() {
			public void handle(KeyEvent ke) {
				List<Parent> layoutables = getLayoutables(root, new ArrayList<>(), "lay");
				for (Parent node : layoutables) {
					node.getStyleClass().remove("layoutable");
					// TODO Load mouse event from hashmap with mouse id
					node.setOnMouseClicked(null);
				}
			}
		});
		scene.getStylesheets().add(this.getClass().getResource("LayoutStyle.css").toExternalForm());
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	/**
	 * Returns a list of nodes which match the style class as well as they must have an id (which one doesn't matter)!
	 * 
	 * @param root
	 *            The starting node!
	 * @param emptyList
	 *            An empty list which is filled with the matched nodes.
	 * @param styleClass
	 *            The css style class which must be actively applied to each node!
	 * @return Returns a list of all matched childrens and childrens of childrens!
	 */
	public static List<Parent> getLayoutables(Parent root, List<Parent> emptyList, String styleClass) {
		if (root.getStyleClass().contains(styleClass) && root.getId() != "")
			emptyList.add(root);
		// TODO Log warning if id is not available but styleclass
		if (root.getChildrenUnmodifiable().size() > 0) {
			for (Node node : root.getChildrenUnmodifiable()) {
				if (node instanceof Parent) {
					getLayoutables((Parent) node, emptyList, styleClass);
				}
			}
		}
		return emptyList;
	}

	public static void main(String[] args) {
		Application.launch(LayoutGUI.class, args);
	}
}
