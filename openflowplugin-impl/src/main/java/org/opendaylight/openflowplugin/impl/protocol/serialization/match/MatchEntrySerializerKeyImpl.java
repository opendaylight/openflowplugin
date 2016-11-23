/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.match;

import org.opendaylight.openflowplugin.api.openflow.protocol.serialization.MatchEntrySerializerKey;

public class MatchEntrySerializerKeyImpl implements MatchEntrySerializerKey {

    private final short version;
    private final int entryCode;

    /**
     * Create new instance of MatchEntrySerializerKeyImpl
     * @param version openflow version
     * @param entryCode match entry code
     */
    public MatchEntrySerializerKeyImpl(final short version, final int entryCode) {
        this.version = version;
        this.entryCode = entryCode;
    }

    public int getEntryCode() {
        return entryCode;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + entryCode;
        result = prime * result + version;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (!(obj instanceof MatchEntrySerializerKeyImpl)) {
            return false;
        }

        final MatchEntrySerializerKeyImpl other = (MatchEntrySerializerKeyImpl)obj;

        if (entryCode != other.entryCode) {
            return false;
        }

        return version == other.version;
    }


    @Override
    public String toString() {
        return "version: " + version + " entryCode: " + entryCode;
    }

}
