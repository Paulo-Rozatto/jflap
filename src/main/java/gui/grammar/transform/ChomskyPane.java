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

import grammar.*;
import gui.*;
import gui.environment.*;
import gui.grammar.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;

/**
 * The pane for converting a grammar to Chomsky normal form.
 *
 * @author Thomas Finley
 */

public class ChomskyPane extends JPanel {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     * The environment.
     */
    GrammarEnvironment environment;
    /**
     * The grammar.
     */
    Grammar grammar;
    /**
     * The converter object.
     */
    CNFConverter converter;
    /**
     * The grammar table.
     */
    GrammarTable grammarTable;
    /**
     * The grammar table.
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
     * The grammar table.
     */
    GrammarTable editingGrammarView = new GrammarTable(editingGrammarModel);
    /**
     * The main label.
     */
    JLabel mainLabel = new JLabel(" ");
    /**
     * The direction label.
     */
    JLabel directionLabel = new JLabel(" ");
    /**
     * The export action.
     */
    AbstractAction exportAction = new AbstractAction("Export") {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            export();
        }
    };
    /**
     * The array of rows that need to be done. This will be updated every
     * turn... I guess.
     */
    private int[] need = new int[0];
    /**
     * The highlight remaining action.
     */
    AbstractAction highlightAction = new AbstractAction("What's Left?") {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            highlightRemaining();
        }
    };

    /**
     * Instantiates a Chomsky pane.
     *
     * @param environment the environment that this pane will become a part of
     * @param grammar     the grammar to convert
     */
    public ChomskyPane(GrammarEnvironment environment, Grammar grammar) {
        this.environment = environment;
        this.grammar = grammar;
        converter = new CNFConverter(grammar);
        mainLabel.setText("Welcome to the Chomsky converter.");
        mainLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() > 10)
                    mainLabel
                            .setText("Click on me again, and I'll kick your ass.");
            }
        });
        initView();
        updateDisplay();
    }

    /**
     * Initializes the GUI components of this pane.
     */
    private void initView() {
        super.setLayout(new BorderLayout());
        GrammarTable table = initGrammarTable();
        JPanel rightPanel = initRightPanel();
        JSplitPane mainSplit = SplitPaneFactory.createSplit(environment, true,
                0.4, new JScrollPane(grammarTable), rightPanel);
        add(mainSplit, BorderLayout.CENTER);
        add(new TableTextSizeSlider(table, JSlider.HORIZONTAL), BorderLayout.NORTH);
    }

    /**
     * Initializes the right panel.
     *
     * @return an initialized right panel
     */
    private JPanel initRightPanel() {
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        initEditingGrammarTable();
        mainLabel.setAlignmentX(0.0f);
        directionLabel.setAlignmentX(0.0f);
        right.add(mainLabel);
        right.add(directionLabel);
        right.add(new JScrollPane(editingGrammarView));
        right.add(new TableTextSizeSlider(editingGrammarView, JSlider.HORIZONTAL), BoxLayout.Y_AXIS);

        JPanel biggie = new JPanel(new BorderLayout());
        biggie.add(right, BorderLayout.CENTER);
        JToolBar bar = new JToolBar();
        bar.add(convertAction);
        bar.add(doAllAction);
        bar.add(highlightAction);
        bar.addSeparator();
        bar.add(exportAction);
        biggie.add(bar, BorderLayout.NORTH);
        return biggie;
    }

    /**
     * Updates the display.
     */
    private void updateDisplay() {
        need = getWhatNeedsDone();
        boolean done = need.length == 0;
        convertAction.setEnabled(!done);
        doAllAction.setEnabled(!done);
        highlightAction.setEnabled(!done);
        exportAction.setEnabled(done);
        if (done)
            directionLabel
                    .setText("Conversion done.  Press \"Export\" to use.");
        else
            directionLabel.setText(need.length
                    + " production(s) must be converted.");
    }

    /**
     * Returns the array of rows that need further reduction.
     *
     * @return an array of row indices that need reduction
     */
    private int[] getWhatNeedsDone() {
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < editingGrammarModel.getRowCount() - 1; i++)
            if (!converter.isChomsky(editingGrammarModel.getProduction(i)))
                list.add(new Integer(i));
        int[] ret = new int[list.size()];
        for (int i = 0; i < ret.length; i++)
            ret[i] = list.get(i).intValue();
        return ret;
    }

    /**
     * Does everything.
     */
    public void doAll() {
        ListSelectionModel model = editingGrammarView.getSelectionModel();
        while (need.length != 0) {
            model.clearSelection();
            for (int i = 0; i < need.length; i++)
                model.addSelectionInterval(need[i], need[i]);
            convertSelected();
        }
        mainLabel.setText("All productions completed.");
        editingGrammarView.dehighlight();
    }

    /**
     * Highlights the remaining rows.
     */
    private void highlightRemaining() {
        editingGrammarView.dehighlight();
        mainLabel.setText("Productions to convert are selected.");
        for (int i = 0; i < need.length; i++)
            editingGrammarView.highlight(need[i]);
    }

    /**
     * Takes the grammar, and attempts to export it.
     */
    private void export() {
        Production[] p = editingGrammarModel.getProductions();
	/*	System.out.println("PRINTTITTING");
		for (int i=0; i<p.length; i++)
		{
			System.out.println(p[i]);
		}*/
        try {
            p = CNFConverter.convert(p);
        } catch (UnsupportedOperationException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Export Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            Grammar g = grammar.getClass().newInstance();
            g.addProductions(p);
            g.setStartVariable(grammar.getStartVariable());
            FrameFactory.createFrame(g);
        } catch (Throwable e) {
            System.err.println(e);
        }
    }

    public Grammar getGrammar() {
        Production[] p = editingGrammarModel.getProductions();
        try {
            p = CNFConverter.convert(p);
        } catch (UnsupportedOperationException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "CNF Conversion Error",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
        try {
            Grammar g = grammar.getClass().newInstance();
            g.addProductions(p);
            g.setStartVariable(grammar.getStartVariable());
            return g;
        } catch (Throwable e) {
            System.err.println(e);
        }
        return null;
    }

    /**
     * Converts the selected rows.
     */
    private void convertSelected() {
        if (!convertAction.isEnabled())
            return;
        int[] r = editingGrammarView.getSelectedRows();
        int unneeded = 0;
        ArrayList<Integer> list = new ArrayList<>();
        editingGrammarView.dehighlight();

        for (int i = r.length - 1; i >= 0; i--) {
            Production p = editingGrammarModel.getProduction(r[i]);
            if (p == null)
                return;
            Production[] ps = null;
            try {
                ps = converter.replacements(p);
            } catch (IllegalArgumentException e) {
                unneeded++;
                continue;
            }
            editingGrammarModel.deleteRow(r[i]);
            for (int j = ps.length - 1; j >= 0; j--) {
                editingGrammarModel.addProduction(ps[j], r[i]);
                Integer integer = new Integer(r[i]);
                list.add(0, integer);
            }
        }
        if (unneeded > 0) {
            JOptionPane.showMessageDialog(this, "Conversion unneeded on "
                            + unneeded + " production(s).\n" + (r.length - unneeded)
                            + " production(s) converted.", "Conversion Unneeded",
                    JOptionPane.ERROR_MESSAGE);
        }
        int last = -1, adjust = 0;
        for (int i = 0; i < list.size(); i++) {
            int toHighlight = list.get(i).intValue() + i;
            if (last != -1 && last != toHighlight - 1)
                adjust++;
            last = toHighlight;
            toHighlight -= adjust;
            editingGrammarView.highlight(toHighlight);
        }
        if (list.size() != 0) {
            editingGrammarView.repaint();
            mainLabel.setText("Replacement production(s) highlighted.");
        }
        updateDisplay();
    }

    /**
     * Initializes the editing grammar view.
     */
    private void initEditingGrammarTable() {
        editingGrammarView.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    convertSelected();
                }
            }
        });
        Production[] ps = grammar.getProductions();
        for (int i = 0; i < ps.length; i++)
            editingGrammarModel.addProduction(ps[i]);
    }    /**
     * The convert action.
     */
    AbstractAction convertAction = new AbstractAction("Convert Selected") {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            convertSelected();
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
        return grammarTable;
    }    /**
     * The do all action.
     */
    AbstractAction doAllAction = new AbstractAction("Do All") {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            doAll();
        }
    };




}
