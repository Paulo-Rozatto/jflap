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


package gui.action;

import automata.Automaton;
import gui.TooltipAction;
import gui.environment.AutomatonEnvironment;
import gui.environment.tag.CriticalTag;
import gui.grammar.automata.ConvertController;
import gui.grammar.automata.ConvertPane;
import gui.viewer.AutomatonPane;
import gui.viewer.SelectionDrawer;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;

/**
 * This class exists as the base class for the conversion of an automaton to a
 * grammar. Subclasses should override <CODE>checkAutomaton</CODE> to detect
 * if the automaton is in the correct form, as well as <CODE>initializeController</CODE>.
 *
 * @author Thomas Finley
 */

public abstract class ConvertAutomatonToGrammarAction extends AutomatonAction {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     * The environment this action is part of.
     */
    private final AutomatonEnvironment environment;
    /**
     * The automaton to convert.
     */
    private final Automaton automaton;
    /**
     * The convert controller for the automaton to grammar process.
     */
    private ConvertController controller;

    /**
     * Instantiates a <CODE>ConvertAutomatonToGrammarAction</CODE>.
     *
     * @param environment the environment which is home to the automaton to convert
     */
    public ConvertAutomatonToGrammarAction(AutomatonEnvironment environment) {
        super("Convert to Grammar", null);
        this.environment = environment;
        this.automaton = environment.getAutomaton();
    }

    /**
     * Modified for 6.3
     *
     * @param title
     * @param environment
     */
    public ConvertAutomatonToGrammarAction(String title, AutomatonEnvironment environment) {
        super(title, null);
        this.environment = environment;
        this.automaton = environment.getAutomaton();
    }

    /**
     * This begins the process of converting an automaton to a grammar.
     *
     * @param e the event to process
     */
    public void actionPerformed(ActionEvent e) {
        if (automaton.getInitialState() == null) {
            JOptionPane.showMessageDialog(environment,
                    "There must be an initial state!", "No Initial State",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!checkAutomaton())
            return;
        Automaton a = (Automaton) getAutomaton().clone();
        final ConvertPane pane = new ConvertPane(environment, a);
        final ConvertController controller = initializeController(pane, pane
                .getDrawer(), a);

        AutomatonPane apane = pane.getAutomatonPane();
        apane.addMouseListener(new MouseAdapter() {
            private final SelectionDrawer drawer = pane.getDrawer();

            public void mouseClicked(MouseEvent event) {
                Object o = drawer.stateAtPoint(event.getPoint());
                if (o == null)
                    o = drawer.transitionAtPoint(event.getPoint());
                if (o == null)
                    return;
                controller.revealObjectProductions(o);
            }
        });
        // Gets a toolbar for the conversion.
        JToolBar toolbar = initToolbar(controller);
        pane.add(toolbar, java.awt.BorderLayout.NORTH);

        environment.add(pane, "Convert to Grammar", new CriticalTag() {
        });
        environment.setActive(pane);
    }

    /**
     * This helper initializes a toolbar to do stuff with the automaton.
     *
     * @param controller the converter controller
     */
    private JToolBar initToolbar(final ConvertController controller) {
        JToolBar bar = new JToolBar();
        bar.add(new TooltipAction("Hint",
                "Shows the productions for one object.") {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e) {
                controller.revealRandomProductions();
            }
        });
        bar.add(new TooltipAction("Show All",
                "Shows all productions remaining.") {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e) {
                controller.revealAllProductions();
            }
        });
        bar.addSeparator();
        bar.add(new TooltipAction("What's Left?",
                "Highlights remaining objects to convert.") {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e) {
                controller.highlightUntransformed();
            }
        });
        bar.add(new TooltipAction("Export",
                "Exports a finished grammar to a new window.") {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e) {
                controller.exportGrammar();
            }
        });
        return bar;
    }

    /**
     * This method should be overridden to check the automaton. Any reason why
     * this checker will fail must be output to the user. If the automaton is
     * okay, there should be no output to the user and the method should simply
     * return <CODE>true</CODE>.
     *
     * @return <CODE>true</CODE> if the automaton is okay, <CODE>false</CODE>
     * if it is not
     */
    protected abstract boolean checkAutomaton();

    /**
     * This method should be overridden to get the <CODE>ConvertController</CODE>
     * that handles the transition of the automaton to the grammar.
     *
     * @param pane      the convert pane that holds the automaton pane and the grammar
     *                  table
     * @param automaton the automaton that's being converted; note that this will not
     *                  be the exact object returned by <CODE>getAutomaton</CODE>
     *                  since a clone is made
     * @return the convert controller to handle the conversion of the automaton
     * to a grammar
     */
    protected abstract ConvertController initializeController(ConvertPane pane,
                                                              SelectionDrawer drawer, Automaton automaton);

    /**
     * Returns the automaton to convert.
     *
     * @return the automaton to convert
     */
    protected Automaton getAutomaton() {
        return automaton;
    }

    /**
     * Returns the environment this action should modify.
     *
     * @return the home environment for the automaton
     */
    protected AutomatonEnvironment getEnvironment() {
        return environment;
    }
}
