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

import grammar.Grammar;
import grammar.parse.*;
import gui.environment.GrammarEnvironment;
import gui.tree.*;
import javax.swing.JComponent;
import javax.swing.JTable;

/**
 * This is a parse pane for LL grammars.
 *
 * @author Thomas Finley
 */

public class LLParsePane extends ParsePane {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     * The parse table.
     */
    final LLParseTable table;
    /**
     * The controller object.
     */
    protected LLParseController controller = new LLParseController(this);
    /**
     * The parse table panel.
     */
    LLParseTablePane tablePanel;
    /**
     * The selection node drawer.
     */
    SelectNodeDrawer nodeDrawer = new SelectNodeDrawer();

    /**
     * Instantiaes a new LL parse pane.
     *
     * @param environment the grammar environment
     * @param grammar     the augmented grammar
     * @param table       the LL parse table
     */
    public LLParsePane(GrammarEnvironment environment, Grammar grammar,
                       LLParseTable table) {
        super(environment, grammar);
        this.table = new LLParseTable(table) {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        initView();
    }

    /**
     * Inits a parse table.
     *
     * @return a table to hold the parse table
     */
    protected JTable initParseTable() {
        tablePanel = new LLParseTablePane(table);
        return tablePanel;
    }

    /**
     * This method is called when there is new input to parse.
     *
     * @param string a new input string
     */
    protected void input(String string) {
        controller.initialize(string);
    }

    /**
     * This method is called when the step button is pressed.
     */
    protected boolean step() {
        controller.step();
        return true;
    }

    /**
     * Inits a new tree panel. This overriding adds a selection node drawer so
     * certain nodes can be highlighted.
     *
     * @return a new display for the parse tree
     */
    protected JComponent initTreePanel() {
        treeDrawer.setNodeDrawer(nodeDrawer);
        return super.initTreePanel();
    }
}
