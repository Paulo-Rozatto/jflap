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
import grammar.Production;
import grammar.parse.CYKParser;
import grammar.parse.CYKTracer;
import grammar.parse.ParseNode;
import gui.SplitPaneFactory;
import gui.TableTextSizeSlider;
import gui.environment.GrammarEnvironment;
import gui.grammar.GrammarTable;
import gui.sim.multiple.InputTableModel;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JToolBar;

/**
 * CYK Parse Pane
 *
 * @author Kyung Min (Jason) Lee
 */
public class CYKParsePane extends BruteParsePane {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * The parser that is going to be used
     **/
    private final CYKParser myParser;
    /**
     * CNF Grammar that is transformed from the original grammar
     */
    private final Grammar myCNFGrammar;
    /**
     * The action for the stepping control.
     */
    private Action myStepAction;
    /**
     * Target string that user is trying to derive
     **/
    private String myTarget;
    /**
     * Boolean variable telling whether grammar is accepted or not (If accepted, we can get trace)
     */
    private boolean myTraceAvailable;


    private ParseNode myCurrentAnswerNode;

    private Production[] myAnswers;

    private LinkedList<ParseNode> myQueue;

    private int myIndex;

    /**
     * Constructor for CYK Control Parse Pane
     * calls the super class's constructor
     *
     * @param environment
     * @param grammar
     */
    public CYKParsePane(GrammarEnvironment environment, Grammar original, Grammar cnf) {
        super(environment, original, null);
        myCNFGrammar = cnf;
        myParser = new CYKParser(myCNFGrammar);
    }

    /**
     * Constructor for CYK Control Parse Pane
     * calls the super class's constructor
     *
     * @param environment
     * @param grammar
     */
    public CYKParsePane(GrammarEnvironment environment, Grammar original, Grammar cnf, InputTableModel model) {
        super(environment, original, model);
        myCNFGrammar = cnf;
        myParser = new CYKParser(myCNFGrammar);
        myModel = model;
    }

    /**
     * Initialize the view
     */
    protected void initView() {
        initTreePanel();

        // Sets up the displays.
        JComponent pt = initParseTable();
        JScrollPane parseTable = pt == null ? null : new JScrollPane(pt);
        GrammarTable g = initGrammarTable(grammar);
        JScrollPane grammarTable = new JScrollPane(g);

        treeDerivationPane.add(initTreePanel(), "0");
        JTable table = initDerivationTable();
        JPanel derivationPanel = new JPanel();
        derivationPanel.setLayout(new BorderLayout());
        derivationPanel.add(table, BorderLayout.CENTER);
        derivationPanel.add(new TableTextSizeSlider(table, JSlider.HORIZONTAL), BorderLayout.NORTH);
        derivationPane = new JScrollPane(derivationPanel);
        treeDerivationPane.add(derivationPane, "1");
        bottomSplit = SplitPaneFactory.createSplit(environment, true, 0.3,
                grammarTable, treeDerivationPane);
        topSplit = SplitPaneFactory.createSplit(environment, true, 0.4,
                parseTable, initInputPanel());
        mainSplit = SplitPaneFactory.createSplit(environment, false, 0.3,
                topSplit, bottomSplit);
        add(mainSplit, BorderLayout.CENTER);
        add(statusDisplay, BorderLayout.SOUTH);
        add(new TableTextSizeSlider(g, JSlider.HORIZONTAL), BorderLayout.NORTH);
    }


    /**
     * Inits a new tree panel.
     *
     * @return a new display for the parse tree
     */
    protected JComponent initTreePanel() {
        treePanel = new SelectableUnrestrictedTreePanel(this);
        return treePanel;
    }

    /**
     * This method is called when there is new input to parse.
     *
     * @param string a new input string
     */
    public void input(String string) {
        this.statusDisplay.setText("");
        myTarget = string;
        treePanel.setAnswer(null);
        treePanel.repaint();
        derivationModel.setRowCount(0);
        myStepAction.setEnabled(false);
        if (myParser.solve(string)) {
            progress.setText("String is Accepted!");
            ////System.out.println(myParser.getTrace());
            myTraceAvailable = true;
            myStepAction.setEnabled(true);
            traceBack();
        } else
            progress.setText("String is Rejected!");
    }

