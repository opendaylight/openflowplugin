/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core;

import org.opendaylight.openflowplugin.openflow.md.core.session.SessionContext;

/**
 * @author mirehak
 *
 */
public interface ErrorHandler extends Runnable, AutoCloseable {

    /**
     * @param e cause
     * @param sessionContext of source
     */
    void handleException(Throwable e, SessionContext sessionContext);

}
