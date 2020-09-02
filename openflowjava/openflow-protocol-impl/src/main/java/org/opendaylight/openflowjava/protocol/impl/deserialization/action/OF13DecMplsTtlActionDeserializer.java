/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.action;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.DecMplsTtlCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.DecMplsTtlCaseBuilder;

/**
 * OF13DecMplsTtlActionDeserializer.
 *
 * @author michal.polkorab
 */
public class OF13DecMplsTtlActionDeserializer extends AbstractEmptyActionDeserializer<DecMplsTtlCase> {
    public OF13DecMplsTtlActionDeserializer() {
        super(new DecMplsTtlCaseBuilder().build());
    }
}
