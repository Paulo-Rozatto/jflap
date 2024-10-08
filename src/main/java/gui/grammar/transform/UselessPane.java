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


package gui.grammar.transform;

import grammar.Grammar;
import grammar.Production;
import grammar.ProductionComparator;
import gui.SplitPaneFactory;
import gui.action.GrammarTransformAction;
import gui.editor.ArrowNontransitionTool;
import gui.editor.EditorPane;
import gui.editor.Tool;
import gui.editor.ToolBox;
import gui.editor.TransitionTool;
import gui.environment.FrameFactory;
import gui.environment.GrammarEnvironment;
import gui.grammar.GrammarTable;
import gui.grammar.GrammarTableModel;
import gui.viewer.AutomatonDrawer;
import gui.viewer.AutomatonPane;
import gui.viewer.SelectionDrawer;
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * This is the pane where the removal of useless productions takes place.
 *
 * @author Thomas Finley
 */

public class UselessPane extends JPanel {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     * The editing row in the table.
     */
    private final int editingRow = -1;
    /**
     * Which columsn of the editing row have been edited yet?
     */
    private final boolean[] editingColumn = new boolean[2];
    /**
     * The grammar environment.
     */
    GrammarEnvironment environment;
    /**
     * The grammar to remove useless productions on.
     */
    Grammar grammar;
    /**
     * The controller object.
     */
    UselessController controller;
    /**
     * The grammar table.
     */
    GrammarTable grammarTable;
    /**
     * The main instruction label.
     */
    JLabel mainLabel = new JLabel(" ");
    /**
     * The detail instruction label.
     */
    JLabel detailLabel = new JLabel(" ");

    // These are some of the data structures relevant.
    /**
     * The terminal introduction label.
     */
    JLabel terminalLabel = new JLabel(" ");
    /**
     * The editor pane.
     */
    EditorPane vdgEditor;
    /**
     * The vdg drawer.
     */
    SelectionDrawer vdgDrawer;

    // These are some of the graphical elements.
    /**
     * Simple kludge to allow us to add stuff to the table without fear.
     */
    boolean editingActive = false;
    /**
     * The editing grammar table mode.
     */
    GrammarTableModel editingGrammarModel = new GrammarTableModel() {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    /**
     * The editing grammar table view.
     */
    GrammarTable editingGrammarView = new GrammarTable(editingGrammarModel);
    AbstractAction proceedAction = new AbstractAction("Proceed") {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            Grammar g = getGrammar();
            if (g == null) {
                JOptionPane.showMessageDialog(environment,
                        "The grammar is empty.  Cannot proceed.",
                        "Bad Grammar", JOptionPane.ERROR_MESSAGE);
                return;
            }
            GrammarTransformAction
                    .hypothesizeChomsky(environment, getGrammar());
        }
    };

