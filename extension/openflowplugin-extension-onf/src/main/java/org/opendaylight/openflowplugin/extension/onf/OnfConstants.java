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
    Uint32 ONF_EXPERIMENTER = Uint32.valueOf(0x4F4E4600).intern();

    /**
     * Experimenter ID of ONF approved extensions.
     */
    ExperimenterId ONF_EXPERIMENTER_ID = new ExperimenterId(ONF_EXPERIMENTER);

    /**
     * ONF_ET_BUNDLE_CONTROL message type.
     */
    Uint32 ONF_ET_BUNDLE_CONTROL = Uint32.valueOf(2300).intern();

    /**
     * ONF_ET_BUNDLE_ADD_MESSAGE message type.
     */
    Uint32 ONF_ET_BUNDLE_ADD_MESSAGE = Uint32.valueOf(2301).intern();
}
