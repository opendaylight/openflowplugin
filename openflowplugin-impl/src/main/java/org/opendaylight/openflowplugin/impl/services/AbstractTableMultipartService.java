package org.opendaylight.openflowplugin.impl.services;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.services.util.RequestInputUtils;
import org.opendaylight.openflowplugin.impl.services.util.ServiceException;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestTableFeaturesCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.table.features._case.MultipartRequestTableFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.table.features._case.multipart.request.table.features.TableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTableOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeaturesKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;

public abstract class AbstractTableMultipartService<T extends OfHeader> extends AbstractMultipartService<UpdateTableInput, T> {

    private final ConvertorExecutor convertorExecutor;
    private final VersionConvertorData data;

    protected AbstractTableMultipartService(RequestContextStack requestContextStack, DeviceContext deviceContext,
                                            ConvertorExecutor convertorExecutor) {
        super(requestContextStack, deviceContext);
        this.convertorExecutor = convertorExecutor;
        data = new VersionConvertorData(getVersion());
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final UpdateTableInput input) throws ServiceException {
        final Optional<List<TableFeatures>> tableFeatures = getConvertorExecutor().convert(input.getUpdatedTable(), data);

        return RequestInputUtils.createMultipartHeader(MultipartType.OFPMPTABLEFEATURES, xid.getValue(), getVersion())
            .setMultipartRequestBody(new MultipartRequestTableFeaturesCaseBuilder()
                .setMultipartRequestTableFeatures(new MultipartRequestTableFeaturesBuilder()
                    .setTableFeatures(tableFeatures
                        .orElseGet(Collections::emptyList))
                    .build())
                .build())
            .build();
    }

    /**
     * Get convertor executor
     * @return convertor executor
     */
    protected ConvertorExecutor getConvertorExecutor() {
        return convertorExecutor;
    }

    /**
     * Get data
     * @return data
     */
    protected VersionConvertorData getData() {
        return data;
    }

    /**
     * Stores table features to operational datastore
     */
    protected void storeStatistics(List<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures> result) {
        final InstanceIdentifier<FlowCapableNode> flowCapableNodeII = InstanceIdentifier
            .create(Nodes.class)
            .child(Node.class, new NodeKey(getDeviceInfo().getNodeId()))
            .augmentation(FlowCapableNode.class);

        result.forEach(tableFeatures -> getTxFacade()
            .writeToTransaction(
                LogicalDatastoreType.OPERATIONAL,
                flowCapableNodeII
                    .child(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features
                        .TableFeatures.class, new TableFeaturesKey(tableFeatures.getTableId())),
                tableFeatures));

        getTxFacade().submitTransaction();
    }

    /**
     * Process experimenter input and result experimenter output
     * @param input experimenter input
     * @return experimenter output
     */
    public abstract Future<RpcResult<UpdateTableOutput>> handleAndReply(UpdateTableInput input);

}
