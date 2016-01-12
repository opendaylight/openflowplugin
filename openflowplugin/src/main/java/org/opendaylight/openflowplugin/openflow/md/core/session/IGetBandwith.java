/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.session;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortGrouping;

/**
 * Utility for extracting bandwith from port according to port version
 * 
 * @author jsebin 
 * 
 */
public interface IGetBandwith {

	/**
	 * 
	 * @param port port group
	 * @return port bandwidth
	 */
    public boolean getBandwidth(PortGrouping port);
}
