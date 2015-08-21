package de.mixedfx.assets;

import java.io.File;

import org.controlsfx.control.PopOver.ArrowLocation;

import de.mixedfx.file.FileObject;
import de.mixedfx.gui.EasyModifier;
import de.mixedfx.gui.EasyModifierConfig;
import de.mixedfx.gui.EasyModifierEventHandler;
import de.mixedfx.gui.EasyModifierHandler;
import de.mixedfx.gui.RegionManipulator;
import de.mixedfx.gui.panes.SuperPane;
import de.mixedfx.inspector.Inspector;
import de.mixedfx.logging.Log;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
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
	public static void setLayoutable(final SuperPane pane, final LayoutManager layoutManager, final EasyModifierConfig config)
	{
		Layouter.setLayoutable(pane, pane, layoutManager, config);
	}

	/**
	 * Should be called after all initializations of the scene graphs' nodes are done. Otherwise may buttons and clickable elements still work! Makes a root and all of its children which have some
	 * config modifiable! Other nodes are not clickable while in modifying mode!
	 *
	 * @param paneToShowSaving
	 *            On this pane load() is called if a new layout background is saved! May be null, then the layout is saved in the background asynchronously without visuals.
	 * @param root
	 *            Itself and all of its children are scanned if they are layoutables! Overwrites the settings of LayoutManager! Must not be null!
	 * @param layoutManager
	 *            The LayoutManager to use. Must not be null!
	 * @param config
	 *            The config for this layout! If null a default config is loaded.
	 */
	public static void setLayoutable(final SuperPane paneToShowSaving, final Parent root, final LayoutManager layoutManager, EasyModifierConfig config)
	{
		if ((root == null) || (layoutManager == null))
			throw new NullPointerException();

		layoutManager.root = root;
		layoutManager.register(config);

		if (config == null)
			config = new EasyModifierConfig();

		/*
		 * Set up PopOver
		 */
		final LayoutPopOver popOver = new LayoutPopOver();
		popOver.setArrowLocation(ArrowLocation.TOP_CENTER);
		popOver.setAutoHide(true);
		popOver.setAutoFix(true);
		popOver.setDetachable(false);
		popOver.showingProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) ->
		{
			if (!newValue)
				Platform.runLater(() ->
				{
					final Region parent = (Region) popOver.getOwnerNode();
					// Just save if background changed!
					if ((parent.getBackground() != null) && !parent.getBackground().equals(popOver.lastBackground))
					{
						final Image image = parent.getBackground().getImages().get(0).getImage();
						final Task<Void> task = new Task<Void>()
						{
							@Override
							protected Void call() throws Exception
							{
								layoutManager.saveElement(parent.getId(), image);
								Log.assets.trace("Background of node with id " + parent.getId() + " was saved! SuperPane was informed!");
								return null;
							}
						};
						if (paneToShowSaving != null)
							paneToShowSaving.load(task);
						else
							Inspector.runNowAsDaemon(task);
					}
				});
		});

		/*
		 * Let PopOver disappear as soon as somewhere else is clicked!
		 */
		root.addEventFilter(MouseEvent.MOUSE_CLICKED, event ->
		{
			if (popOver.isShowing())
				popOver.hide();
		});

		final EasyModifierHandler handler = (parent, doIt) ->
		{
			if (!(parent instanceof Region))
			{
				Log.assets.warn("Can layout only Regions but this Parent is marked with the style class but not a Region: " + parent);
				return;
			}

			final Region region = (Region) parent;

			final EventHandler<MouseEvent> event = event2 ->
			{
				Log.assets.trace("Clicked on a dynamically modifable element with id " + region.getId() + "!");

				/*
				 * Initialize PopOver content!
				 */
				final HBox toolBox = new HBox();
				final Button button = new Button("Bild auswählen!");
				final ColorPicker picker = new ColorPicker();
				picker.showingProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> popOver.setAutoHide(!newValue));
				picker.setOnAction(event1 -> RegionManipulator.bindBackground(region, ImageProducer.getMonoColored(picker.getValue())));
				toolBox.getChildren().addAll(picker, button);
				popOver.setContentNode(toolBox);
				button.setOnAction(event1 ->
				{
					popOver.setAutoHide(false);
					final FileChooser imageChooser = new FileChooser();
					imageChooser.setTitle("Bild auswählen!");
					final File selected = imageChooser.showOpenDialog(popOver);
					RegionManipulator.bindBackground(region, ImageHandler.readImage(FileObject.create(selected)));
					popOver.setAutoHide(true);
				});

				/*
				 * Show PopOver!
				 */
				popOver.lastBackground = region.getBackground();
				popOver.show(region, event2.getScreenX(), event2.getScreenY());
				event2.consume();
			};

			if (doIt)
			{
				region.setOnMouseClicked(new EasyModifierEventHandler(region.getOnMouseClicked(), event));
			} else
			{
				region.setOnMouseClicked(((EasyModifierEventHandler) region.getOnMouseClicked()).getOldEventHandler());
			}
		};

		EasyModifier.init(root, KeyCode.CONTROL, config, handler);
	}
}
