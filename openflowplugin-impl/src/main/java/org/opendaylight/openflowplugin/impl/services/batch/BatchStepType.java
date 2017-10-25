/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services.batch;

/**
 * Batch step types - holding combinations of target object type and action type.
 */
public enum BatchStepType {

    /** Flow add operation. */FLOW_ADD,
    /** Flow remove operation. */FLOW_REMOVE,
    /** Flow update operation. */FLOW_UPDATE,

    /** Group add operation. */GROUP_ADD,
    /** Group remove operation. */GROUP_REMOVE,
    /** Group update operation. */GROUP_UPDATE,

    /** Meter add operation. */METER_ADD,
    /** Meter remove operation. */METER_REMOVE,
    /** Meter update operation. */METER_UPDATE
}
