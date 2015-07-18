package de.mixedfx.gui.panes;

import javafx.animation.Transition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import de.mixedfx.gui.RegionManipulator;

/**
 * A Pane which contains to side by side panes. First only the mainScreen is shown, after
 * {@link #showDetailed()} is called only the detailed screen is visible. This can be undone with
 * {@link #showMain()}. It is based on the Apple iOS 8 screen change animation.
 *
 * @author Jerry
 *
 */
public class SlidePane extends ScrollPane
{
	private final Region			mainScreen;
	private final Region			detailedScreen;

	private final SlideTransition	slideAnimation;
	public final DoubleProperty		slidingFrac;

	public SlidePane(final Region mainScreen, final Region detailedScreen, final Duration duration)
	{
		// Remove focussable border from ScrollPane
		this.getStylesheets().add(this.getClass().getResource("/de/mixedfx/gui/panes/SlidePane.css").toExternalForm());

		// Remove ScrollBars
		this.setHbarPolicy(ScrollBarPolicy.NEVER);
		this.setVbarPolicy(ScrollBarPolicy.NEVER);

		final StackPane largePane = new StackPane();
		largePane.setMinSize(0, 0);
		RegionManipulator.bindAllHeight(largePane, this.heightProperty());
		RegionManipulator.bindAllWidth(largePane, this.widthProperty());

		this.mainScreen = mainScreen;
		this.detailedScreen = detailedScreen;

		this.slideAnimation = new SlideTransition(duration);
		this.slidingFrac = new SimpleDoubleProperty();

		// Start positioning the DetailedScreen outside the pane on the right side
		this.mainScreen.translateXProperty().bind(this.mainScreen.widthProperty().multiply(this.slidingFrac).multiply(-1));
		this.detailedScreen.translateXProperty().bind(this.mainScreen.widthProperty().add(this.mainScreen.translateXProperty()));

		largePane.getChildren().add(this.mainScreen);
		largePane.getChildren().add(this.detailedScreen);

		this.setContent(largePane);
	}

	public void showMain()
	{
		this.slideAnimation.setRate(-1.0);
		if (this.slidingFrac.get() != 0)
		{
			this.slideAnimation.play();
		}
	}

	public void showDetailed()
	{
		this.slideAnimation.setRate(1.0);

		if (this.slidingFrac.get() != 1)
		{
			this.slideAnimation.play();
		}
	}

	private class SlideTransition extends Transition
	{
		public SlideTransition(final Duration duration)
		{
			this.setCycleDuration(duration);
		}

		@Override
		protected void interpolate(final double frac)
		{
			SlidePane.this.slidingFrac.set(frac);
		}

	}
}
