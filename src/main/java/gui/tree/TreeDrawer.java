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


package gui.tree;

import java.awt.Graphics2D;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

/**
 * The <CODE>TreeDrawer</CODE> object is used to draw a <CODE>TreeModel</CODE>
 * in a given space.
 *
 * @author Thomas Finley
 */

public interface TreeDrawer {
    /**
     * Draws the tree in the indicated amount of space.
     *
     * @param g    the graphics object to draw upon
     * @param size the bounds for the space the tree has to draw itself in in the
     *             current graphics, assumed to be a rectangle with a corner at
     *             0,0.
     */
    void draw(Graphics2D g, Dimension2D size);

    /**
     * Returns the <CODE>TreeModel</CODE> that this <CODE>TreeDrawer</CODE>
     * draws.
     *
     * @return the tree model this drawer draws
     */
    TreeModel getModel();

    /**
     * This marks the structure as uninitialized, indicating that some state has
     * changed.
     */
    void invalidate();

    /**
     * This initializes whatever structures need to be reinitialized after there
     * is some change in the tree.
     */
    void revalidate();

    /**
     * Returns the node at a particular point.
     *
     * @param point the point to check for the presence of a node
     * @param size  the size that the tree, if drawn, would be drawn in
     */
    TreeNode nodeAtPoint(Point2D point, Dimension2D size);

    /**
     * Returns the node placer for this drawer.
     *
     * @return the node placer for this drawer
     */
    NodePlacer getNodePlacer();

    /**
     * Sets the node placer for this drawer.
     *
     * @param placer the new node placer
     */
    void setNodePlacer(NodePlacer placer);

    /**
     * Returns the node drawer for this drawer
     *
     * @return the node drawer for this drawer
     */
    NodeDrawer getNodeDrawer();

    /**
     * Sets the node drawer for this drawer.
     *
     * @param drawer the new node drawer
     */
    void setNodeDrawer(NodeDrawer drawer);
}
