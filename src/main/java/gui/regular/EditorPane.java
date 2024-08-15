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


package gui.regular;

import gui.TextFieldSizeSlider;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import regular.*;

/**
 * The editor pane for a regular expression allows the user to change the
 * regular expression.
 *
 * @author Thomas Finley
 */

public class EditorPane extends JPanel {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     * The regular expression.
     */
    private final RegularExpression expression;
    /**
     * The field where the expression is displayed and edited.
     */
    private final JTextField field = new JTextField("");
    /**
     * The expression change listener for a regular expression detects if there
     * are changes in the environment, and if so, changes the display.
     */
    private final ExpressionChangeListener listener = new ExpressionChangeListener() {
        public void expressionChanged(ExpressionChangeEvent e) {
            field.setText(e.getExpression().asString());
        }
    };
    /**
     * The reference object.
     */
    private final Reference<String> ref = new WeakReference<String>(null) {
        public String get() {
            return field.getText();
        }
    };

    /**
     * Instantiates a new editor pane for a given regular expression.
     *
     * @param expression the regular expression
     */
    public EditorPane(RegularExpression expression) {
        // super(new BorderLayout());
        this.expression = expression;
        field.setText(expression.asString());
        field.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                updateExpression();
            }
        });
        field.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                updateExpression();
            }

            public void removeUpdate(DocumentEvent e) {
                updateExpression();
            }

            public void changedUpdate(DocumentEvent e) {
                updateExpression();
            }
        });
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.gridwidth = GridBagConstraints.REMAINDER;

        add(new JLabel("Edit the regular expression below:"), c);
        add(field, c);
        add(new TextFieldSizeSlider(field, JSlider.HORIZONTAL, "Input Field Text Size (For optimiztion, adjust the size of "
                + "this window after resizing the text field)"), c);
    }

    /**
     * This is called when the regular expression should be updated to accord
     * with the field.
     */
    private void updateExpression() {
        expression.change(ref);
    }
}
