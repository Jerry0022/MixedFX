package de.mixedfx.gui.panes;

import java.util.Set;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Worker.State;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSException;

/**
 * Use this browser by wrapping it into a pane:
 *
 * <pre>
 * final Browser browser = new Browser(&quot;&lt;ul&gt;&lt;li&gt;&quot; + description + &quot;&lt;/li&gt;&lt;/ul&gt;&quot;);
 * final Pane pane = new Pane(browser);
 * browser.prefWidthProperty().bind(pane.widthProperty());
 * </pre>
 */
public class Browser extends Region
{

	final WebView	webview		= new WebView();
	final WebEngine	webEngine	= this.webview.getEngine();

	public Browser(final String content)
	{
		this.webview.setPrefHeight(5);

		this.setPadding(new Insets(5));
		this.widthProperty().addListener((ChangeListener<Object>) (observable, oldValue, newValue) ->
		{
			final Double width = (Double) newValue;
			System.out.println("Region width changed: " + width);
			Browser.this.webview.setPrefWidth(width);
			Browser.this.adjustHeight();
		});

		this.webview.getEngine().getLoadWorker().stateProperty().addListener((ChangeListener<State>) (arg0, oldState, newState) ->
		{
			if (newState == State.SUCCEEDED)
				Browser.this.adjustHeight();
		});

		// http://stackoverflow.com/questions/11206942/how-to-hide-scrollbars-in-the-javafx-webview
		this.webview.getChildrenUnmodifiable().addListener((ListChangeListener<Node>) change ->
		{
			final Set<Node> scrolls = Browser.this.webview.lookupAll(".scroll-bar");
			for (final Node scroll : scrolls)
				scroll.setVisible(false);
		});

		this.setContent(content);

		this.getChildren().add(this.webview);
	}

	public void setContent(final String content)
	{
		Platform.runLater(() ->
		{
			Browser.this.webEngine.loadContent(Browser.this.getHtml(content));
			Platform.runLater(() -> Browser.this.adjustHeight());
		});
	}

	@Override
	protected void layoutChildren()
	{
		final double w = this.getWidth();
		final double h = this.getHeight();
		this.layoutInArea(this.webview, 0, 0, w, h, 0, HPos.CENTER, VPos.CENTER);
	}

	private void adjustHeight()
	{

		Platform.runLater(() ->
		{
			try
			{
				// "document.getElementById('mydiv').offsetHeight"
				final Object result = Browser.this.webEngine.executeScript("document.getElementById('mydiv').offsetHeight");
				if (result instanceof Integer)
				{
					final Integer i = (Integer) result;
					double height = new Double(i);
					height = height + 20;
					Browser.this.webview.setPrefHeight(height);
					System.out.println("height on state: " + height + " prefh: " + Browser.this.webview.getPrefHeight());
				}
			}
			catch (final JSException e)
			{
				// not important
			}
		});

	}

	private String getHtml(final String content)
	{
		return "<html><body>" + "<div id=\"mydiv\">" + content + "</div>" + "</body></html>";
	}

}