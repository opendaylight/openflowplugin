package org.opendaylight.openflowplugin.openflow.md.util;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.flow.capable.port.State;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.flow.capable.port.StateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortState;

public class PortTranslatorUtil {
    public static  org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures translatePortFeatures(PortFeatures apf) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures napf = null;
        if(apf != null){
                napf = new org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures(
                        apf.is_100gbFd(),apf.is_100mbFd(),apf.is_100mbHd(),apf.is_10gbFd(),apf.is_10mbFd(),apf.is_10mbHd(),
                        apf.is_1gbFd(),apf.is_1gbHd(),apf.is_1tbFd(),apf.is_40gbFd(),apf.isAutoneg(),apf.isCopper(),apf.isFiber(),apf.isOther(),
                        apf.isPause(), apf.isPauseAsym());
        }
        return napf;
    }

    public static  State translatePortState(PortState state) {
        StateBuilder nstate = new StateBuilder();
        if(state !=null) {
            nstate.setBlocked(state.isBlocked());
            nstate.setLinkDown(state.isLinkDown());
            nstate.setLive(state.isLive());
        }
        return nstate.build();
    }

    public static org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig translatePortConfig(PortConfig pc) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig npc = null;
        if(pc != null) {
                npc = new org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig(pc.isNoFwd(),
                        pc.isNoPacketIn(), pc.isNoRecv(), pc.isPortDown());
        }
        return npc;
    }
}
