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


package gui.grammar.parse;

import grammar.*;
import grammar.parse.*;
import gui.environment.GrammarEnvironment;
import java.awt.event.ActionEvent;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 * This controller handles user actions for the parsing of a grammar.
 *
 * @author Thomas Finley
 */

public class LLParseDerivationController {
    /**
     * The identifiers for steps.
     */
    static final int FIRST_SETS = 0, FOLLOW_SETS = 1, PARSE_TABLE = 2,
            FINISHED = 3;
    /**
     * The parse table tableview
     */
    private final LLParseTablePane parseTable;
    /**
     * The target parse table.
     */
    private final LLParseTable targetParseTable;
    /**
     * The grammar environment
     */
    GrammarEnvironment environment;
    /**
     * The first-follow table
     */
    FirstFollowTable firstFollow;
    /**
     * The direction label that displays the step the user is on now.
     */
    JLabel directions;
    /**
     * Which step are we currently on?
     */
    int step = -1;
    /**
     * The target first sets.
     */
    Map<String, Set<String>> targetFirstSets;
    /**
     * The target follow sets.
     */
    Map<String, Set<String>> targetFollowSets;
    /**
     * The grammar.
     */
    Grammar grammar;

    /**
     * Instantiates a new parse derivation controller.
     *
     * @param grammar     the grammar
     * @param environment the grammar environment
     * @param firstFollow the first-follow table
     * @param parseTable  the parse table tableview
     * @param directions  the label that displays what step the user is on
     */
    public LLParseDerivationController(Grammar grammar,
                                       GrammarEnvironment environment, FirstFollowTable firstFollow,
                                       LLParseTablePane parseTable, JLabel directions) {
        this.environment = environment;
        this.firstFollow = firstFollow;
        this.parseTable = parseTable;
        this.directions = directions;
        this.grammar = grammar;

        targetFirstSets = Operations.first(grammar);
        targetFollowSets = Operations.follow(grammar);
        targetParseTable = LLParseTableGenerator.generate(grammar);

        nextStep();
    }

    /**
     * If the current step has not been completed, this method will report back
     * to the user what remains to be done.
     *
     * @return <CODE>true</CODE> if the current step is finished, <CODE>false</CODE>
     * plus some user output if the current step is unfinished
     */
    boolean done() {
        switch (step) {
            case -1:
                return true;
            case FIRST_SETS:
            case FOLLOW_SETS:
                Map<String, Set<String>> sets = step == FIRST_SETS ? targetFirstSets : targetFollowSets;
                int col = step == FIRST_SETS ? 1 : 2;
                FirstFollowModel ffm = firstFollow.getFFModel();
                try {
                    firstFollow.getCellEditor().stopCellEditing();
                } catch (NullPointerException e) {
                }
                int highlighted = 0;
                for (int i = 0; i < ffm.getRowCount(); i++) {
                    String var = (String) ffm.getValueAt(i, 0);
                    if (!ffm.getSet(i, col).equals(sets.get(var))) {
                        firstFollow.highlight(i, col);
                        highlighted++;
                    }
                }
                if (highlighted == 0)
                    return true;
                firstFollow.clearSelection();
                try {
                    firstFollow.getCellEditor().stopCellEditing();
                } catch (NullPointerException e) {
                }
                JOptionPane.showMessageDialog(parseTable,
                        "Highlighted sets are incorrect.", "Bad Sets",
                        JOptionPane.ERROR_MESSAGE);
                firstFollow.dehighlight();
                return false;
            case PARSE_TABLE:
                LLParseTable pt = parseTable.getParseTable();
                try {
                    parseTable.getCellEditor().stopCellEditing();
                } catch (NullPointerException e) {
                }
                String[][] diff = pt.getDifferences(targetParseTable);
                if (diff.length == 0)
                    return true;
                for (int i = 0; i < diff.length; i++) {
                    int row = pt.getRow(diff[i][0]);
                    int column = pt.getColumn(diff[i][1]);
                    parseTable.highlight(row, column);
                }
                parseTable.clearSelection();
                JOptionPane.showMessageDialog(firstFollow,
                        "Highlighted cells are incorrect.", "Bad Parse Table",
                        JOptionPane.ERROR_MESSAGE);
                parseTable.dehighlight();
                return false;
            case FINISHED:
                JOptionPane.showMessageDialog(firstFollow,
                        "The parse table is complete.", "Finished",
                        JOptionPane.ERROR_MESSAGE);
            default:
                return false;
        }
    }

