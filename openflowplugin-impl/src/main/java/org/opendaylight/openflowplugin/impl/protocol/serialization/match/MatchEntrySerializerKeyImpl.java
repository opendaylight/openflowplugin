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
    private final int oxmClass;
    private final int oxmField;

    /**
     * Create new instance of MatchEntrySerializerKeyImpl
     * @param version openflow version
     * @param oxmClass match entry oxm class
     * @param oxmField match entry field code
     */
    public MatchEntrySerializerKeyImpl(final short version, final int oxmClass, final int oxmField) {
        this.version = version;
        this.oxmClass = oxmClass;
        this.oxmField = oxmField;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + oxmClass;
        result = prime * result + oxmField;
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

        final MatchEntrySerializerKeyImpl other = (MatchEntrySerializerKeyImpl) obj;


        return oxmClass == other.oxmClass
                && oxmField == other.oxmField
                && version == other.version;
    }


    @Override
    public String toString() {
        return "version: " + version
                + " oxmClass:" + oxmClass
                + " oxmField: " + oxmField;
    }

}
