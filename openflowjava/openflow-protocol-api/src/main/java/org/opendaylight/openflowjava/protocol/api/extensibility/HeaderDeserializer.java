/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.api.extensibility;

import io.netty.buffer.ByteBuf;
import org.opendaylight.yangtools.binding.DataContainer;

/**
 * Deserializes headers.
 *
 * @author michal.polkorab
 * @param <E> output message type
 */
public interface HeaderDeserializer<E extends DataContainer> extends OFGeneralDeserializer {

    /**
     * Deserializes a byte message headers.
     *
     * @param rawMessage message as bytes in ByteBuf
     * @return POJO/DTO
     */
    E deserializeHeader(ByteBuf rawMessage);
}
