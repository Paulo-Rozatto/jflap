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

import grammar.parse.LRParseTable;
import gui.LeftTable;
import java.awt.Color;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JToolTip;
import javax.swing.ToolTipManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * This holds a LR parse table.
 *
 * @author Thomas Finley
 */

public class LRParseTablePane extends LeftTable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     * The built in renderer.
     */
    private final TableHighlighterRendererGenerator THRG = new TableHighlighterRendererGenerator() {
        private DefaultTableCellRenderer renderer = null;

        public TableCellRenderer getRenderer(int row, int column) {
            if (renderer == null) {
                renderer = new CellRenderer();
                renderer.setBackground(new Color(255, 150, 150));
            }
            return renderer;
        }
    };
    /**
     * The parse table for this pane.
     */
    private final LRParseTable table;

    /**
     * Instantiates a new parse table pane for a parse table.
     *
     * @param table the table pane's parse table
     */
    public LRParseTablePane(LRParseTable table) {
        super(table);
        this.table = table;
        setCellSelectionEnabled(true);
        setDefaultRenderer(Object.class, new LRParseTablePane.CellRenderer());
        ToolTipManager.sharedInstance().registerComponent(this);
    }

    /**
     * Highlights a particular cell. Overridden to make sure that the
     * highlighted cells use the same special rendering components this table
     * uses for other entries.
     *
     * @param row    the row index of the cell to highlight
     * @param column the column index of the cell to highlight
     */
    public void highlight(int row, int column) {
        highlight(row, column, THRG);
    }

    /**
     * Retrieves the parse table in this pane.
     *
     * @return the parse table in this pane
     */
    public LRParseTable getParseTable() {
        return table;
    }

    /**
     * Since there may be ambiguity in the LR parse table, each description for
     * each entry appears on a separate line, so the tool tips must have
     * seperate lines.
     *
     * @return the tool tip creation
     */
    public JToolTip createToolTip() {
        return new gui.JMultiLineToolTip();
    }

    /**
     * This extends the concept of the cell renderer.
     */
    class CellRenderer extends DefaultTableCellRenderer {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        public java.awt.Component getTableCellRendererComponent(JTable aTable,
                                                                Object value, boolean isSelected, boolean hasFocus, int row,
                                                                int column) {
            JComponent c = (JComponent) super.getTableCellRendererComponent(
                    aTable, value, isSelected, hasFocus, row, column);
            c.setToolTipText(table.getContentDescription(row, column));
            return c;
        }

    }
}
