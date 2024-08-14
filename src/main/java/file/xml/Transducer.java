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


package file.xml;

import file.ParseException;
import java.io.Serializable;
import org.w3c.dom.Document;

/**
 * This is an interface for objects that serve as a go between from DOM to a
 * JFLAP object representing a structure (such as an automaton or grammar), and
 * back again.
 *
 * @author Thomas Finley
 */

public interface Transducer {
    /**
     * The tag name for the root of a structure.
     */
    String STRUCTURE_NAME = "structure";
    /**
     * The tag name for the type of structure this is.
     */
    String STRUCTURE_TYPE_NAME = "type";

    /**
     * Given a document, this will return the corresponding JFLAP structure
     * encoded in the DOM document.
     *
     * @param document the DOM document to decode
     * @return a serializable object, as all JFLAP structures are encoded in
     * serializable objects
     * @throws ParseException in the event of an error that may lead to undesirable
     *                        functionality
     */
    Serializable fromDOM(Document document);

    /**
     * Given a JFLAP structure, this will return the corresponding DOM encoding
     * of the structure.
     *
     * @param structure the JFLAP structure to encode
     * @return a DOM document instance
     */
    Document toDOM(Serializable structure);

    /**
     * Returns the string encoding of the type this transducer decodes and
     * encodes.
     *
     * @return the type this transducer recognizes
     */
    String getType();
}
