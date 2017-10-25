/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.api.connection;

import java.util.concurrent.Future;

/**
 * for testing purposed
 * @author mirehak
 */
public interface ListeningStatusProvider {

    /**
     * @return future holding startup result of all library instances under plugin's control
     */
    Future<Boolean> isOnline();

}
