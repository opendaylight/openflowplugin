/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services.batch;

/**
 * batch step types - holding combinations of target object type and action type (e.g.: flow + update)
 */
public enum BatchStepType {

    /** flow -&gt; add operation */FLOW_ADD,
    /** flow -&gt; remove operation */FLOW_REMOVE,
    /** flow -&gt; update operation */FLOW_UPDATE,

    /** group -&gt; add operation */GROUP_ADD,
    /** group -&gt; remove operation */GROUP_REMOVE,
    /** group -&gt; update operation */GROUP_UPDATE,

    /** meter -&gt; add operation */METER_ADD,
    /** meter -&gt; remove operation */METER_REMOVE,
    /** meter -&gt; update operation */METER_UPDATE
}
