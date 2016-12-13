/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.deserialization.key;

import java.util.Objects;

import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.openflowplugin.extension.api.path.MatchPath;

public class MessageCodeMatchKey extends MessageCodeKey {

    private MatchPath matchPath;

    /**
     * Constructor
     * @param version wire protocol version
     * @param value used as distinguisher (read from binary data / buffer)
     * @param clazz class of object that is going to be deserialized
     * @param matchPath match extension path
     */
    public MessageCodeMatchKey(short version, int value, Class<?> clazz, MatchPath matchPath) {
        super(version, value, clazz);
        this.matchPath = matchPath;
    }

    public MatchPath getMatchPath() {
        return this.matchPath;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        return prime * super.hashCode() + (Objects.isNull(matchPath) ? 0 : matchPath.ordinal());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof MessageCodeMatchKey)) {
            return false;
        }
        MessageCodeMatchKey other = (MessageCodeMatchKey) obj;
        if (matchPath == null) {
            if (other.matchPath != null) {
                return false;
            }
        } else if (!matchPath.equals(other.matchPath)) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return super.toString() + " matchPath: " + matchPath.name();
    }
}
