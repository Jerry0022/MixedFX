package de.mixedfx.gui.controls;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

import java.lang.reflect.Field;

/**
 * Created by Jerry on 12.10.2015.
 */
public class TooltipStartTimer {
    public static void doIt(Tooltip tooltip, Duration duration) {
        try {
            final Field fieldBehavior = tooltip.getClass().getDeclaredField("BEHAVIOR");
            fieldBehavior.setAccessible(true);
            final Object objBehavior = fieldBehavior.get(tooltip);

            final Field fieldTimer = objBehavior.getClass().getDeclaredField("activationTimer");
            fieldTimer.setAccessible(true);
            final Timeline objTimer = (Timeline) fieldTimer.get(objBehavior);

            objTimer.getKeyFrames().clear();
            objTimer.getKeyFrames().add(new KeyFrame(duration));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
