/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.api.keys;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.instruction.container.instruction.choice.ExperimenterIdCase;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Key for an experimenter instruction serializer.
 *
 * @author michal.polkorab
 */
public final class ExperimenterInstructionSerializerKey extends InstructionSerializerKey<ExperimenterIdCase> {
    /**
     * Constructor.
     *
     * @param msgVersion protocol wire version
     * @param experimenterId experimenter / vendor ID
     */
    public ExperimenterInstructionSerializerKey(final Uint8 msgVersion, final Long experimenterId) {
        super(msgVersion, ExperimenterIdCase.class, experimenterId);
    }
}
