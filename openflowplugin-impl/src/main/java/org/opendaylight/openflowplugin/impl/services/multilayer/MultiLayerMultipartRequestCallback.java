package org.opendaylight.openflowplugin.impl.services.multilayer;

import java.util.List;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.EventIdentifier;
import org.opendaylight.openflowplugin.impl.services.AbstractMultipartRequestCallback;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;

public class MultiLayerMultipartRequestCallback extends AbstractMultipartRequestCallback<MultipartReply> {

    MultiLayerMultipartRequestCallback(RequestContext<List<MultipartReply>> context, Class<?> requestType, DeviceContext deviceContext, EventIdentifier eventIdentifier) {
        super(context, requestType, deviceContext, eventIdentifier);
    }

    @Override
    protected boolean isMultipart(final OfHeader result) {
        return result instanceof MultipartReply;
    }

    @Override
    protected boolean isReqMore(final MultipartReply result) {
        return result.getFlags().isOFPMPFREQMORE();
    }

}
