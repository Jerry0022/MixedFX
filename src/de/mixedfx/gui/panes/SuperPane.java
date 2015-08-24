package de.mixedfx.gui.panes;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import de.mixedfx.gui.RegionManipulator;
import de.mixedfx.inspector.Inspector;
import de.mixedfx.logging.Log;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * <p>
 * This class can be instantiated to get an advanced very flexible and lightweight pane.
 * </p>
 *
 * @author JerryMobil
 */
public class SuperPane extends StackPane
{
	private enum StyleClasses
	{
		BACKGROUND, CONTENT, OVERLAY;

		/**
		 * @return A unique String representation of this enum!
		 */
		public String get()
		{
			return SuperPane.class.getName().replace(".", "-").concat(this.toString());
		}
	}

	/*
	 * Configuration
	 */

	/**
	 * Says how many tasks can be run in parallel. Other tasks will wait. MUST be set before {@link SuperPane} is initialized!
	 */
	public static int taskMaxParallel = 3;

	/**
	 * Indicates after how many milliseconds a running task shall request to open the LoadScreen. MUST be set before {@link SuperPane} is initialized!
	 */
	public static int taskLoadScreenDelayMS = 400;

	/**
	 * Sets the transparency if a dynamic was opened! Value must not be less than 0 or greater than 1. MUST be set before {@link SuperPane} is initialized!
	 */
	public static double opacity = 0.35;

	/*
	 * Find my SuperPane :)
	 */

	/**
	 * Retrieves and returns the first found parent SuperPane (which doesn't have to be the direct parent node).
	 *
	 * @param child
	 *            Any node which is in the scene graph.
	 * @return Returns the SuperPane requested or returns null if there is no SuperPane as (direct or indirect) parent
	 */
	public final static SuperPane getMySP(final Node child)
	{
		Parent parent = child.getParent();
		while ((parent != null) && !(parent instanceof SuperPane))
		{
			parent = parent.getParent();
		}
		return (SuperPane) parent;
	}

	/**
	 * Retrieves and returns the last/uppest found parent SuperPane (which doesn't have to be the direct parent node).
	 *
	 * @param child
	 *            Any node which is in the scene graph.
	 * @return Returns the SuperPane requested or returns null if there is no SuperPane as (direct or indirect) parent.
	 */
	public final static SuperPane getMyUpperMostSP(final Node child)
	{
		SuperPane lastSuperPane = null;
		Parent parent = child.getParent();
		while (parent != null)
		{
			if (parent instanceof SuperPane)
				lastSuperPane = (SuperPane) parent;
			parent = parent.getParent();
		}
		return lastSuperPane;
	}

	/**
	 * @param root
	 *            The top root node.
	 * @return Returns null if there was no SuperPane found in the Scene Graph. Otherwise it returns the top nearest first SuperPane.
	 */
	public final static SuperPane getNearest(final Parent root)
	{
		Parent child = root;
		if (child instanceof SuperPane)
			return (SuperPane) child;
		// Check direct children
		for (final Node node : child.getChildrenUnmodifiable())
			if (node instanceof SuperPane)
				return (SuperPane) node;
		for (final Node node : child.getChildrenUnmodifiable())
		{
			if (node instanceof Parent)
			{
				// Check indirect children
				child = SuperPane.getNearest((Parent) node);
				if (child instanceof SuperPane)
					return (SuperPane) child;
			}
		}
		return null;
	}

	/*
	 * OBJECT INSTANCE
	 */

	private Node	content;
	private Node	loadScreen;

	/**
	 * Indicates whether the Load Screen is open, not if a task is running (because if many short tasks are running there is no LoadScreen)
	 */
	private AtomicBoolean	loading;
	private Runnable		onLoadingDone;

	/**
	 * Handles all jobs which are loaded via {@link #load(Task)}! Executes three workers at the same time. Others have to wait.
	 */
	private ExecutorService					taskCollector;
	private ArrayList<Task<?>>				taskList;
	private EventHandler<WorkerStateEvent>	taskDoneHandler;

