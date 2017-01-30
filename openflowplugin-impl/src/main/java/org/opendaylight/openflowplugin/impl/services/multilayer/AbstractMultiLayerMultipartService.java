package org.opendaylight.openflowplugin.impl.services.multilayer;

import com.google.common.util.concurrent.FutureCallback;
import java.util.List;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.impl.services.AbstractMultipartService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;

public abstract class AbstractMultiLayerMultipartService<I> extends AbstractMultipartService<I, MultipartReply> {

    protected AbstractMultiLayerMultipartService(RequestContextStack requestContextStack, DeviceContext deviceContext) {
        super(requestContextStack, deviceContext);
    }


    @Override
    protected final FutureCallback<OfHeader> createCallback(final RequestContext<List<MultipartReply>> context, final Class<?> requestType) {
        return new MultiLayerMultipartRequestCallback(context, requestType, getDeviceContext(), getEventIdentifier());
    }

}

