/*
 * (c) Copyright 2001-2002 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.syntax;

import java.util.Properties;

import org.opendaylight.util.syntax.parsers.Utilities;

/**
 * Abstraction of a syntax node capable of inheritance.
 * 
 * @author Thomas Vachuska
 */
public abstract class SyntaxNodeExtension extends SyntaxNode {

    private static final long serialVersionUID = -7381481474402488512L;

    /** Syntax node from which this node inherits its properties. */
    private SyntaxNodeExtension parent = null;

    /** Minimum number of times, this node must occur in a row. */
    private int minOccurrences = 1;

    /** Maximum number of times, this node must occur in a row. */
    private int maxOccurrences = 1;

    /**
     * Default constructor.
     */
    protected SyntaxNodeExtension() {
    }

    /**
     * Constructs a new extended syntax node from the provided node.
     * 
     * @param node Original generic node from which to create this fully
     *        resolved node.
     * @param parent Fully resolved syntax node from which this node is to
     *        inherit its properties, i.e. node to be extended.
     */
    protected SyntaxNodeExtension(SyntaxNode node, SyntaxNodeExtension parent) {
        super(node);
        this.parent = parent;

        Properties db = getDB();

        // See if there is an optional indicator.
        if (Utilities.get(db, OPTIONAL, false)) {
            minOccurrences = 0;
            maxOccurrences = 1;
            return;
        }
        
        // See if there are indicators about the number of occurences
        // of this parameter.
        minOccurrences = Utilities.get(db, MIN_OCCURRENCES, 1);

        // If the minimum number of occurrences was specified, assume
        // that the maximum is unlimited by default; otherwise assume 1.
        boolean minSpecified = db.getProperty(MIN_OCCURRENCES) != null;
        int defaultMax = minSpecified ? Integer.MAX_VALUE : 1;
        maxOccurrences = Utilities.get(db, MAX_OCCURRENCES, defaultMax);
        if (maxOccurrences < 0)
            maxOccurrences = Integer.MAX_VALUE;

        // Make sure that the maximum is at least equal to the minimum.
        maxOccurrences = Math.max(maxOccurrences, minOccurrences);
    }

    /**
     * Get the parent node, which this node directly extends or null of this
     * node does not extend any node.
     * 
     * @return Node from which this node inherits its properties.
     */
    public SyntaxNodeExtension getParent() {
        return parent;
    }

    /**
     * Returns the minimum number of times, this parameter must occur in a
     * row.
     * 
     * @return number of minimum occurrences for this node
     */
    public int getMinOccurrences() {
        return minOccurrences;
    }

    /**
     * Returns the maximum number of times, this parameter must occur in a
     * row.
     * 
     * @return number of maximum occurrences for this node
     */
    public int getMaxOccurrences() {
        return maxOccurrences;
    }

}
