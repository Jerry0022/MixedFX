package de.mixedfx.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.controlsfx.control.PopOver.ArrowLocation;

import de.mixedfx.assets.ImageHandler;
import de.mixedfx.file.FileObject;
import de.mixedfx.gui.panes.SuperPane;
import de.mixedfx.logging.Log;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;

public class EasyModifier
{

	public static void setLayoutable(SuperPane pane, EasyModifierConfig config)
	{
		setLayoutable(pane, pane, config);
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
	public static void setLayoutable(SuperPane paneToShowSaving, Parent root, EasyModifierConfig config)
	{
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
				Platform.runLater(() ->
				{
					Region parent = (Region) popOver.getOwnerNode();
					// Just save if background changed!
					if (!newValue && !parent.getBackground().equals(popOver.lastBackground))
					{
						paneToShowSaving.load(new Task<Void>()
						{
							@Override
							protected Void call() throws Exception
							{
								System.out.println("BACKGROUND CHANGED! Save it by using Superpane.load()!");
								Thread.sleep(4000);
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
								region.setBackground(new Background(new BackgroundFill(picker.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));
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

		EasyModifier.init(root, config, handler);
	}

	/**
	 * @param root
	 *            The root to manipulate, all listeners and events of all sub nodes will still work.
	 * @param config
	 *            The config to be used!
	 * @param handler
	 *            The handler which shall apply as long as the root is in modifying node! DoIt signalizes if the Parent is in modifying mode ({@link EasyModifierConfig#trigger} is constantly pressed).
	 */
	public static void init(Parent root, EasyModifierConfig config, EasyModifierHandler handler)
	{
		EasyModifierHandler styleHandler = new EasyModifierHandler()
		{
			@Override
			public void modify(Parent parent, boolean doIt)
			{
				if (doIt)
				{
					parent.getStyleClass().add(config.dynamicClass);
				} else
				{
					parent.getStyleClass().remove(config.dynamicClass);
				}
			}
		};

		root.setOnKeyPressed(new EventHandler<KeyEvent>()
		{
			public void handle(KeyEvent ke)
			{
				if (config.trigger == null || ke.getCode().equals(config.trigger))
				{
					runOnAllSubNodes(root, config.staticClass, true, styleHandler, handler);
				}
			}
		});
		root.setOnKeyReleased(new EventHandler<KeyEvent>()
		{
			public void handle(KeyEvent ke)
			{
				if (config.trigger == null || ke.getCode().equals(config.trigger))
				{
					runOnAllSubNodes(root, config.staticClass, false, styleHandler, handler);
				}
			}
		});
	}

	/**
	 * Returns a list of nodes which match the style class as well as they must have an id (which id doesn't matter)!
	 * 
	 * @param root
	 *            The starting node!
	 * @param styleClass
	 *            The css style class which must be actively applied to each node as identifier which nodes shall be modified!
	 * @param doIt
	 *            This value will be forwarded to {@link EasyModifierHandler#modify(Parent, boolean)}!
	 * @param modifier
	 *            If not null, {@link EasyModifierHandler#modify(Parent, boolean)} is called on all matching Parents. If null no actions are taken, use the returned list instead.
	 * @return Returns a list of all matched childrens and childrens of childrens etc. (including the root).
	 */
	public static List<Parent> runOnAllSubNodes(Parent root, String styleClass, boolean doIt, EasyModifierHandler... modifier)
	{
		return runOnAllSubNodes(root, styleClass, doIt, new ArrayList<>(), modifier);
	}

	private static List<Parent> runOnAllSubNodes(Parent root, String styleClass, boolean doIt, List<Parent> emptyList, EasyModifierHandler... modifier)
	{
		if (root.getStyleClass().contains(styleClass))
			if (root.getId() != "")
			{
				if (modifier != null)
					for (EasyModifierHandler mod : modifier)
						mod.modify(root, doIt);
				emptyList.add(root);
			} else
			{
				Log.assets.warn("The node " + root + " has no id but has the identifying style class " + styleClass + ". A modifying operation can only perform if the id is uniquely filled.");
			}

		if (root.getChildrenUnmodifiable().size() > 0)
		{
			for (Node node : root.getChildrenUnmodifiable())
			{
				if (node instanceof Parent)
				{
					runOnAllSubNodes((Parent) node, styleClass, doIt, emptyList, modifier);
				} else if (root.getStyleClass().contains(styleClass))
					Log.assets.warn("The node " + node + " is not a Parent but has the identifying style class " + styleClass + ". Only javafx.scene.Parent are supported!");
			}
		}
		return emptyList;
	}

}
