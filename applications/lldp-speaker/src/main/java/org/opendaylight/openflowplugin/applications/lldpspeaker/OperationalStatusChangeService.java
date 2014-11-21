package org.opendaylight.openflowplugin.applications.lldpspeaker;

import java.util.concurrent.Future;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.applications.lldp.speaker.rev141023.ChangeOperationalStatusInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.applications.lldp.speaker.rev141023.LldpSpeakerService;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * Created by Martin Bobak mbobak@cisco.com on 11/20/14.
 */
public class OperationalStatusChangeService implements LldpSpeakerService {

    private LLDPSpeaker speakerInstance;

    public OperationalStatusChangeService(LLDPSpeaker speakerInstance) {
        this.speakerInstance = speakerInstance;
    }

    @Override
    public Future<RpcResult<Void>> changeOperationalStatus(ChangeOperationalStatusInput input) {
        speakerInstance.setOperationalStatus(input.getOperationalStatus());
        return null;
    }
}