	/**
	 * Initializes the StackPane resizing mechanism and all task related stuff.
	 */
	public SuperPane()
	{
		this.setMinSize(0, 0); // Makes the children resizing to fit the parent

		// Initialize the task management
		this.loading = new AtomicBoolean(false);
		this.taskList = new ArrayList<Task<?>>();
		this.taskCollector = Executors.newFixedThreadPool(SuperPane.taskMaxParallel, r ->
		{
			final Thread t = new Thread(r);
			t.setDaemon(true);
			return t;
		});
		this.taskDoneHandler = event ->
		{
			if (event.getEventType().equals(WorkerStateEvent.WORKER_STATE_FAILED) || event.getEventType().equals(WorkerStateEvent.WORKER_STATE_SUCCEEDED) || event.getEventType().equals(WorkerStateEvent.WORKER_STATE_CANCELLED))
			{
				boolean allDone = true;
				for (final Task<?> t : SuperPane.this.taskList)
				{
					if (!t.isDone())
					{
						allDone = false;
					}
				}

				if (allDone)
				{
					Platform.runLater(() ->
					{
						SuperPane.this.closeLoadScreen();
						if (this.onLoadingDone != null)
							this.onLoadingDone.run();
					});
				}
			}
		};
	}

	/**
	 * <p>
	 * Creates an instance of a {@link SuperPane}. In fact is not much more than an (empty) StackPane.
	 * </p>
	 *
	 * <p>
	 * You can get the nearest {@link SuperPane} from {@link SuperPane#getMySP(Node)}!
	 * </p>
	 *
	 * Architecture (may differ from current {@link SuperPane#getChildren()} order, since not all must be inside):
	 * <ol>
	 * <li>Layer 0: Background</li>
	 * <li>Layer 1: Content</li>
	 * <li>Layer 2: LoadScreen <br>
	 * <li>Layer n: {@link Node} with or without {@link Dynamic} being opened with {@link SuperPane#openDynamic(Node)} or {@link SuperPane#openDialogue(Node)}</li>
	 * </ol>
	 *
	 * Lifecycle:
	 * <ol>
	 * <li>All parts are only part of the object once they were added and therefore in the scene graph as long as they are visible</li>
	 * </ol>
	 *
	 * The LoadScreen is null = No LoadScreen is shown while loading...
	 *
	 * @param content
	 *            The content which shall be shown.
	 */
	public SuperPane(final Node content)
	{
		this();
		if (content != null)
		{
			this.setContent(content);
		}
	}

	/**
	 * See also {@link #SuperPane(Node)}
	 *
	 * @param content
	 * @param loadScreen
	 *            Can be a {@link Dynamic} to be informed about its lifecycle. The Load Screen is not part of the scene graph until {SuperPane {@link #load(Task)} is called.
	 */
	public SuperPane(final Node content, final Node loadScreen)
	{
		this();

		if (content != null)
		{
			this.setContent(content);
		}

		// Set Load Screen (is visible if load() is called)
		this.loadScreen = loadScreen;
	}

	/**
	 * Blurs and darkens the whole StackPane. First it removes all old overlays. Then maybe adds a new Overlay as forelast element (only in case the last element is not the content).
	 */
	private void blurAndDarkenPreLastLayer()
	{
		// Removes all old overlays
		this.getChildren().removeAll(this.getChildren().stream().filter(node -> node.getStyleClass().contains(StyleClasses.OVERLAY.get())).collect(Collectors.toList()));

		// Remove all blur effects
		for (int i = 0; i < this.getChildren().size(); i++)
		{
			this.getChildren().get(i).setEffect(null);
		}

		// Add an overlay only if the last element is not the content
		if ((this.content != null) && !this.getChildren().get(this.getChildren().size() - 1).equals(this.content))
		{
			// Blur all layer except the last one
			final BoxBlur blurBox = new BoxBlur();
			blurBox.setIterations(3);
			for (int i = 0; i < (this.getChildren().size() - 1); i++)
			{
				this.getChildren().get(i).setEffect(blurBox);
			}

			// Add an (unblurred) OverlayPane (to darken) before the last
			// element
			final Rectangle overlay = new Rectangle();
			if ((SuperPane.opacity < 0) || (SuperPane.opacity > 1))
				SuperPane.opacity = 0.35;
			overlay.setFill(Color.valueOf("rgba(0, 0, 0, " + SuperPane.opacity + ")"));
			overlay.getStyleClass().add(StyleClasses.OVERLAY.get());
			overlay.widthProperty().bind(this.widthProperty());
			overlay.heightProperty().bind(this.heightProperty());

			this.getChildren().add(this.getChildren().size() - 1, overlay);
		}
	}

