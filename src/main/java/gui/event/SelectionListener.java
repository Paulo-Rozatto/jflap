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

package gui.event;

import java.util.EventListener;

/**
 * A <CODE>SelectionListener</CODE> is an interface for objects that want to listen to <CODE>
 * SelectionEvent</CODE>s.
 *
 * @author Thomas Finley
 * @see gui.event.SelectionEvent
 */
public interface SelectionListener extends EventListener {
  /**
   * This method is called when a selection in an object is changed.
   *
   * @param event the selection event
   */
  void selectionChanged(SelectionEvent event);
}
