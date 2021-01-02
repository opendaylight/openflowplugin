/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.onf;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Stores common constants for ONF approved extensions.
 */
public interface OnfConstants {
    /**
     * Experimenter ID of ONF approved extensions.
     */
    long ONF_EXPERIMENTER_ID_LONG = 0x4F4E4600;

    /**
     * Experimenter ID of ONF approved extensions.
     */
    ExperimenterId ONF_EXPERIMENTER_ID = new ExperimenterId(Uint32.valueOf(ONF_EXPERIMENTER_ID_LONG).intern());

    /**
     * ONF_ET_BUNDLE_CONTROL message type.
     */
    int ONF_ET_BUNDLE_CONTROL = 2300;

    /**
     * ONF_ET_BUNDLE_ADD_MESSAGE message type.
     */
    int ONF_ET_BUNDLE_ADD_MESSAGE = 2301;
}
