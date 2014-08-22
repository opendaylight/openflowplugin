/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.lib.err;

import org.opendaylight.of.lib.OfpCodeBasedEnum;

/**
 * Tag interface for error code enumerations.
 *
 * @author Simon Hunt
 */
public interface ErrorCode extends OfpCodeBasedEnum {
    /** Returns the parent error type for this error code.
     *
     * @return the parent error type
     */
    ErrorType parentType();
}
