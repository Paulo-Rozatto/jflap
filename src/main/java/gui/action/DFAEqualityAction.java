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

import automata.UselessStatesDetector;
import automata.fsa.FiniteStateAutomaton;
import automata.graph.FSAEqualityChecker;
import gui.environment.Environment;
import gui.environment.EnvironmentFrame;
import gui.environment.Universe;
import java.awt.event.ActionEvent;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;

/**
 * This tests to see if two finite state automatons accept the same language.
 *
 * @author Thomas Finley
 */

public class DFAEqualityAction extends FSAAction {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     * The equality checker.
     */
    private static final FSAEqualityChecker checker = new FSAEqualityChecker();
    /**
     * The environment.
     */
    private final Environment environment;

    /**
     * Instantiates a new <CODE>DFAEqualityAction</CODE>.
     *
     * @param automaton   the automaton that input will be simulated on
     * @param environment the environment object that we shall add our simulator pane to
     */
    public DFAEqualityAction(FiniteStateAutomaton automaton,
                             Environment environment) {
        super("Compare Equivalence", null);
        this.environment = environment;
        /*
         * putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke (KeyEvent.VK_R,
         * MAIN_MENU_MASK+InputEvent.SHIFT_MASK));
         */
    }

    /**
     * Runs a comparison with another automaton.
     *
     * @param e the action event
     */
    public void actionPerformed(ActionEvent e) {
        JComboBox<EnvironmentFrame> combo = new JComboBox<>();
        // Figure out what existing environments in the program have
        // the type of structure that we need.
        EnvironmentFrame[] frames = Universe.frames();
        for (int i = 0; i < frames.length; i++) {
            if (!isApplicable(frames[i].getEnvironment().getObject())
                    || frames[i].getEnvironment() == environment)
                continue;
            combo.addItem(frames[i]);
        }
        // Set up our automaton.
        FiniteStateAutomaton automaton = (FiniteStateAutomaton) environment
                .getObject();

        if (combo.getItemCount() == 0) {
            JOptionPane.showMessageDialog(Universe
                    .frameForEnvironment(environment), "No other FAs around!");
            return;
        }
        if (automaton.getInitialState() == null) {
            JOptionPane.showMessageDialog(Universe
                            .frameForEnvironment(environment),
                    "This automaton has no initial state!");
            return;
        }
        // Prompt the user.
        int result = JOptionPane.showOptionDialog(Universe
                        .frameForEnvironment(environment), combo, "Compare against FA",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, null, null);
        if (result != JOptionPane.YES_OPTION && result != JOptionPane.OK_OPTION)
            return;
        FiniteStateAutomaton other = (FiniteStateAutomaton) ((EnvironmentFrame) combo
                .getSelectedItem()).getEnvironment().getObject();
        if (other.getInitialState() == null) {
            JOptionPane.showMessageDialog(Universe
                            .frameForEnvironment(environment),
                    "The other automaton has no initial state!");
            return;
        }
        other = (FiniteStateAutomaton) UselessStatesDetector
                .cleanAutomaton(other);
        automaton = (FiniteStateAutomaton) UselessStatesDetector
                .cleanAutomaton(automaton);
        String checkedMessage = checker.equals(other, automaton) ? "They ARE equivalent!"
                : "They AREN'T equivalent!";
        JOptionPane.showMessageDialog(
                Universe.frameForEnvironment(environment), checkedMessage);
    }
}
