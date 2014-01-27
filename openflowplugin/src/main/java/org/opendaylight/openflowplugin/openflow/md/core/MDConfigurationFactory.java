/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core;

import org.opendaylight.openflowplugin.openflow.md.core.sal.MDConfiguration;

/**
 * 
 */
public abstract class MDConfigurationFactory {

    /**
     * @return configuration with cleaning [ switch + config context ] turned ON 
     */
    public static MDConfiguration createDefaultConfiguration() {
        return new MDConfiguration() {
            
            @Override
            public boolean isCleanSwitchUponConnect() {
                return true;
            }
            
            @Override
            public boolean isCleanConfigUponSwitchDisconnect() {
                return true;
            }
        };
    }
    
}
