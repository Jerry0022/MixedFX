package de.mixedfx.gui.superpane;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * <p>
 * This class can be instantiated to get an advanced very flexible and lightweight pane.
 * </p>
 *
 * @author JerryMobil
 */
public class SuperPane extends StackPane
{
	/**
	 * Says how many tasks can be run in parallel. Other tasks will wait.
	 */
	private final static int	taskMaxParallel			= 3;
	/**
	 * Indicates after how many milliseconds a running task shall request to open the LoadScreen
	 */
	private final static int	taskLoadScreenDelayMS	= 400;

	/**
	 * Retrieves and returns the first found parent SuperPane (which doesn't have to be the direct
	 * parent node).
	 *
	 * @param content
	 * @return Returns the SuperPane requested or returns null if there is no SuperPane as (direct
	 *         or indirect) parent
	 */
	public final static SuperPane getMySP(final Node content)
	{
		Parent parent = content.getParent();
		while (parent != null && !(parent instanceof SuperPane))
			parent = parent.getParent();
		return (SuperPane) parent;
	}

	/*
	 * OBJECT INSTANCE DESCRIPTION:
	 */

	private Node							content;
	private Node							loadScreen;

	/**
	 * Indicates whether the Load Screen is open, not if a task is running (because if many short
	 * tasks are running there is no LoadScreen)
	 */
	private AtomicBoolean					loading;

	/**
	 * Handles all jobs which are loaded via {@link #load(Task)}! Executes three workers at the same
	 * time. Others have to wait.
	 */
	private ExecutorService					taskCollector;
	private ArrayList<Task<?>>				taskList;
	private EventHandler<WorkerStateEvent>	taskDoneHandler;

	/**
	 * Initializes the StackPane resizing mechanism and all task related stuff.
	 */
	private SuperPane()
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
					if (!t.isDone())
						allDone = false;

