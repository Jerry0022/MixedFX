package de.mixedfx.gui;

import java.io.File;

import org.controlsfx.control.PopOver.ArrowLocation;

import de.mixedfx.assets.ImageHandler;
import de.mixedfx.assets.ImageProducer;
import de.mixedfx.file.FileObject;
import de.mixedfx.gui.panes.SuperPane;
import de.mixedfx.logging.Log;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;

public class Layouter
{

	/**
	 * @param pane
	 *            Superpane which will be used for load() tasks while saving and as root for all children which might can be layouted!
	 * @param config
	 */
	public static void setLayoutable(SuperPane pane, LayoutManager2 layoutManager, EasyModifierConfig config)
	{
		Layouter.setLayoutable(pane, pane, layoutManager, config);
	}

	/**
	 * Should be called after all initializations of the scene graphs' nodes are done. Otherwise may buttons and clickable elements still work! Makes a root and all of its children which have some
	 * config modifiable! Other nodes are not clickable while in modifying mode!
	 * 
	 * @param paneToShowSaving
	 *            On this pane load() is called if a new layout background is saved!
	 * @param root
	 *            Itself and all of its children are scanned if they are layoutables!
	 * @param config
	 *            The config for this layout!
	 */
	public static void setLayoutable(SuperPane paneToShowSaving, Parent root, LayoutManager2 layoutManager, EasyModifierConfig config)
	{
		if (layoutManager.root == null)
			layoutManager.root = root;

		/*
		 * Set up PopOver
		 */
		LayoutPopOver popOver = new LayoutPopOver();
		popOver.setArrowLocation(ArrowLocation.TOP_CENTER);
		popOver.setAutoHide(true);
		popOver.setAutoFix(true);
		popOver.setDetachable(false);
		popOver.showingProperty().addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
			{
				if (!newValue)
					Platform.runLater(() ->
					{
						Region parent = (Region) popOver.getOwnerNode();
						// Just save if background changed!
						if (!parent.getBackground().equals(popOver.lastBackground))
						{
							Image image = parent.getBackground().getImages().get(0).getImage();
							paneToShowSaving.load(new Task<Void>()
							{
								@Override
								protected Void call() throws Exception
								{
									System.out.println("BACKGROUND CHANGED! Save it by using Superpane.load()!");
									layoutManager.saveElement(parent.getId(), image);
									System.out.println("BACKGROUND SAVED!");
									return null;
								}
							});
						}
					});
			}
		});

		/*
		 * Let PopOver disappear as soon as somewhere else is clicked!
		 */
		root.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				if (popOver.isShowing())
					popOver.hide();
			}
		});

		EasyModifierHandler handler = new EasyModifierHandler()
		{
			@Override
			public void modify(Parent parent, boolean doIt)
			{
				if (!(parent instanceof Region))
				{
					Log.assets.warn("Can layout only Regions but this Parent is marked with the style class but not a Region: " + parent);
					return;
				}

				Region region = (Region) parent;

				EventHandler<MouseEvent> event = new EventHandler<MouseEvent>()
				{
					@Override
					public void handle(MouseEvent event)
					{
						Log.assets.trace("Clicked on a dynamically modifable element!");

						/*
						 * Initialize PopOver content!
						 */
						HBox toolBox = new HBox();
						Button button = new Button("Bild auswählen!");
						ColorPicker picker = new ColorPicker();
						picker.showingProperty().addListener(new ChangeListener<Boolean>()
						{
							@Override
							public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
							{
								popOver.setAutoHide(!newValue);
							}
						});
						picker.setOnAction(new EventHandler<ActionEvent>()
						{
							@Override
							public void handle(ActionEvent event)
							{
								RegionManipulator.bindBackground(region, ImageProducer.getMonoColored(picker.getValue()));
							}
						});
						toolBox.getChildren().addAll(picker, button);
						popOver.setContentNode(toolBox);
						button.setOnAction(new EventHandler<ActionEvent>()
						{
							@Override
							public void handle(ActionEvent event)
							{
								popOver.setAutoHide(false);
								FileChooser imageChooser = new FileChooser();
								imageChooser.setTitle("Bild auswählen!");
								File selected = imageChooser.showOpenDialog(popOver);
								RegionManipulator.bindBackground(region, ImageHandler.readImage(FileObject.create(selected)));
								popOver.setAutoHide(true);
							}
						});

						/*
						 * Show PopOver!
						 */
						popOver.lastBackground = region.getBackground();
						popOver.show(region, event.getScreenX(), event.getScreenY());
						event.consume();
					}
				};

				if (doIt)
				{
					region.setOnMouseClicked(new EasyModifierEventHandler(region.getOnMouseClicked(), event));
				} else
				{
					region.setOnMouseClicked(((EasyModifierEventHandler) region.getOnMouseClicked()).getOldEventHandler());
				}
			}
		};

		EasyModifier.init(root, config, handler, null);
	}
}
