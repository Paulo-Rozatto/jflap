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
import gui.SplitPaneFactory;
import gui.TableTextSizeSlider;
import gui.TripleTextFieldSizeSlider;
import gui.environment.GrammarEnvironment;
import gui.grammar.*;
import gui.tree.*;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

/**
 * The parse pane is an abstract class that defines the interface common between
 * parsing panes.
 *
 * @author Thomas Finley
 */

abstract class ParsePane extends JPanel {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     * The input text field.
     */
    public JTextField inputField = new JTextField();
    /**
     * The grammar being displayed.
     */
    public Grammar grammar;
    /**
     * The action for the stepping control.
     */
    public AbstractAction stepAction = new AbstractAction("Step") {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            step();
        }
    };
    /**
     * The label that displays the remaining input.
     */
    JTextField inputDisplay = new JTextField();
    /**
     * The label that displays the stack.
     */
    JTextField stackDisplay = new JTextField();
    /**
     * The label that displays the current status of the parse.
     */
    JLabel statusDisplay = new JLabel("Input a string to begin.");
    /**
     * The display for the grammar.
     */
    GrammarTable grammarTable;
    /**
     * The environment.
     */
    GrammarEnvironment environment;
    /**
     * The action for the start control.
     */
    AbstractAction startAction = new AbstractAction("Start") {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            input(inputField.getText());
        }
    };
    /**
     * A default tree drawer.
     */
    DefaultTreeDrawer treeDrawer = new DefaultTreeDrawer(new DefaultTreeModel(
            new DefaultMutableTreeNode())) {
        private final Color INNER = new Color(100, 200, 120), LEAF = new Color(
                255, 255, 100);

        protected Color getNodeColor(TreeNode node) {
            return node.isLeaf() ? LEAF : INNER;
        }
    };
    /**
     * A default tree display.
     */
    JComponent treePanel = new TreePanel(treeDrawer);
    /**
     * The table model for the derivations.
     */
    DefaultTableModel derivationModel = new DefaultTableModel(new String[]{
            "Production", "Derivation"}, 0) {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    /**
     * The split views.
     */
    JSplitPane mainSplit, topSplit, bottomSplit;
    /**
     * The card layout.
     */
    CardLayout treeDerivationLayout = new CardLayout();
    /**
     * The derivation/parse tree view.
     */
    public JPanel treeDerivationPane = new JPanel(treeDerivationLayout);
    /**
     * The derivation view.
     */
    JScrollPane derivationPane;
    /**
     * The parse table
     **/
    JScrollPane parseTable;

    /**
     * Instantiates a new parse pane. This will not place components. A call to
     * {@link #initView} by a subclass is necessary.
     *
     * @param grammar the grammar that is being parsed
     */
    public ParsePane(GrammarEnvironment environment, Grammar grammar) {
        super(new BorderLayout());
        this.grammar = grammar;
        this.environment = environment;
    }

    /**
     * Initializes the GUI.
     */
    protected void initView() {
        treePanel = initTreePanel();

        // Sets up the displays.
        JComponent pt = initParseTable();
		/*JPanel pTable = pt == null ? null : new JPanel();
		if (pTable != null) {
			pTable.setLayout(new BorderLayout());
			pTable.add(pt, BorderLayout.CENTER);
			pTable.add(new TableTextSizeSlider((JTable)pt, JSlider.HORIZONTAL), BorderLayout.NORTH);
			parseTable = new JScrollPane(pTable);
		} else {
			parseTable = null;
		}
		//JScrollPane parseTable = pt == null ? null : new JScrollPane(pt);
		GrammarTable g = initGrammarTable(grammar);
		JScrollPane grammarTable = new JScrollPane(g);

		treeDerivationPane.add(initTreePanel(), "0");*/
        JTable table = initDerivationTable();
        JPanel derivationPanel = new JPanel();
        derivationPanel.setLayout(new BorderLayout());
        derivationPanel.add(table, BorderLayout.CENTER);
        derivationPanel.add(new TableTextSizeSlider(table, JSlider.HORIZONTAL), BorderLayout.NORTH);
        derivationPane = new JScrollPane(derivationPanel);
        JScrollPane parseTable = pt == null ? null : new JScrollPane(pt);
        GrammarTable g = initGrammarTable(grammar);
        JScrollPane grammarTable = new JScrollPane(g);
        treeDerivationPane.add(initTreePanel(), "0");
        //derivationPane = new JScrollPane(initDerivationTable());
        treeDerivationPane.add(derivationPane, "1");
        bottomSplit = SplitPaneFactory.createSplit(environment, true, 0.3,
                grammarTable, treeDerivationPane);
        topSplit = SplitPaneFactory.createSplit(environment, true, 0.4,
                parseTable, initInputPanel());
        JPanel topHolder = new JPanel();
        topHolder.setLayout(new BorderLayout());
        topHolder.add(topSplit, BorderLayout.CENTER);
        if (pt != null) {
            topHolder.add(new TableTextSizeSlider((JTable) pt, JSlider.HORIZONTAL), BorderLayout.NORTH);
        }
        JPanel bottomHolder = new JPanel();
        bottomHolder.setLayout(new BorderLayout());
        bottomHolder.add(bottomSplit, BorderLayout.CENTER);
        bottomHolder.add(new TableTextSizeSlider(g, JSlider.HORIZONTAL), BorderLayout.NORTH);
        mainSplit = SplitPaneFactory.createSplit(environment, false, 0.3,
                topHolder, bottomHolder);
        add(mainSplit, BorderLayout.CENTER);
        add(statusDisplay, BorderLayout.SOUTH);
        //add(new TableTextSizeSlider(g, JSlider.HORIZONTAL), BorderLayout.NORTH);
    }

    /**
     * Initializes a table for the grammar.
     *
     * @param grammar the grammar
     * @return a table to display the grammar
     */
    protected GrammarTable initGrammarTable(Grammar grammar) {
        grammarTable = new GrammarTable(new GrammarTableModel(grammar) {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            public boolean isCellEditable(int r, int c) {
                return false;
            }
        }) {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            public String getToolTipText(java.awt.event.MouseEvent event) {
                try {
                    int row = rowAtPoint(event.getPoint());
                    return getGrammarModel().getProduction(row).toString()
                            + " is production " + row;
                } catch (Throwable e) {
                    return null;
                }
            }
        };
        return grammarTable;
    }

    /**
     * Returns the interface that holds the input area.
     */
    protected JPanel initInputPanel() {
        JPanel bigger = new JPanel(new BorderLayout());
        JPanel panel = new JPanel();
        //JTable table = new JTable();
        //table.setLayout(new BorderLayout());
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        panel.setLayout(gridbag);

        c.fill = GridBagConstraints.BOTH;

        c.weightx = 0.0;
        panel.add(new JLabel("Input"), c);
        //table.add(new JLabel("Input"));
        c.weightx = 1.0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        panel.add(inputField, c);
        //table.add(inputField);
        inputField.addActionListener(startAction);
        // c.weightx = 0.0;
        // JButton startButton = new JButton(startAction);
        // panel.add(startButton, c);

        c.weightx = 0.0;
        c.gridwidth = 1;
        panel.add(new JLabel("Input Remaining"), c);
        //table.add(new JLabel("Input Remaining"));
        c.weightx = 1.0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        inputDisplay.setEditable(false);
        panel.add(inputDisplay, c);
        //table.add(inputDisplay);

        c.weightx = 0.0;
        c.gridwidth = 1;
        panel.add(new JLabel("Stack"), c);
        //table.add(new JLabel("Stack"));
        c.weightx = 1.0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        stackDisplay.setEditable(false);
        panel.add(stackDisplay, c);
        //table.add(stackDisplay);

//		//testing  this out
//		Object[] array1 =  {"Input", inputField};
//		Object[] array2 =  {"Input Remaining", inputDisplay};
//		Object[] array3 =  {"Stack", stackDisplay};
//		Object[][] rows = {array1, array2, array3};
//		Object[] columns = {"Titles", "Fields"};

        //table.add(panel, BorderLayout.CENTER);
        //JTable table = new JTable(rows, columns);
        //JScrollPane sp = new JScrollPane(table);
        bigger.add(panel, BorderLayout.CENTER);
        //bigger.add(new TableTextSizeSlider(table), BorderLayout.SOUTH);
        bigger.add(new TripleTextFieldSizeSlider(inputField, inputDisplay, stackDisplay, JSlider.HORIZONTAL, "Input Field Text Size "
                + "(For optimization, move one of the window size adjustors around this window after resizing the text fields)"), BorderLayout.SOUTH);
        //bigger.add(new PanelTextSizeSlider(panel), BorderLayout.SOUTH);
        bigger.add(initInputToolbar(), BorderLayout.NORTH);

        return bigger;
    }

    /**
     * Returns the choices for the view.
     *
     * @return an array of strings for the choice of view
     */
    protected String[] getViewChoices() {
        return new String[]{"Noninverted Tree", "Inverted Tree",
                "Derivation Table"};
    }

    /**
     * Returns the tool bar for the main user input panel.
     *
     * @return the tool bar for the main user input panel
     */
    protected JToolBar initInputToolbar() {
        JToolBar toolbar = new JToolBar();
        toolbar.add(startAction);
        stepAction.setEnabled(false);
        toolbar.add(stepAction);

        // Set up the view customizer controls.
        toolbar.addSeparator();

        final JComboBox<Object> box = new JComboBox<Object>(getViewChoices());
        box.setSelectedIndex(0);
        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                changeView((String) box.getSelectedItem());
            }
        };
        box.addActionListener(listener);
        toolbar.add(box);
        return toolbar;
    }

    /**
     * Changes the view.
     *
     * @param name the view button name that was pressed
     */
    protected void changeView(String name) {
        if (name.equals("Noninverted Tree")) {
            treeDerivationLayout.first(treeDerivationPane);
            treeDrawer.setInverted(false);
            treePanel.repaint();
        } else if (name.equals("Inverted Tree")) {
            treeDerivationLayout.first(treeDerivationPane);
            treeDrawer.setInverted(true);
            treePanel.repaint();
        } else if (name.equals("Derivation Table")) {
            treeDerivationLayout.last(treeDerivationPane);
        }
    }

    /**
     * Inits a parse table.
     *
     * @return a table to hold the parse table
     */
    protected abstract JTable initParseTable();

    /**
     * Inits a new tree panel.
     *
     * @return a new display for a parse tree
     */
    protected JComponent initTreePanel() {
        treeDrawer.hideAll();
        treeDrawer.setNodePlacer(new LeafNodePlacer());
        return treePanel;
    }

    /**
     * Inits a new derivation table.
     *
     * @return a new display for the derivation of the parse
     */
    protected JTable initDerivationTable() {
        JTable table = new JTable(derivationModel);
        table.setGridColor(Color.lightGray);
        return table;
    }

    /**
     * This method is called when there is new input to parse.
     *
     * @param string a new input string
     */
    protected abstract void input(String string);

    /**
     * This method is called when the step button is pressed.
     */
    protected abstract boolean step();

    /**
     * Prints this component. This will print only the tree section of the
     * component.
     *
     * @param g the graphics object to print to
     */
    public void printComponent(Graphics g) {
        treeDerivationPane.print(g);
    }

    /**
     * Children are not painted here.
     *
     * @param g the graphics object to paint to
     */
    public void printChildren(Graphics g) {

    }
}
