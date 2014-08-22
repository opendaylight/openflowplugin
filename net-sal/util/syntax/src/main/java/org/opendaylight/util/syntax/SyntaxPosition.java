/*
 * (c) Copyright 2001-2002 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.syntax;

import java.text.ParsePosition;

/**
 * Class to hold the offset of an argument on which a parse was attempted and
 * the syntax node against which the parse was attempted.
 *
 * @author Thomas Vachuska
 * @author Steve Britt
 */
public class SyntaxPosition extends ParsePosition {

    /** Internal flag to keep track of whether the position has been updated
        since the last time wasUpdated method was called.  */
    private boolean wasUpdated = false;

    /** The syntax node with which this position is associated.  */
    private SyntaxNode node = null;

    /**
     * Constructs a new syntax position object using the offset of -1.
     */
    public SyntaxPosition() {
        super(-1);
    }
    
    /**
     * Gets the syntax node with which this position is associated.
     *
     * @return {@link SyntaxNode} instance associated with this exception.
     */
    public SyntaxNode getSyntaxNode() {
        return node;
    }

    /**
     * This method updates the error offset of this syntax position and it
     * will also update it's own syntax node reference with the specified one,
     * if the specified position's error offset is larger than its own.
     *
     * @param position Original position whose state should be cloned.
     */
    public synchronized void update(SyntaxPosition position) {
        update(position, position.node);
    }

    /**
     * This method updates the error offset of this syntax position and it
     * will also update it's own syntax node reference with the specified one,
     * if the specified parse position's error offset is larger than its
     * own.
     *
     * @param position Original position whose state should be cloned.
     * @param node Node to associate with this position as part of the update.
     */
    public synchronized void update(ParsePosition position, SyntaxNode node) {
        update(position, node, false);
    }

    /**
     * This method updates the error offset of this syntax position and it
     * will also update it's own syntax node reference with the specified one,
     * if the specified parse position's error offset is larger than its own.
     *
     * This method is one of several update methods. This version of update is
     * used when a parameter matches. That is very different than when a
     * keyword matches. Because of this the matching algorithm is NOT examined
     * as it is in other update methods.
     *
     * @param position Original position whose state should be cloned.
     * @param node Node to associate with this position as part of the update.
     * @param overrideIfIndicesEqual True if the update should occur even if
     * the indices are equal to each other; false if the update should occur
     * only if the new index is greater than the old one.
     */
    public synchronized void update(ParsePosition position, SyntaxNode node,
                                    boolean overrideIfIndicesEqual) {        
        int newIndex = position.getIndex();
        boolean updateData = (getIndex() < newIndex);
        if (overrideIfIndicesEqual)
            updateData = updateData || (getIndex() == newIndex);
        if (updateData) {
            setIndex (newIndex); 
            this.node = node; 
            this.wasUpdated = true;
        } // if
    }

    /** 
     * Determines if the position was updated and resets the update flag to
     * false, thus operating as a reset-on-read latch.
     *
     * @return True if the position was updated; false otherwise.
     */
    public synchronized boolean wasUpdated() {
        boolean result = wasUpdated;
        wasUpdated = false;
        return result;
    }
    
    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }
    
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    
}