    /**
     * This method will complete the current step. When done with whatever it
     * must do it will call {@link #nextStep} to move to the next step unless it
     * is ont he last step, in which case a small error message is displayed.
     */
    public void completeStep() {
        switch (step) {
            case FIRST_SETS:
            case FOLLOW_SETS:
                Map<String, Set<String>> sets = step == FIRST_SETS ? targetFirstSets : targetFollowSets;
                int col = step == FIRST_SETS ? 1 : 2;
                FirstFollowModel ffm = firstFollow.getFFModel();
                // Get each variable.
                for (int i = 0; i < ffm.getRowCount(); i++) {
                    String var = (String) ffm.getValueAt(i, 0);
                    ffm.setSet(sets.get(var), i, col);
                }
                try {
                    firstFollow.getCellEditor().stopCellEditing();
                } catch (NullPointerException e) {
                }
                firstFollow.repaint();
                nextStep();
                break;
            case PARSE_TABLE:
                LLParseTable pt = parseTable.getParseTable();
                for (int r = 0; r < pt.getRowCount(); r++) {
                    String var = (String) pt.getValueAt(r, 0);
                    for (int c = 1; c < pt.getColumnCount(); c++) {
                        int cv = parseTable.convertColumnIndexToView(c);
                        pt.setValueAt(targetParseTable.getValueAt(r, c), r, c);
                    }
                }
                try {
                    parseTable.getCellEditor().stopCellEditing();
                } catch (NullPointerException e) {
                }
                parseTable.repaint();
                nextStep();
                break;
            case FINISHED:
                JOptionPane.showMessageDialog(firstFollow,
                        "The parse table is complete.", "Finished",
                        JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * This method will complete the step for whatever cells are highlighted, as
     * is appropriate for the current step.
     */
    public void completeSelected() {
        switch (step) {
            case FIRST_SETS:
            case FOLLOW_SETS:
                Map<String, Set<String>> sets = step == FIRST_SETS ? targetFirstSets : targetFollowSets;
                int col = step == FIRST_SETS ? 1 : 2;
                FirstFollowModel ffm = firstFollow.getFFModel();
                int viewCol = firstFollow.convertColumnIndexToView(col);
                for (int i = 0; i < ffm.getColumnCount(); i++) {
                    if (!firstFollow.isCellSelected(i, viewCol))
                        continue;
                    // ////System.out.println("Doing row "+i);
                    String var = (String) ffm.getValueAt(i, 0);
                    ffm.setSet(sets.get(var), i, col);
                }
                try {
                    firstFollow.getCellEditor().stopCellEditing();
                } catch (NullPointerException e) {
                }
                firstFollow.repaint();
                break;
            case PARSE_TABLE:
                LLParseTable pt = parseTable.getParseTable();
                for (int r = 0; r < pt.getRowCount(); r++) {
                    String var = (String) pt.getValueAt(r, 0);
                    for (int c = 1; c < pt.getColumnCount(); c++) {
                        int cv = parseTable.convertColumnIndexToView(c);
                        if (!parseTable.isCellSelected(r, cv))
                            continue;
                        pt.setValueAt(targetParseTable.getValueAt(r, c), r, c);
                    }
                }
                // done!
                try {
                    parseTable.getCellEditor().stopCellEditing();
                } catch (NullPointerException e) {
                }
                parseTable.repaint();
                break;
            case FINISHED:
                JOptionPane.showMessageDialog(firstFollow,
                        "The parse table is complete.", "Finished",
                        JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * This finishes absolutely everything.
     */
    public void completeAll() {
        do {
            completeStep();
        } while (step != FINISHED);
    }

    /**
     * This will handle parsing.
     */
    public void parse() {
        LLParsePane panel = new LLParsePane(environment, grammar,
                targetParseTable);
        environment.add(panel, "LL(1) Parsing");
        environment.setActive(panel);
    }

    /**
     * Checks the grammar for LL1ness.
     *
     * @return if the grammar is LL1
     */
    public boolean isLL1() {
        return Operations.isLL1(grammar);
    }

    /**
     * Moves the controller to the next step of the building of the parse table.
     *
     * @return if the controller could be advanced to the next step
     */
    public boolean nextStep() {
        if (!done())
            return false;

        step++;

        switch (step) {
            case FIRST_SETS:
                parseAction.setEnabled(false);
                firstFollow.getFFModel().setCanEditFirst(true);
                firstFollow.getFFModel().setCanEditFollow(false);
                directions
                        .setText("Define FIRST sets.  ! is the lambda character.");
                break;
            case FOLLOW_SETS:
                firstFollow.getFFModel().setCanEditFirst(false);
                firstFollow.getFFModel().setCanEditFollow(true);
                directions
                        .setText("Define FOLLOW sets.  $ is the end of string character.");
                break;
            case PARSE_TABLE:
                firstFollow.getFFModel().setCanEditFollow(false);
                directions
                        .setText("Fill entries in parse table.  Use ! for a lambda entry.");
                break;
            case FINISHED:
                try {
                    parseTable.getCellEditor().stopCellEditing();
                } catch (NullPointerException e) {
                }

                doSelectedAction.setEnabled(false);
                doStepAction.setEnabled(false);
                doAllAction.setEnabled(false);
                nextAction.setEnabled(false);
                if (isLL1()) {
                    parseAction.setEnabled(true);
                    directions
                            .setText("Parse table complete.  Press \"parse\" to use it.");
                } else {
                    directions.setText("Parse table complete, but has ambiguity.");
                }
                break;
        }
        return true;
    }

    /**
     * These are actions the derivation pane uses. They are activated and
     * deactivated as is appropriate.
     */
    AbstractAction doSelectedAction = new AbstractAction("Do Selected") {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            completeSelected();
        }
    }, doStepAction = new AbstractAction("Do Step") {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            completeStep();
        }
    }, doAllAction = new AbstractAction("Do All") {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            completeAll();
        }
    }, nextAction = new AbstractAction("Next") {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            nextStep();
        }
    }, parseAction = new AbstractAction("Parse") {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            parse();
        }
    };
}
