package org.opendaylight.openflowplugin.applications.frm;

/**
 * Created by eshuvka on 10/23/2015.
 */
public interface FlowNodeConnectorInventoryTranslator {

    public boolean isNodeConnectorUpdated(long dpId, String portName);
}