	/**
	 * Is the same method like {@link #closeDynamic(Node)}!
	 *
	 * @param dialogue
	 *            The dialogue to close.
	 */
	public void closeDialogue(final Node dialogue)
	{
		this.closeDynamic(dialogue);
	}

	/**
	 * Closes a Dynamic or if a parent was added to this SuperPane it closes this one.
	 * <p>
	 * If the Dynamic implements {@link Dynamic} it will be informed about the stop (before it will be removed from the scene graph).
	 * </p>
	 *
	 * @param dynamic
	 *            The node (if {@link Dynamic} it will be informed) which shall be shown on top of the SuperPane
	 */
	public void closeDynamic(final Node dynamic)
	{
		if (dynamic instanceof Dynamic)
		{
			((Dynamic) dynamic).stop();
		}

		Node toDelete = dynamic;
		while ((toDelete != null) && !this.getChildren().remove(toDelete))
		{
			toDelete = toDelete.getParent();
		}

		this.blurAndDarkenPreLastLayer();
	}

	/**
	 * Closes the load screen.
	 */
	private void closeLoadScreen()
	{
		// Close Load Screen only if it is shown
		if (this.loading.getAndSet(false))
		{
			if (this.loadScreen != null)
			{
				this.closeDynamic(this.loadScreen);
			}
		}
	}

	/**
	 * @return Returns true if at least one Node is open which is not the BACKGROUND or CONTENT node.
	 */
	public boolean isOverlaying()
	{
		return ((this.lookupAll("." + StyleClasses.BACKGROUND).size() + this.lookupAll("." + StyleClasses.CONTENT).size()) != this.getChildren().size());
	}

	/**
	 * Loads a task asynchronously and shows a stopping task animation as an overlay if the task is running longer than {@link SuperPane#taskLoadScreenDelayMS}. In each case the content is unclickable
	 * for the time. Doesn't have to be called via FXThread.
	 *
	 * @param task
	 *            The task which shall be executed. Maybe it is not executed immediately because more than {@link SuperPane#taskMaxParallel} are running in parallel. But the Load Screen is shown all
	 *            over the time.
	 */
	public void load(final Task<?> task)
	{
		this.taskList.add(task);
		task.setOnSucceeded(this.taskDoneHandler);
		task.setOnCancelled(this.taskDoneHandler);
		task.setOnFailed(this.taskDoneHandler);
		this.taskCollector.execute(task);

		// Start thread to delay/avoid the animation if the task is only a short one
		Inspector.runNowAsDaemon(() ->
		{
			try
			{
				if (SuperPane.this.content != null)
					SuperPane.this.content.setMouseTransparent(true);
				Thread.sleep(SuperPane.taskLoadScreenDelayMS);
				if (SuperPane.this.content != null)
					SuperPane.this.content.setMouseTransparent(false);
				// Show Load Screen only if the task was not a very short
				// one
				if (!task.isDone())
				{
					Platform.runLater(() ->
					{
						SuperPane.this.openLoadScreen();
					});
				}
			} catch (final InterruptedException e)
			{
			}
		});
	}

