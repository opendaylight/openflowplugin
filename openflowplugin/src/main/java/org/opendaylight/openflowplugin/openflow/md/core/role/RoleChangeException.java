/**
 * Copyright (c) 2015, 2016 Dell.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.role;


public class RoleChangeException extends Exception {
    private static final long serialVersionUID = -615991366447313972L;

    /**
     * default ctor
     *
     * @param message
     */
    public RoleChangeException(String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public RoleChangeException(String message, Throwable cause) {
        super(message, cause);
    }
}