				if (allDone)
					Platform.runLater(() ->
					{
						SuperPane.this.closeLoadScreen();
					});
			}
		};
	}

	/**
	 * See also {@link #SuperPane(Node)}
	 *
	 * @param content
	 * @param loadScreen
	 *            Can be a {@link Dynamic} to be informed about its lifecycle. The Load Screen is
	 *            not part of the scene graph until {SuperPane {@link #load(Task)} is called.
	 */
	public SuperPane(final Node content, final Node loadScreen)
	{
		this();

		// Add content to the pane
		this.content = content;
		this.getChildren().add(this.content);

		// Set Load Screen (is visible if load() is called)
		this.loadScreen = loadScreen;
	}

	/**
	 * <p>
	 * You can get the {@link SuperPane} from {@link SuperPane#getMySP(Node)}
	 * </p>
	 *
	 * Architecture:
	 * <ol>
	 * <li>1st layer: 1 time Content</li>
	 * <li>1 layer: LoadScreen <br>
	 * n layer: 0-n times Dialogue<br>
	 * n layer: 0-n times SplashScreen</li>
	 * </ol>
	 *
	 * Lifecycle:
	 * <ol>
	 * <li>Content is saved at instantiation</li>
	 * <li>Others are only part of the object and therefore the scene graph as long as they are
	 * visible</li>
	 * </ol>
	 *
	 * The LoadScreen is null = No LoadScreen is shown while loading...
	 *
	 * @param content
	 *            The content which shall be shown.
	 */
	public SuperPane(final Node content)
	{
		this(content, null);
	}

	/**
	 * Sets the Background.
	 *
	 * @param background
	 *            In contrast to {@link Region#setBackground(javafx.scene.layout.Background)} this
	 *            can be any Node.
	 */
	public void setBackground(final Node background)
	{
		if (!this.getChildren().get(0).equals(this.content))
			this.getChildren().remove(0);
		this.getChildren().add(0, background);
	}

	/**
	 * Loads a task asynchronously and shows a stopping task animation as an overlay if the task is
	 * running longer than {@link SuperPane#taskMaxParallel}. In each case the content is
	 * unclickable for the time. Doesn't have to be called via FXThread.
	 *
	 * @param task
	 *            The task which shall be executed. Maybe it is not executed immediately because
	 *            more than {@link SuperPane#taskMaxParallel} are running in parallel. But the Load
	 *            Screen is shown all over the time.
	 */
	public void load(final Task<?> task)
	{
		this.taskList.add(task);
		task.setOnSucceeded(this.taskDoneHandler);
		task.setOnCancelled(this.taskDoneHandler);
		task.setOnFailed(this.taskDoneHandler);
		this.taskCollector.execute(task);

		// Start thread to delay/avoid the animation if the task is only a short
		// one
		final Thread t = new Thread(() ->
		{
			try
			{
				SuperPane.this.content.setMouseTransparent(true);
				Thread.sleep(SuperPane.taskLoadScreenDelayMS);
				SuperPane.this.content.setMouseTransparent(false);
				// Show Load Screen only if the task was not a very short
				// one
				if (!task.isDone())
					Platform.runLater(() ->
					{
						SuperPane.this.openLoadScreen();
					});
			}
			catch (final InterruptedException e)
			{
			}
		});
		t.setDaemon(true);
		t.start();
	}

	/**
	 * Opens the load screen. Can be only opened once.
	 */
	private void openLoadScreen()
	{
		// Show Load Screen only if it is not already shown
		if (!this.loading.getAndSet(true))
			if (this.loadScreen != null)
				this.openDynamic(this.loadScreen);
	}

	/**
	 * Closes the load screen.
	 */
	private void closeLoadScreen()
	{
		// Close Load Screen only if it is shown
		if (this.loading.getAndSet(false))
			if (this.loadScreen != null)
				this.closeDynamic(this.loadScreen);
	}

	/**
	 * Opens a new dialogue. Works if a dialogue is opened or not.
	 */
	public void openDialogue(final Node dialogue)
	{
		this.openDynamic(dialogue);
	}

	public void closeDialogue(final Node dialogue)
	{
		this.closeDynamic(dialogue);
	}

	/**
	 * <p>
	 * Opens a Dynamic.
	 * </p>
	 * <p>
	 * Only if the direct node implements {@link Dynamic} it will be informed about the start (after
	 * it was added to the scene graph).
	 * </p>
	 *
	 * @param dynamic
	 *            The node (if {@link Dynamic} it will be informed) which shall be shown on top of
	 *            the SuperPane
	 */
	public void openDynamic(final Node dynamic)
	{
		this.getChildren().add(dynamic);
		this.blurAndDarkenPreLastLayer();

		if (dynamic instanceof Dynamic)
			((Dynamic) dynamic).start();
	}

	/**
	 * Closes a Dynamic or if a parent was added to this SuperPane it closes this one.
	 * <p>
	 * If the Dynamic implements {@link Dynamic} it will be informed about the stop (before it will
	 * be removed from the scene graph).
	 * </p>
	 *
	 * @param dynamic
	 *            The node (if {@link Dynamic} it will be informed) which shall be shown on top of
	 *            the SuperPane
	 */
	public void closeDynamic(final Node dynamic)
	{
		if (dynamic instanceof Dynamic)
			((Dynamic) dynamic).stop();

		Node toDelete = dynamic;
		while (toDelete != null && !this.getChildren().remove(toDelete))
			toDelete = toDelete.getParent();

		this.blurAndDarkenPreLastLayer();
	}

	/**
	 * Blurs and darkens the whole StackPane. First it removes all old overlays. Then maybe adds a
	 * new Overlay as forelast element (only in case the last element is not the content).
	 */
	private void blurAndDarkenPreLastLayer()
	{
		// Removes all old overlays
		this.getChildren().removeAll(this.getChildren().stream().filter(node -> node instanceof OverlayPane).collect(Collectors.toList()));

		// Remove all blur effects
		for (int i = 0; i < this.getChildren().size(); i++)
			this.getChildren().get(i).setEffect(null);

		// Add an overlay only if the last element is not the content
		if (!this.getChildren().get(this.getChildren().size() - 1).equals(this.content))
		{
			// Blur all layer except the last one
			final BoxBlur blurBox = new BoxBlur();
			blurBox.setIterations(3);
			for (int i = 0; i < this.getChildren().size() - 1; i++)
				this.getChildren().get(i).setEffect(blurBox);

			// Add an (unblurred) OverlayPane (to darken) before the last
			// element
			final OverlayPane overlay = new OverlayPane();
			overlay.widthProperty().bind(this.widthProperty());
			overlay.heightProperty().bind(this.heightProperty());

			this.getChildren().add(this.getChildren().size() - 1, overlay);
		}
	}
}
