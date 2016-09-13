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

    /** flow add operation */FLOW_ADD,
    /** flow remove operation */FLOW_REMOVE,
    /** flow update operation */FLOW_UPDATE,

    /** group add operation */GROUP_ADD,
    /** group remove operation */GROUP_REMOVE,
    /** group update operation */GROUP_UPDATE,

    /** meter add operation */METER_ADD,
    /** meter remove operation */METER_REMOVE,
    /** meter update operation */METER_UPDATE
}