    /** The pane where */
    AbstractAction exportAction = new AbstractAction("Export") {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            Grammar g = getGrammar();
            if (g == null) {
                JOptionPane.showMessageDialog(environment,
                        "The grammar is empty.  Cannot proceed.",
                        "Bad Grammar", JOptionPane.ERROR_MESSAGE);
                return;
            }
            FrameFactory.createFrame(getGrammar());
        }
    };

    /**
     * Instantiates a new useless production removing pane.
     *
     * @param environment the grammar environment this pane will belong to
     * @param grammar     the grammar to remove useless productions from
     */
    public UselessPane(GrammarEnvironment environment, Grammar grammar) {
        this.environment = environment;
        this.grammar = grammar;
        controller = new UselessController(this, grammar);
        initView();
    }

    /**
     * Initializes the GUI components of this pane.
     */
    private void initView() {
        super.setLayout(new BorderLayout());
        initGrammarTable();
        JPanel rightPanel = initRightPanel();
        JSplitPane mainSplit = SplitPaneFactory.createSplit(environment, true,
                0.4, new JScrollPane(grammarTable), rightPanel);
        this.add(mainSplit, BorderLayout.CENTER);
    }    // These are general controls.
    AbstractAction doStepAction = new AbstractAction("Do Step") {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            controller.doStep();
        }
    };

    /**
     * Initializes the right panel.
     */
    private JPanel initRightPanel() {
        JPanel right = new JPanel(new BorderLayout());

        // Sets the alignments.
        mainLabel.setAlignmentX(0.0f);
        detailLabel.setAlignmentX(0.0f);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(mainLabel);
        panel.add(detailLabel);
        panel.add(terminalLabel);
        initEditingGrammarTable();

        // Sets up the editor pane.
        vdgDrawer = new SelectionDrawer(controller.vdg);
        vdgEditor = new EditorPane(vdgDrawer, new ToolBox() {
            public List<Tool> tools(AutomatonPane view,
                                    AutomatonDrawer drawer) {
                List<Tool> t = new LinkedList<>();
                t.add(new ArrowNontransitionTool(view, drawer));
                t.add(new TransitionTool(view, drawer));
                return t;
            }
        }, true);
        // Grammar editor?
        JPanel grammarEditor = new JPanel(new BorderLayout());
        JToolBar editingBar = new JToolBar();
        editingBar.setAlignmentX(0.0f);
        editingBar.setFloatable(false);
        editingBar.add(deleteAction);
        grammarEditor.add(editingBar, BorderLayout.NORTH);
        grammarEditor.add(new JScrollPane(editingGrammarView),
                BorderLayout.CENTER);
        JSplitPane rightSplit = SplitPaneFactory.createSplit(environment,
                false, 0.5, vdgEditor, grammarEditor);
        panel.add(rightSplit);

        JToolBar toolbar = new JToolBar();
        toolbar.setAlignmentX(0.0f);
        toolbar.add(doStepAction);
        toolbar.add(doAllAction);
        toolbar.addSeparator();
        toolbar.add(proceedAction);
        toolbar.add(exportAction);
        right.add(toolbar, BorderLayout.NORTH);

        right.add(panel, BorderLayout.CENTER);

        return right;
    }    AbstractAction doAllAction = new AbstractAction("Do All") {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            controller.doAll();
        }
    };

    /**
     * Initializes a table for the grammar.
     *
     * @return a table to display the grammar
     */
    private GrammarTable initGrammarTable() {
        grammarTable = new GrammarTable(new GrammarTableModel(grammar) {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            public boolean isCellEditable(int r, int c) {
                return false;
            }
        });
        grammarTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                GrammarTable gt = (GrammarTable) event.getSource();
                Point at = event.getPoint();
                int row = gt.rowAtPoint(at);
                if (row == -1)
                    return;
                if (row == gt.getGrammarModel().getRowCount() - 1)
                    return;
                Production p = gt.getGrammarModel().getProduction(row);
                controller.productionClicked(p, event);
            }
        });
        return grammarTable;
    }

    /**
     * Updates the delete action enabledness.
     */
    void updateDeleteEnabledness() {
        if (controller.step != UselessController.PRODUCTION_MODIFY) {
            deleteAction.setEnabled(false);
            return;
        }
        int min = editingGrammarView.getSelectionModel().getMinSelectionIndex();
        if (min == -1 || min >= editingGrammarModel.getRowCount() - 1) {
            deleteAction.setEnabled(false);
            return;
        }
        deleteAction.setEnabled(true);
    }

    // These are some of the special structures relevant to the
    // grammar editing table.

    /**
     * Initializes the editing grammar view.
     */
    private void initEditingGrammarTable() {
        editingGrammarView.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent event) {
                        updateDeleteEnabledness();
                    }
                });
        Object o = new Object();
        editingGrammarView.getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), o);
        editingGrammarView.getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), o);
        editingGrammarView.getActionMap().put(o, deleteAction);
    }

    /**
     * This method should be called when the deletion method is called.
     */
    private void deleteActivated() {
        if (controller.step != UselessController.PRODUCTION_MODIFY)
            return;
        int deleted = 0, kept = 0;
        for (int i = editingGrammarModel.getRowCount() - 2; i >= 0; i--) {
            if (!editingGrammarView.isRowSelected(i))
                continue;
            Production p = editingGrammarModel.getProduction(i);
            if (controller.productionDeleted(p, i)) {
                editingGrammarModel.deleteRow(i);
                deleted++;
            } else {
                kept++;
            }
        }
        if (kept != 0) {
            JOptionPane.showMessageDialog(this, kept
                            + " production(s) selected should not be removed.\n"
                            + deleted + " production(s) were removed.",
                    "Bad Selection", JOptionPane.ERROR_MESSAGE);
        }
        if (deleted != 0) {
            controller.updateDisplay();
        }
    }

    /**
     * Returns a nice sorted grammar.
     */
    public Grammar getGrammar() {
        Grammar g = editingGrammarView.getGrammar(grammar.getClass());
        Production[] p = g.getProductions();
        Arrays.sort(p, new ProductionComparator(grammar));
        if (p.length == 0 || !p[0].getLHS().equals(grammar.getStartVariable()))
            return null;
        Grammar g2 = null;
        try {
            g2 = g.getClass().newInstance();
            g2.addProductions(p);
            g2.setStartVariable(grammar.getStartVariable());
        } catch (Throwable e) {
            System.err.println("BADNESS!");
            System.err.println(e);
            return g2;
        }
        return g2;
    }





    /**
     * The delete action for deleting rows.
     */
    AbstractAction deleteAction = new AbstractAction("Delete") {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            deleteActivated();
        }
    };
}
