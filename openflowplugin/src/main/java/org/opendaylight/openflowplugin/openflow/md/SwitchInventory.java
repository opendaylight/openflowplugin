package org.opendaylight.openflowplugin.openflow.md;

import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;

public interface SwitchInventory {
    
    ModelDrivenSwitch getSwitch(NodeRef node);
}
