package de.mixedfx.test;

import de.mixedfx.assets.ImageHandler;
import de.mixedfx.assets.ImageProducer;
import de.mixedfx.gui.EasyModifierConfig;
import de.mixedfx.gui.Layouter;
import de.mixedfx.gui.panes.SuperPane;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class LayoutTester extends Application
{
	public static Scene scene;

	@Override
	public void start(Stage primaryStage) throws Exception
	{

		HBox box = new HBox();

		HBox colouredPane = ImageHandler.getPane(ImageProducer.getMonoColored(Color.RED));
		colouredPane.setPrefSize(100, 100);
		colouredPane.getStyleClass().add("modifiable");
		colouredPane.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				System.out.println("Usual MouseEvent of the node!");
			}
		});

		HBox subBox = new HBox();
		HBox.setHgrow(subBox, Priority.ALWAYS);
		subBox.setPrefSize(50, 50);
		HBox subColouredPane = ImageHandler.getPane(ImageProducer.getMonoColored(Color.BLUE));
		subColouredPane.getStyleClass().add("modifiable");
		subColouredPane.setPrefSize(40, 40);
		Button yesButton = new Button("YEAH!");
		yesButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				System.out.println("yesButton was clicked!");
			}
		});
		SuperPane superPane = new SuperPane(null, new LayoutLoadScreen());
		subBox.getChildren().addAll(subColouredPane, new Label("COOL"), yesButton, superPane);

		box.getChildren().addAll(colouredPane, subBox);
		StackPane root = new StackPane();
		root.getChildren().add(box);

		root.getStylesheets().add(this.getClass().getResource("LayoutStyle.css").toExternalForm());
		Scene scene = new Scene(root, 600, 400);
		LayoutTester.scene = scene;
		Layouter.setLayoutable(superPane, root, new EasyModifierConfig());
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	// System.out.println("Damit jerre merge muss");
	// ddaasdfas/// final LayoutElement<Ia
	// final List<LayoutdE
	// final LayoutElement<String> ll2 = new LayoutElement<>("style",
	// String.class);
	// layoutElements.add(ll2);
	// sysoutasöljdkfaöljskddfdf
	// final LayoutElement<Istegear> color = new
	// LayoutElement<>("meineFarbe", Integer.class);
	// layoutElements.add(color);asd
	// asdfasdfaösldkjfölaksdlökjfasdf
	// // Erstellen eines LayoutManagers mit dem man im LanTool dynamisch
	// das
	// // Layout wechseln kann
	// final LayoutManager lm = new
	// LayoutManager(FileObject.create().setPath("assets\\layouts"),
	// layoutElements, "BlueMoon");
	// System.err.println("HUHUU");
	// // Wenn man das Layoasdfasdfasdfut ändert
	// color.set(new Intege
	// // IRGENDWsdasdfO IM CODE im LanTool
	// final ImageView view = new ImageView();
	// view.imageProperty().bind(ll);

	public static void main(String[] args)
	{
		Application.launch(LayoutTester.class, args);
	}
}
