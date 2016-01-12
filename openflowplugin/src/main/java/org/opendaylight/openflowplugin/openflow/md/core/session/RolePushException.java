/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.session;

/**
 * covers role pushing issues
 */
public class RolePushException extends Exception {

    private static final long serialVersionUID = -615991366447313972L;

    /**
     * default ctor
     *
     * @param message exception message
     */
    public RolePushException(String message) {
        super(message);
    }

    /**
     * @param message exception message
     * @param cause exception cause
     */
    public RolePushException(String message, Throwable cause) {
        super(message, cause);
    }
}
