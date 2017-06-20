/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.lifecycle;

public enum ContextChainState {
    /**
     * Context chain is working as MASTER.
     */
    WORKING_MASTER,
    /**
     * Context chain is working as SLAVE, initial gathering already done.
     */
    WORKING_SLAVE,
    /**
     * Context chain is undefined.
     */
    UNDEFINED

}
