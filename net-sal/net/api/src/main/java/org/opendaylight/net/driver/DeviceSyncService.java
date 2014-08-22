/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.driver;

/**
 * Interface used to inform the DeviceStoreManager whether the local system has 
 * been selected as the base system by the CordDataSynchronizer.
 *
 * @author Steve Dean
 */
public interface DeviceSyncService {
    /**
     * Specifies whether the local system has been selected as the base system.
     * 
     * @param base true if local system is base system
     */
    public void setBase(boolean base);
}
