/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.facet;

import org.opendaylight.util.driver.HandlerFacet;
import org.opendaylight.net.model.ElementId;
import org.opendaylight.net.model.InterfaceInfo;

import java.util.List;

/**
 * Query the device for its interfaces.
 *
 * @author Sean Humphress
 */
public interface InterfaceHandler extends HandlerFacet {

    /**
     * Retrieve the list of interfaces discovered by fetch().
     * 
     * @return interfaces found on the device
     */
    public List<InterfaceInfo> getInterfaces();

    /**
     * ElementId of the device that hosts the interfaces.
     * 
     * @param elementId device id
     */
    public void setHostedBy(ElementId elementId);
}
