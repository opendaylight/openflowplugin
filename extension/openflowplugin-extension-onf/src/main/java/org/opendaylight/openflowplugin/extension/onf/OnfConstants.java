/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.onf;

/**
 * Stores common constants for ONF approved extensions.
 */
public abstract class OnfConstants {

    /** Experimenter ID of ONF approved extensions */
    public static final long ONF_EXPERIMENTER_ID = 0x4F4E4600;
    /** ONF_ET_BUNDLE_CONTROL message type */
    public static final int ONF_ET_BUNDLE_CONTROL = 2300;
    /** ONF_ET_BUNDLE_ADD_MESSAGE message type */
    public static final int ONF_ET_BUNDLE_ADD_MESSAGE = 2301;

}
