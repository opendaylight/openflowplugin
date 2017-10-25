/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.api.extensibility;

import io.netty.buffer.ByteBuf;
import org.opendaylight.yangtools.yang.binding.DataContainer;

/**
 * Does only-header serialization (such as oxm_ids, action_ids, instruction_ids)
 * @author michal.polkorab
 * @param <T> input message type
 */
public interface HeaderSerializer<T extends DataContainer> extends OFGeneralSerializer {

    /**
     * Serializes object headers (e.g. for Multipart message - Table Features)
     * @param input object whose headers should be serialized
     * @param outBuffer output buffer
     */
    void serializeHeader(T input, ByteBuf outBuffer);
}
