/*
 * Copyright (c) 2019 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.serialization.match;

public abstract class AbstractPrimitiveEntrySerializer<E> extends AbstractMatchEntrySerializer<E, Void> {
    protected AbstractPrimitiveEntrySerializer(int oxmFieldCode, int oxmClassCode, int valueLength) {
        super(oxmFieldCode, oxmClassCode, valueLength);
    }

    @Override
    protected final Void extractEntryMask(E entry) {
        return null;
    }
}
