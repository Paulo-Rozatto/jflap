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
import gui.LeftTable;
import gui.environment.Universe;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * This table is an table specifically for the <CODE>FirstFollowModel</CODE>
 * for handling user entry of first and follow sets.
 *
 * @author Thomas Finley
 */

public class FirstFollowTable extends LeftTable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     * The built in highlight renderer generator.
     */
    private static final gui.HighlightTable.TableHighlighterRendererGenerator THRG = new TableHighlighterRendererGenerator() {
        private DefaultTableCellRenderer renderer = null;

        public TableCellRenderer getRenderer(int row, int column) {
            if (renderer == null) {
                renderer = new SetsCellRenderer();
                renderer.setBackground(new Color(255, 150, 150));
            }
            return renderer;
        }
    };
    /**
     * The sets cell renderer.
     */
    private static final TableCellRenderer RENDERER = new SetsCellRenderer();
    /**
     * The table model.
     */
    private final FirstFollowModel model;

    /**
     * Instantiates a new first follow table for a grammar.
     *
     * @param grammar the grammar to create the table
     */
    public FirstFollowTable(Grammar grammar) {
        super(new FirstFollowModel(grammar));
        model = (FirstFollowModel) getModel();

        getColumnModel().getColumn(1).setCellRenderer(RENDERER);
        getColumnModel().getColumn(2).setCellRenderer(RENDERER);

        setCellSelectionEnabled(true);
    }

    /**
     * Converts a string to a set string.
     */
    private static String getSetString(String s) {
        if (s == null)
            return "{ }";
        StringBuffer sb = new StringBuffer("{ ");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '!')
                c = Universe.curProfile.getEmptyString().charAt(0);
            sb.append(c);
            if (i != s.length() - 1)
                sb.append(',');
            sb.append(' ');
        }
        sb.append('}');
        return sb.toString();
    }

    /**
     * Returns the first follow table model.
     *
     * @return the table model
     */
    public FirstFollowModel getFFModel() {
        return model;
    }

    /**
     * Modified to use the set renderer highlighter.
     */
    public void highlight(int row, int column) {
        highlight(row, column, THRG);
    }

    /**
     * The modified table cell renderer.
     */
    private static class SetsCellRenderer extends DefaultTableCellRenderer {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        public Component getTableCellRendererComponent(JTable table,
                                                       Object value, boolean isSelected, boolean hasFocus, int row,
                                                       int column) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(table,
                    value, isSelected, hasFocus, row, column);
            if (hasFocus && table.isCellEditable(row, column))
                return l;
            l.setText(getSetString((String) value));
            return l;
        }
    }
}
