/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.driver.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache for tracking primordial info to device type name bindings.
 *
 * @author Thomas Vachuska
 */
class DeviceTypeNameCache {

    // FIXME: add javadocs and unit tests

    private static final String DEFAULT_TYPE_NAME = "switch.openflow";
    private static final String DELIM = "~";

    private final Map<String, String> typeNames = new ConcurrentHashMap<>();

    String getTypeName(String mfr, String hw, String fw) {
        String typeName = typeNames.get(key(mfr, hw, fw));
        return typeName != null ? typeName : DEFAULT_TYPE_NAME;
    }

    private String key(String mfr, String hw, String fw) {
        return mfr + DELIM + hw + DELIM + fw;
    }

    void addTypeName(String mfr, String hw, String fw, String typeName) {
        typeNames.put(key(mfr, hw, fw), typeName);
    }

    void removeTypeName(String mfr, String hw, String fw) {
        typeNames.remove(key(mfr, hw, fw));
    }

}
