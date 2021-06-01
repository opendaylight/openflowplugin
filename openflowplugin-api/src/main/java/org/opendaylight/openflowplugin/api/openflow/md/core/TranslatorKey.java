/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.md.core;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import org.opendaylight.yangtools.yang.common.Uint8;

public class TranslatorKey {
    private final Uint8 version;
    private final String messageClass;

    /**
     * Constructor.
     * @param version version
     * @param messageClass message class
     */
    public TranslatorKey(final Uint8 version, final String messageClass) {
        this.version = requireNonNull(version);
        this.messageClass = messageClass;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + (messageClass == null ? 0 : messageClass.hashCode());
        result = prime * result + version.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        TranslatorKey other = (TranslatorKey) obj;
        return Objects.equals(messageClass, other.messageClass) && version.equals(other.version);
    }
}
