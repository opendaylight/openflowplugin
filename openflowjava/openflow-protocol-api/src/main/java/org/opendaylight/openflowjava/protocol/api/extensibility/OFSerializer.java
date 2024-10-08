/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.api.extensibility;

import io.netty.buffer.ByteBuf;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.DataContainer;

/**
 * Uniform interface for serializers.
 *
 * @author michal.polkorab
 * @author timotej.kubas
 * @param <T> message type
 */
public interface OFSerializer<T extends DataContainer> extends OFGeneralSerializer {
    /**
     * Transforms POJO/DTO into byte message (ByteBuf).
     *
     * @param input object to be serialized
     * @param outBuffer output buffer
     */
    void serialize(@NonNull T input, @NonNull ByteBuf outBuffer);
}
