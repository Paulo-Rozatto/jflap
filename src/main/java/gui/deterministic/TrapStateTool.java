/*
 *  JFLAP - Formal Languages and Automata Package
 *
 *
 *  Susan H. Rodger
 *  Computer Science Department
 *  Duke University
 *  August 27, 2009

 *  Copyright (c) 2002-2009
 *  All rights reserved.

 *  JFLAP is open source software. Please see the LICENSE for terms.
 *
 */


package gui.deterministic;

import automata.State;
import gui.editor.StateTool;
import gui.viewer.AutomatonDrawer;
import gui.viewer.AutomatonPane;
import java.awt.event.MouseEvent;

/**
 * Aids in creating new trap state
 *
 * @author Kyung Min (Jason) Lee
 */
public class TrapStateTool extends StateTool {


    /**
     * The controller object.
     */
    private final AddTrapStateController myController;
    /**
     * The state that was created.
     */
    private State myTrapState = null;

    /**
     * Instantiates a new trap state tool.
     *
     * @param view       the view that the automaton is drawn in
     * @param drawer     the automaton drawer for the view
     * @param controller the controller object we report to
     */
    public TrapStateTool(AutomatonPane view, AutomatonDrawer drawer,
                         AddTrapStateController controller) {
        super(view, drawer);
        myController = controller;
    }

    /**
     * When the user clicks, one creates a state.
     *
     * @param event the mouse event
     */
    public void mousePressed(MouseEvent event) {
        if ((myTrapState = myController.stateCreate(event.getPoint())) == null)
            return;
        getView().repaint();
    }

    /**
     * When the user drags, one moves the created state.
     *
     * @param event the mouse event
     */
    public void mouseDragged(MouseEvent event) {
        if (myTrapState == null)
            return;
        myTrapState.setPoint(event.getPoint());
        getView().repaint();
    }
}
