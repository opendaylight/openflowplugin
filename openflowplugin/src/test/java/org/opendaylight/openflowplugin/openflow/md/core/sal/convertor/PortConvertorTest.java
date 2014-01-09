package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.UpdatePortInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.UpdatePortInputBuilder;

public class PortConvertorTest {


	@Test
	public void testPortModCommandConvertorwithAllParameters()
	{


	    UpdatePortInputBuilder updatePortInputB = new UpdatePortInputBuilder();
	  //  UpdatedPortBuilder updatedPortBuilder = new UpdatedPortBuilder();

        PortFeatures features = new PortFeatures(true, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null);


	   // updatePortInputB.setUpdatedPort(updatedPortBuilder.build());

        UpdatePortInput source = updatePortInputB.build();



      // PortModInput portOut = PortConvertor.toPortModInput();


		return;



	}
}
