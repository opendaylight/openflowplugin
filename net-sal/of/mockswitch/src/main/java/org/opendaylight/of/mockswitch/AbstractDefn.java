/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.mockswitch;

import org.opendaylight.util.StringUtils;

/**
 * A base class for representing test fixture definitions.
 *
 * @author Simon Hunt
 */
public abstract class AbstractDefn {

    protected final AbstractDefReader reader;

    /** Constructs the definition, caching the definition
     * file reader object.
     *
     * @param reader a definition file reader
     */
    public AbstractDefn(AbstractDefReader reader) {
        this.reader = reader;
    }

    @Override
    public String toString() {
        return "{" + getClass().getSimpleName() + ": \"" +
                reader.getPath() + "\"}";
    }

    /** Provides a multi-line string representation of this definition,
     * suitable for debugging.
     *
     * @return a multi-line string representation
     */
    public String toDebugString() {
        StringBuilder sb = new StringBuilder(toString());
        for (String s: reader.debugLines())
            sb.append(EOLI).append(s);
        sb.append(EOLI_DASH);
        return sb.toString();
    }

    /** Returns the path of the definition file.
     *
     * @return the definition file path
     */
    public String getPath() {
        return reader.getPath();
    }

    protected static final String EOL = StringUtils.EOL;
    protected static final String EOLI = EOL + "  ";
    protected static final String EOLI_DASH = EOL + "  ---";
}
