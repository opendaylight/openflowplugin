package org.opendaylight.openflowplugin.openflow.md.core.session;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.Port;

public interface IGetBandwith {

    public boolean getBandwidth(Port port);
}
