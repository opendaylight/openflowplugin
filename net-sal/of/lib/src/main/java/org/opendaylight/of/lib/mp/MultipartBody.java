/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.lib.mp;

import org.opendaylight.of.lib.IncompleteStructureException;
import org.opendaylight.of.lib.Structure;
import org.opendaylight.of.lib.msg.OfmMultipartReply;
import org.opendaylight.of.lib.msg.OfmMultipartRequest;

/**
 * Tag interface for classes that form the body of either
 * {@link OfmMultipartRequest} or {@link OfmMultipartReply}.
 *
 * @author Simon Hunt
 */
public interface MultipartBody extends Structure {

    /** Validates this body for completeness and throws an exception
     * if it is considered "not complete".
     *
     * @throws IncompleteStructureException if the body is not complete
     */
    void validate() throws IncompleteStructureException;

    /** Returns the total length of the body, in bytes.
     *
     * @return the total length of the body
     */
    int getTotalLength();
}
