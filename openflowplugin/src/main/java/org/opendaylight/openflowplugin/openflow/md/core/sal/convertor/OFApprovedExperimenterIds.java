/*
 * Copyright (c) 2016 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Created by Anil Vishnoi (avishnoi@Brocade.com) on 8/2/16.
 */
public final class OFApprovedExperimenterIds {
    public static final ExperimenterId MATCH_TCP_FLAGS_EXP_ID = new ExperimenterId(Uint32.valueOf(1330529792));

    private OFApprovedExperimenterIds() {
        // Hidden on purpose
    }
}
