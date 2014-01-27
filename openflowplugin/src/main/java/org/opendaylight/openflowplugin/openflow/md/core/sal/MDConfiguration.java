/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal;

/**
 * 
 */
public interface MDConfiguration {
    
    /**
     * @return true, if switch is supposed to be cleaned upon connection (flows, meters, groups)
     */
    boolean isCleanSwitchUponConnect();
    /**
     * @return true, if config context is supposed to be cleaned upon switch disconnect (flows, meters, groups)
     */
    boolean isCleanConfigUponSwitchDisconnect();

}
