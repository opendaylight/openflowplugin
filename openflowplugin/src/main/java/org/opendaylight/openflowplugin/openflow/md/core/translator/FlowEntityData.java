package org.opendaylight.openflowplugin.openflow.md.core.translator;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.node.error.notification.object.reference.FlowRefBuilder;

public class FlowEntityData extends AbstractDPNEntity {

    FlowRefBuilder flowRef = new FlowRefBuilder();

    @Override
    public FlowRefBuilder getBuilder(Object object) {
        // TODO Auto-generated method stub
        if (object instanceof AddFlowInput) {
            AddFlowInput addFlowinput = ((AddFlowInput) object);
            flowRef.setFlowRef(addFlowinput.getFlowRef());
        } else if (object instanceof RemoveFlowInput) {

            RemoveFlowInput removeFlowinput = ((RemoveFlowInput) object);
            flowRef.setFlowRef(removeFlowinput.getFlowRef());
        } else {
            UpdateFlowInput updateFlowinput = ((UpdateFlowInput) object);
            flowRef.setFlowRef(updateFlowinput.getFlowRef());
        }
        return flowRef;
    }

}
