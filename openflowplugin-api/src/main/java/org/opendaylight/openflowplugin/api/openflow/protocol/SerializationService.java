/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.protocol;

import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;

public interface SerializationService {
    /**
     * Injects new serializers and deserializers into provided switch connection provider.
     * @param provider OpenFlowJava switch connection provider
     */
    void injectSerializers(SwitchConnectionProvider provider);

    /**
     * Reverts injected serializers and deserializers into their original state.
     * @param provider OpenFlowJava switch connection provider
     */
    void revertSerializers(SwitchConnectionProvider provider);
}