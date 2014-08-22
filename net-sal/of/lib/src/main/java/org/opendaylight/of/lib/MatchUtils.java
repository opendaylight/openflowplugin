/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib;

import org.opendaylight.of.lib.match.MatchField;

import java.util.List;

/**
 * Provides useful utilities regarding matches.
 *
 * @author Simon Hunt
 */
public class MatchUtils {

    // TODO: add method to create a match based on a network packet.

    /**
     * Performs order independent comparison of two lists of match fields.
     *
     * @param one first list
     * @param two second list
     * @return true if lists have same contents
     */
    public static boolean sameMatchFields(List<MatchField> one,
                                          List<MatchField> two) {
        // TODO: add unit tests for this
        if (one.size() != two.size())
            return false;
        for (MatchField a : one)
            if (!two.contains(a))
                return false;
        return true;
    }

}
