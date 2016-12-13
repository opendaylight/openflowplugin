/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.protocol.deserialization;

import io.netty.buffer.ByteBuf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;

public interface MatchEntryDeserializer {

    /**
     * Transforms byte match entry message into POJO/DTO (of type E).
     *
     * @param message message as bytes in ByteBuf
     * @param builder match builder
     */
    void deserializeEntry(ByteBuf message, MatchBuilder builder);

}
