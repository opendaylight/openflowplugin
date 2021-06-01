/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.api.keys;

import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Key for an experimenter instruction deserializer.
 *
 * @author michal.polkorab
 */
public final class ExperimenterInstructionDeserializerKey extends InstructionDeserializerKey
        implements ExperimenterDeserializerKey {

    /**
     * Constructor.
     *
     * @param version protocol wire version
     * @param experimenterId experimenter (vendor) identifier
     */
    public ExperimenterInstructionDeserializerKey(final Uint8 version, final Long experimenterId) {
        super(version, EncodeConstants.EXPERIMENTER_VALUE, experimenterId);
    }
}
