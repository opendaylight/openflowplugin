/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.util;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;

/**
 * Interface for making code keys.
 *
 * @author michal.polkorab
 */
public interface CodeKeyMaker {

    /**
     * Makes a code key.
     *
     * @param input buffer that will be the needed data gathered from
     * @return key for deserializer lookup
     */
    MessageCodeKey make(ByteBuf input);
}
