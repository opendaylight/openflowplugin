/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.md.queue;

/**
 * message harvester simple control.
 */
public interface HarvesterHandle {

    /**
     * wakeup harvester in case it is in phase of starving sleep.
     */
    void ping();

}
