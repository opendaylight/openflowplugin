/*
 * Copyright (c) 2016 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.spi.connection;

import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow._switch.connection.config.rev160506.SwitchConnectionConfig;

/**
 * Factory for creating SwitchConnectionProvider instances.
 *
 * @author Thomas Pantelis
 */
public interface SwitchConnectionProviderFactory {
    /**
     * Creates a new SwitchConnectionProvider with the given configuration.
     *
     * @param config the SwitchConnectionConfig
     * @return a SwitchConnectionProvider instance
     */
    SwitchConnectionProvider newInstance(SwitchConnectionConfig config);
}
