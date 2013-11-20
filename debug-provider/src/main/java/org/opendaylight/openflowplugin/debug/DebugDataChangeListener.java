package org.opendaylight.openflowplugin.debug;
import org.opendaylight.controller.md.sal.common.api.data.DataChangeEvent;
import org.opendaylight.controller.sal.binding.api.data.DataChangeListener;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DebugDataChangeListener implements DataChangeListener {
    static Logger LOG = LoggerFactory.getLogger(DebugDataChangeListener.class);

    @Override
    public void onDataChanged(
            DataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
        LOG.error("DataChangeEvent: " + change.getUpdatedOperationalData());

    }

}