    /**
     * Method for Multiple Parsing
     */
    public void parseMultiple() {
        String[][] inputs = myModel.getInputs();
        row = -1;
        while (row < (inputs.length - 1)) {
            //System.out.println("ROW = "+row);
            row++;
            //System.out.println("String is = "+inputs[row][0]);
            if (myParser.solve(inputs[row][0]))
                myModel.setResult(row, "Accept", null, environment.myTransducerStrings, row);
            else
                myModel.setResult(row, "Reject", null, environment.myTransducerStrings, row);
        }
    }

    /**
     * Method for getting the original Productions back
     * NOTE: STILL UNDER CONSTRUCTION BETA VERSION ONLY!
     */
    public void traceBack() {
        if (!myTraceAvailable)
            return;
        CYKTracer cykTracer = new CYKTracer(grammar, myParser.getTrace());
        cykTracer.traceBack();
        myAnswers = cykTracer.getAnswer();
		
	/*	System.out.println("Answer is ");
		for (int i=0; i<myAnswers.length; i++)
			System.out.println(myAnswers[i].getLHS()+" -> "+myAnswers[i].getRHS());
	*/
        if (!myAnswers[0].getLHS().equals(grammar.getStartVariable())) {

            for (int i = 1; i < myAnswers.length; i++) {
                if (myAnswers[i].getLHS().equals(grammar.getStartVariable())) {
                    Production p = myAnswers[0];
                    myAnswers[0] = myAnswers[i];
                    myAnswers[i] = p;
                    break;
                }
            }
        }
		
		
	
	/*	System.out.println("After is ");
		for (int i=0; i<myAnswers.length; i++)
			System.out.println(myAnswers[i].getLHS()+" -> "+myAnswers[i].getRHS());
	*/
        myCurrentAnswerNode = new ParseNode(grammar.getStartVariable(), new Production[0], new int[0]);
        myQueue = new LinkedList<ParseNode>();
        myQueue.add(myCurrentAnswerNode);
        myIndex = 0;
        stepForward();
    }


    private void stepForward() {
        // TODO Auto-generated method stub
        treePanel.setAnswer(myCurrentAnswerNode);
        treePanel.repaint();
        if (myCurrentAnswerNode.getDerivation().equals(myTarget)) {
            myStepAction.setEnabled(false);
            return;
        }

        ParseNode node = myQueue.removeFirst();

        String deriv = node.getDerivation();
	/*	System.out.println("DERIV => "+deriv);
		System.out.println("PROD => "+myAnswers[myIndex]);
		System.out.println("LHS => "+myAnswers[myIndex].getLHS());
	*/
        int index = deriv.indexOf(myAnswers[myIndex].getLHS());
        if (index == -1) {
            myStepAction.setEnabled(false);
            return;
        }
        deriv = deriv.substring(0, index) + myAnswers[myIndex].getRHS() + deriv.substring(index + 1);
        int[] temp = new int[1];
        temp[0] = index;
        Production[] temp1 = new Production[1];
        temp1[0] = myAnswers[myIndex];
        ParseNode pNode = new ParseNode(deriv, temp1, temp);
        pNode = new ParseNode(pNode);
        node.add(pNode);
        myQueue.add(pNode);
        myCurrentAnswerNode = pNode;
        myIndex++;
    }

    // adding Trace button to the GUI
    protected JToolBar initInputToolbar() {
        JToolBar toolbar = new JToolBar();
        toolbar.add(startAction);
        myStepAction = new AbstractAction("Step") {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e) {
                stepForward();
            }
        };
        myStepAction.setEnabled(false);
        toolbar.add(myStepAction);

        // Set up the view customizer controls.
        toolbar.addSeparator();


        final JComboBox<?> box = new JComboBox<Object>(getViewChoices());
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
}
