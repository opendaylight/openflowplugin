package org.opendaylight.openflowplugin.openflow.md;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev130819.FlowStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;

public interface ModelDrivenSwitch extends SalFlowService, PacketProcessingService {

}
