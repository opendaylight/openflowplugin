package org.opendaylight.openflowplugin.openflow.md.core.translator;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.node.error.notification.object.reference.MeterRefBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterInput;

public class MeterEntityData extends AbstractDPNEntity {

    MeterRefBuilder meterRef = new MeterRefBuilder();

    @Override
    public MeterRefBuilder getBuilder(Object object) {
        // TODO Auto-generated method stub
        if (object instanceof AddMeterInput) {
            AddMeterInput addMeterinput = ((AddMeterInput) object);
            meterRef.setMeterRef(addMeterinput.getMeterRef());
        } else if (object instanceof UpdateMeterInput) {
            UpdateMeterInput updateMeterinput = ((UpdateMeterInput) object);
            meterRef.setMeterRef(updateMeterinput.getMeterRef());
        } else {
            RemoveMeterInput removeMeterinput = ((RemoveMeterInput) object);
            meterRef.setMeterRef(removeMeterinput.getMeterRef());
        }
        return meterRef;
    }

}
