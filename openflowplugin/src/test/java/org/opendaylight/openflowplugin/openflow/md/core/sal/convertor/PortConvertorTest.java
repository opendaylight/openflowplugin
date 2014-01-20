/**
 * Copyright (c) 2014 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor: usha.m.s@ericsson.com
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.UpdatePortInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.UpdatePortInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.port.update.UpdatedPortBuilder;

public class PortConvertorTest {


	@Test
	public void testPortModCommandConvertorwithAllParameters()
	{


	    UpdatePortInputBuilder updatePortInputB = new UpdatePortInputBuilder();
	    UpdatedPortBuilder updatedPortBuilder = new UpdatedPortBuilder();

        PortFeatures features = new PortFeatures(true, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null);


	    updatePortInputB.setUpdatedPort(updatedPortBuilder.build());

        UpdatePortInput source = updatePortInputB.build();



      // PortModInput portOut = PortConvertor.toPortModInput();


		return;



	}
}
