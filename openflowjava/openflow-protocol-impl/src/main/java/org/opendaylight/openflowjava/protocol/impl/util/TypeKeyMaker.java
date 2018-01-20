/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.util;

import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;

/**
 * Interface for a type key maker.
 *
 * @author michal.polkorab
 * @param <T> type the key maker is based on
 */
public interface TypeKeyMaker<T> {

    /**
     * Makes a MessageTypeKey.
     *
     * @param entry the entry for which to create the key
     * @return key that will be used for serializer lookup in the serializer registry
     */
    MessageTypeKey<?> make(T entry);
}
