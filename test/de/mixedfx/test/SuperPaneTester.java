package de.mixedfx.test;

import de.mixedfx.assets.ImageProducer;
import de.mixedfx.gui.RegionManipulator;
import de.mixedfx.gui.panes.SuperPane;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class SuperPaneTester extends Application
{

	@Override
	public void start(Stage primaryStage)
	{
		SuperPane superPane = new SuperPane();
		superPane.setBackgroundImage(ImageProducer.getMonoColored(Color.BROWN));
		StackPane node = new StackPane();
		node.setPrefSize(50, 50);
		RegionManipulator.bindBackground(node, ImageProducer.getMonoColored(Color.YELLOW));
		superPane.openDialogueFitParent(node);

		Scene scene = new Scene(superPane, 640, 480);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public static void main(String[] args)
	{
		launch(args);
	}
}
