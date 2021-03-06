package de.mixedfx.java;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.util.Duration;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * This class helps to identify changes or visual panes correctly. Or helps simply for testing GUI.
 *
 * @author Jerry
 */
public class Inspector {
    /**
     * This method adds an listener to the node's width and height and shows every change of these values in the console.
     *
     * @param region The Region (a type of {@link Node} which should be observed.
     * @param name   A string to identify the node in the console
     */
    public static void inspectSize(final Region region, final String name) {
        region.widthProperty().addListener((observable, oldValue, newValue) ->
        {
            System.out.println("Width of " + name + " changed to: " + newValue);

        });

        region.heightProperty().addListener((observable, oldValue, newValue) ->
        {
            System.out.println("Height of " + name + " changed to: " + newValue);

        });
    }

    /**
     * This method adds an listener to the node's width and height and shows every change of these values in the console. The displayed name is the id
     * of the object instance.
     *
     * @param region The Region (a type of {@link Node} which should be observed.
     */
    public static void inspectSize(final Region region) {
        Inspector.inspectSize(region, region.toString());
    }

    /**
     * <p>
     * Sets the background of a node to a random color and shows the chosen color in the console. Use the hex color in the console on the website
     * <a href="http://www.css3-generator.de/rgba.html" >http://www.css3-generator.de/rgba.html</a>
     * </p>
     *
     * @param region The node which background shall be modified.
     * @param name   A string to identify the node in the console
     */
    public static void makeBackgroundVisible(final Region region, final String name) {
        final Random rand = new Random(System.currentTimeMillis());

        final int r = rand.nextInt(255);
        final int g = rand.nextInt(255);
        final int b = rand.nextInt(255);

        final String randomColorRGBA = "rgba(" + r + ", " + g + ", " + b + ", " + "1.0)";
        region.setStyle("-fx-background-color: " + randomColorRGBA + ";");

        final String randomColorHEX = String.format("#%02X%02X%02X", r, g, b);

        System.out.println("Background color of " + name + " set to: " + randomColorRGBA + " or " + randomColorHEX);
    }

    /**
     * <p>
     * Sets the background of a node to a random color and shows the chosen color in the console. The displayed name is the id of the object instance.
     * Use the hex color in the console on the website <a href="http://www.css3-generator.de/rgba.html" >http://www.css3-generator.de/rgba.html</a>
     * </p>
     *
     * @param region The node which background shall be modified.
     */
    public static void makeBackgroundVisible(final Region region) {
        Inspector.makeBackgroundVisible(region, region.toString());
    }

    /**
     * Runs a Runnable later in the JavaFX GUI thread. Waiting time is asynchronous.
     *
     * @param toRun      The commands to be executed in GUI thread.
     * @param timeToWait The Duration this command should wait asynchronous before executing the Runnable on GUI thread.
     */
    public static void runFXLater(final Runnable toRun, final Duration timeToWait) {
        new Thread(() ->
        {
            try {
                Thread.sleep((long) timeToWait.toMillis());
            } catch (final InterruptedException ignored) {
            }
            Platform.runLater(toRun);
        }).start();
    }

    /**
     * Runs a Runnable later in the JavaFX GUI thread. Waiting time is asynchronous. Default timeToWait of 3 seconds.
     *
     * @param toRun The commands to be executed in GUI thread.
     */
    public static void runFXLater(final Runnable toRun) {
        Inspector.runFXLater(toRun, Duration.seconds(3));
    }

    /**
     * Runs a Runnable later in a new thread. Waits some time before executing the Runnable.
     *
     * @param toRun      The commands to be executed in a separate thread.
     * @param timeToWait The Duration this command should wait asynchronous before executing the Runnable in this new thread.
     */
    public static void runLater(final Runnable toRun, final Duration timeToWait) {
        Inspector.runNowAsDaemon(() ->
        {
            try {
                Thread.sleep((long) timeToWait.toMillis());
            } catch (final InterruptedException ignored) {
            }
            toRun.run();
        });
    }

    /**
     * Runs a Runnable later in a new thread. Waiting time is asynchronous. Default timeToWait of 3 seconds.
     *
     * @param toRun The commands to be executed in the new thread.
     */
    public static void runLater(final Runnable toRun) {
        Inspector.runLater(toRun, Duration.seconds(3));
    }

    /**
     * Runs a Runnable later in a new thread. Waiting time is asynchronous. Default timeToWait of 3 seconds.
     *
     * @param toRun The commands to be executed in the new thread.
     */
    public static void runNowAsDaemon(final Runnable toRun) {
        final Thread newThread = new Thread(toRun);
        newThread.setDaemon(true);
        newThread.start();
    }

    /**
     * @return Returns a single thread executor which starts threads as daemon
     */
    public static ExecutorService getThreadExecutor() {
        return Executors.newSingleThreadExecutor(Inspector.getThreadFactory());

    }

    /**
     * @return Returns a thread factory which produces threads as daemons
     */
    public static ThreadFactory getThreadFactory() {
        return runnable ->
        {
            final Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            return thread;
        };
    }

    /**
     * Sleeps a duration and ignores {@link InterruptedException}!
     *
     * @param duration The duration to sleep.
     */
    public static void sleep(Duration duration) {
        try {
            Thread.sleep((long) duration.toMillis());
        } catch (InterruptedException ignored) {
        }
    }

    public static void endlessSleep() {
        while (true)
            sleep(Duration.seconds(10));
    }
}
