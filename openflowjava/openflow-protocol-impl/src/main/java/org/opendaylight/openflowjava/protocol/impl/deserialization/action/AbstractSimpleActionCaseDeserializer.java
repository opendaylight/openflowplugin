/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.action;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.openflowjava.protocol.api.extensibility.ActionDeserializer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.ActionChoice;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint8;

abstract class AbstractSimpleActionCaseDeserializer<T extends ActionChoice> extends AbstractActionCaseDeserializer<T>
        implements ActionDeserializer.OFProvider {
    private final @NonNull Uint8 version;
    private final @NonNull Uint16 type;

    AbstractSimpleActionCaseDeserializer(final @NonNull T emptyChoice, final @NonNull Uint8 version,
            final @NonNull Uint16 type) {
        super(emptyChoice);
        this.version = requireNonNull(version);
        this.type = requireNonNull(type);
    }

    @Override
    public final Uint8 version() {
        return version;
    }

    @Override
    public final Uint16 type() {
        return type;
    }

    @Override
    public final ActionDeserializer deserializer() {
        return this;
    }
}