	/**
	 * Opens a new dialogue which does isn't resized to fit the parent. Works if a dialogue is opened or not.
	 *
	 * @param dialogue
	 *            The dialogue to open.
	 */
	public void openDialogue(final Node dialogue)
	{
		this.openDynamic(new StackPane()
		{
			{
				this.getChildren().add(dialogue);
				this.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
			}
		});
	}

	/**
	 * <p>
	 * Opens a Dynamic.
	 * </p>
	 * <p>
	 * Only if the direct node implements {@link Dynamic} it will be informed about the start (after it was added to the scene graph).
	 * </p>
	 *
	 * @param dynamic
	 *            The node (if {@link Dynamic} it will be informed) which shall be shown on top of the SuperPane
	 */
	public void openDynamic(final Node dynamic)
	{
		this.openDynamic(dynamic, true);
	}

	/**
	 * @param dynamic
	 * @param darkenLastLayer
	 *            Only has an effect if there is not already a layer darkened
	 */
	public void openDynamic(final Node dynamic, final boolean darkenLastLayer)
	{
		this.getChildren().add(dynamic);
		if (this.content == null)
			Log.assets.warn("The SuperPane has no content. Therefore nothing will be blurred after opening this Dynamic: " + dynamic);
		if (darkenLastLayer)
			this.blurAndDarkenPreLastLayer();

		if (dynamic instanceof Dynamic)
		{
			((Dynamic) dynamic).start();
		}
	}

	/**
	 * Opens the load screen. Can be only opened once.
	 */
	private void openLoadScreen()
	{
		// Show Load Screen only if it is not already shown
		if (!this.loading.getAndSet(true))
		{
			if (this.loadScreen != null)
			{
				this.openDynamic(this.loadScreen);
			}
		}
	}

	/**
	 * Sets the Background.
	 *
	 * @param background
	 *            In contrast to {@link Region#setBackground(javafx.scene.layout.Background)} this can be any Node.
	 */
	public void setBackgroundImage(final Image background)
	{
		RegionManipulator.bindBackground(this, background);
	}

	/**
	 * Sets the Background.
	 *
	 * @param background
	 *            In contrast to {@link Region#setBackground(javafx.scene.layout.Background)} this can be any Node.
	 */
	public void setBackgroundNode(final Node background)
	{
		this.setBackgroundNode(null);
		background.getStyleClass().add(StyleClasses.BACKGROUND.get());

		// Remove background node
		final List<Node> backgrounds = new ArrayList<>();
		for (final Node node : this.getChildren())
			if (node.getStyleClass().contains(StyleClasses.BACKGROUND.get()))
				backgrounds.add(node);
		this.getChildren().removeAll(backgrounds);

		// Put background before other nodes
		this.getChildren().add(0, background);
	}

	/**
	 * Set the main content node.
	 *
	 * @param content
	 *            The content which shall be shown.
	 */
	public void setContent(final Node content)
	{
		content.getStyleClass().add(StyleClasses.CONTENT.get());

		// Remove content node
		final List<Node> contents = new ArrayList<>();
		for (final Node node : this.getChildren())
			if (node.getStyleClass().contains(StyleClasses.CONTENT.get()))
				contents.add(node);
		this.getChildren().removeAll(contents);

		// If there is a background put content after background before other nodes
		if ((this.getChildren().size() > 0) && this.getChildren().get(0).getStyleClass().contains(StyleClasses.BACKGROUND.get()))
			this.getChildren().add(1, content);
		else
			this.getChildren().add(0, content);

		this.content = content;
	}

	public void setLoadScreen(final Node loadScreen)
	{
		this.loadScreen = loadScreen;
	}

	/**
	 * A runnable which is every time called from the FXThread when the SuperPane finished loading (after the loading pane disappeared)!
	 *
	 * @param onLoadingDone
	 *            Any runnable which is called from the FXThread (therefore do not execute long running tasks here)!
	 */
	public void setOnLoadingDone(final Runnable onLoadingDone)
	{
		this.onLoadingDone = onLoadingDone;
	}
}
