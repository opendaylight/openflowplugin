/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.md.core.connection;

import org.opendaylight.openflowjava.protocol.api.connection.SwitchConnectionHandler;

/**
 * Connection manager manages connections with devices.
 * It instantiates and registers {@link org.opendaylight.openflowplugin.api.openflow.md.core.connection.ConnectionContext}
 * used for handling all communication with device when onSwitchConnected notification is processed.
 *
 * Created by Martin Bobak <mbobak@cisco.com> on 25.2.2015.
 */
public interface ConnectionManager extends SwitchConnectionHandler {


}
