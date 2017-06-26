/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.util;

import java.util.Map;
import org.opendaylight.openflowjava.protocol.api.keys.TypeToClassKey;

/**
 * @author michal.polkorab
 *
 */
public class TypeToClassInitHelper {

    private short version;
    private Map<TypeToClassKey, Class<?>> messageClassMap;

    /**
     * Constructor
     * @param version protocol wire version
     * @param messageClassMap map which stores type to class mapping
     */
    public TypeToClassInitHelper(short version, Map<TypeToClassKey,
            Class<?>> messageClassMap) {
        this.version = version;
        this.messageClassMap = messageClassMap;
    }

    /**
     * Registers Class int the type to class mapping
     * @param type code value for message type / class
     * @param clazz class corresponding to the code
     */
    public void registerTypeToClass(short type, Class<?> clazz) {
        messageClassMap.put(new TypeToClassKey(version, type), clazz);
    }
}
