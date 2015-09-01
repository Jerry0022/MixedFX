package de.mixedfx.gui.panes;

/* 
 * Copyright 2014 Jens Deters.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.util.Duration;

/**
 * @author Jens Deters
 * modified by Jerry
 * 
 * PROPOSAL: Add max value to slide, not only to "replace" side one
 * PROPOSAL: Or take minimum size for that
 */
public class _SplitPaneDividerSlider {

    public enum Direction {

        UP, DOWN, LEFT, RIGHT;
    }

    private Direction direction;
    private final SplitPane splitPane;
    private final int dividerIndex;
    private BooleanProperty aimContentVisibleProperty;
    private DoubleProperty lastDividerPositionProperty;
    private DoubleProperty currentDividerPositionProperty;
    private Region content;
    private double contentInitialMinWidth;
    private double contentInitialMinHeight;
    private SlideTransition slideTransition;
    private Duration cycleDuration;
    private SplitPane.Divider dividerToMove;

    public _SplitPaneDividerSlider(SplitPane splitPane, int dividerIndex, Direction direction) {
        this(splitPane, dividerIndex, direction, Duration.millis(7000.0));
    }

    public _SplitPaneDividerSlider(SplitPane splitPane,
            int dividerIndex,
            Direction direction,
            Duration cycleDuration) {
        this.direction = direction;
        this.splitPane = splitPane;
        this.dividerIndex = dividerIndex;
        this.cycleDuration = cycleDuration;
        init();
    }

    private void init() {
        slideTransition = new SlideTransition(cycleDuration);

        // figure out right splitpane content
        switch (direction) {
            case LEFT:
            case UP:
                content = (Region) splitPane.getItems().get(dividerIndex);
                break;
            case RIGHT:
            case DOWN:
                content = (Region) splitPane.getItems().get(dividerIndex + 1);
                break;
        }
        contentInitialMinHeight = content.getMinHeight();
        contentInitialMinWidth = content.getMinWidth();
        dividerToMove = splitPane.getDividers().get(dividerIndex);

        aimContentVisibleProperty().addListener((ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) -> {
            if (!newValue) {
                // store divider position before transition:
                setLastDividerPosition(splitPane.getDividers().get(dividerIndex).getPosition());
                // "arm" current divider position before transition:
                setCurrentDividerPosition(getLastDividerPosition());
            }
            content.setMinSize(0.0, 0.0);
            slideTransition.init();
            slideTransition.play();
        });
    }

    private void restoreContentSize() {
        content.setMinHeight(contentInitialMinHeight);
        content.setMinWidth(contentInitialMinWidth);
        setCurrentDividerPosition(getLastDividerPosition());
    }

    public BooleanProperty aimContentVisibleProperty() {
        if (aimContentVisibleProperty == null) {
            aimContentVisibleProperty = new SimpleBooleanProperty(true);
        }
        return aimContentVisibleProperty;
    }

    public void setAimContentVisible(boolean aimContentVisible) {
        aimContentVisibleProperty().set(aimContentVisible);
    }

    public boolean isAimContentVisible() {
        return aimContentVisibleProperty().get();
    }

    public DoubleProperty lastDividerPositionProperty() {
        if (lastDividerPositionProperty == null) {
            lastDividerPositionProperty = new SimpleDoubleProperty();
        }
        return lastDividerPositionProperty;
    }

    public double getLastDividerPosition() {
        return lastDividerPositionProperty().get();
    }

    public void setLastDividerPosition(double lastDividerPosition) {
        lastDividerPositionProperty().set(lastDividerPosition);
    }

    public DoubleProperty currentDividerPositionProperty() {
        if (currentDividerPositionProperty == null) {
            currentDividerPositionProperty = new SimpleDoubleProperty();
        }
        return currentDividerPositionProperty;
    }

    public double getCurrentDividerPosition() {
        return currentDividerPositionProperty().get();
    }

    public void setCurrentDividerPosition(double currentDividerPosition) {
        currentDividerPositionProperty().set(currentDividerPosition);
        dividerToMove.setPosition(currentDividerPosition);
    }
   
    public void setOnFinished(EventHandler<ActionEvent> eventHandler)
    {
    	slideTransition.setOnFinished(eventHandler);
    }

    private class SlideTransition extends Transition {
    	private double difference;
    	
        public SlideTransition(final Duration cycleDuration) {
            setCycleDuration(cycleDuration);
            setInterpolator(Interpolator.LINEAR);
            System.out.println(
            		((Pane)splitPane.getItems().get(1)).getPrefWidth()
            				);
        }
        
        public void init()
        {
        	double dockedSliderPosition = 0.0;
            switch (direction) {
	            case LEFT:
	            case UP:
	            	if(dividerIndex == 0)
	            		dockedSliderPosition = 0.0;
	            	else
	            		dockedSliderPosition = splitPane.getDividers().get(dividerIndex-1).getPosition();
                    break;
	            case RIGHT:
	            case DOWN:
	            	if(dividerIndex == splitPane.getDividers().size()-1)
	            		dockedSliderPosition = 1.0;
	            	else
	            		dockedSliderPosition = splitPane.getDividers().get(dividerIndex+1).getPosition();
                break;
            }
            this.difference = Math.abs(getLastDividerPosition() - dockedSliderPosition);
        }

        @Override
        protected void interpolate(double d) {
            switch (direction) {
                case LEFT:
                case UP:
                    // intent to slide in content:  
                    if (isAimContentVisible()) {
                        if ((getCurrentDividerPosition() + d*this.difference) <= getLastDividerPosition()) {
                        	setCurrentDividerPosition(getLastDividerPosition() + d*this.difference);
                        } else { //DONE
                            restoreContentSize();
                            stop();
                        }
                    } // intent to slide out content:  
                    else {
                        if (getCurrentDividerPosition() > 0.0) {
                         	setCurrentDividerPosition(getLastDividerPosition() - d*this.difference);
                        } else { //DONE
                            setCurrentDividerPosition(0.0);
                            stop();
                        }
                    }
                    break;
                case RIGHT:
                case DOWN:
                    // intent to slide in content:  
                    if (isAimContentVisible()) {
                        if ((getCurrentDividerPosition() - d*this.difference) >= getLastDividerPosition()) {
                        	setCurrentDividerPosition(getLastDividerPosition() - d*this.difference);
                        } else { //DONE
                        	// ACTIVATE
//                        	System.err.println(getCurrentDividerPosition());
//                        	System.err.println(getLastDividerPosition());
//                        	System.err.println(this.difference);
//                        	setCurrentDividerPosition(getLastDividerPosition() - (1-d)*this.difference);
                            restoreContentSize();
//                            System.err.println(getCurrentDividerPosition());
                            stop();
                        }
                    } // intent to slide out content:  
                    else {
                        if (getCurrentDividerPosition() < 1.0) {
//                        	System.err.println("HÄ2?");
                        	setCurrentDividerPosition(getLastDividerPosition() + d*this.difference);
                        } else {//DONE
//                        	System.err.println("HÄ?");
                            setCurrentDividerPosition(1.0);
                            stop();
                        }
                    }
                    break;
        	}
        }
    }
}