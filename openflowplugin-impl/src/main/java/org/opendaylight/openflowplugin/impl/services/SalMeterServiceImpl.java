/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.Future;
import javax.annotation.Nullable;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.rpc.ItemLifeCycleSource;
import org.opendaylight.openflowplugin.api.openflow.rpc.listener.ItemLifecycleListener;
import org.opendaylight.openflowplugin.impl.util.ErrorUtil;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.SalMeterService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SalMeterServiceImpl implements SalMeterService, ItemLifeCycleSource {
    private static final Logger LOG = LoggerFactory.getLogger(SalMeterServiceImpl.class);
    private final MeterService<AddMeterInput, AddMeterOutput> addMeter;
    private final MeterService<Meter, UpdateMeterOutput> updateMeter;
    private final MeterService<RemoveMeterInput, RemoveMeterOutput> removeMeter;
    private final MeterMessageService<AddMeterOutput> addMeterMessage;
    private final MeterMessageService<UpdateMeterOutput> updateMeterMessage;
    private final MeterMessageService<RemoveMeterOutput> removeMeterMessage;

    private ItemLifecycleListener itemLifecycleListener;
    private final DeviceContext deviceContext;

    public SalMeterServiceImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext, final ConvertorExecutor convertorExecutor) {
        this.deviceContext = deviceContext;
        addMeter = new MeterService<>(requestContextStack, deviceContext, AddMeterOutput.class, convertorExecutor);
        updateMeter = new MeterService<>(requestContextStack, deviceContext, UpdateMeterOutput.class, convertorExecutor);
        removeMeter = new MeterService<>(requestContextStack, deviceContext, RemoveMeterOutput.class, convertorExecutor);

        addMeterMessage = new MeterMessageService<>(requestContextStack, deviceContext, AddMeterOutput.class);
        updateMeterMessage = new MeterMessageService<>(requestContextStack, deviceContext, UpdateMeterOutput.class);
        removeMeterMessage = new MeterMessageService<>(requestContextStack, deviceContext, RemoveMeterOutput.class);
    }

    @Override
    public void setItemLifecycleListener(@Nullable ItemLifecycleListener itemLifecycleListener) {
        this.itemLifecycleListener = itemLifecycleListener;
    }

    @Override
    public Future<RpcResult<AddMeterOutput>> addMeter(final AddMeterInput input) {
        final ListenableFuture<RpcResult<AddMeterOutput>> resultFuture = addMeterMessage.isSupported()
            ? addMeterMessage.handleServiceCall(input)
            : addMeter.handleServiceCall(input);

        Futures.addCallback(resultFuture, new FutureCallback<RpcResult<AddMeterOutput>>() {
            @Override
            public void onSuccess(@Nullable RpcResult<AddMeterOutput> result) {
                if (result.isSuccessful()) {
                   if (LOG.isDebugEnabled()) {
                        LOG.debug("Meter add with id={} finished without error", input.getMeterId());
                    }
                    deviceContext.getDeviceMeterRegistry().store(input.getMeterId());
                    addIfNecessaryToDS(input.getMeterId(),input);
                } else {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Meter add with id={} failed, errors={}", input.getMeterId(),
                                ErrorUtil.errorsToString(result.getErrors()));
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
                 LOG.warn("Service call for adding meter={} failed, reason: {}", input.getMeterId(), t);
            }
        });
        return resultFuture;
    }

    @Override
    public Future<RpcResult<UpdateMeterOutput>> updateMeter(final UpdateMeterInput input) {
        final ListenableFuture<RpcResult<UpdateMeterOutput>> resultFuture = updateMeterMessage.isSupported()
            ? updateMeterMessage.handleServiceCall(input.getUpdatedMeter())
            : updateMeter.handleServiceCall(input.getUpdatedMeter());

        Futures.addCallback(resultFuture, new FutureCallback<RpcResult<UpdateMeterOutput>>() {

            @Override
            public void onSuccess(@Nullable RpcResult<UpdateMeterOutput> result) {
                if (result.isSuccessful()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Meter update with id={} finished without error", input.getOriginalMeter().getMeterId());
                    }
                    if (itemLifecycleListener != null) {
                        removeIfNecessaryFromDS(input.getOriginalMeter().getMeterId());
                        addIfNecessaryToDS(input.getUpdatedMeter().getMeterId(),input.getUpdatedMeter());
                    }
                } else {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Meter update with id={} failed, errors={}", input.getOriginalMeter().getMeterId(),
                                ErrorUtil.errorsToString(result.getErrors()));
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
                LOG.warn("Service call for updating meter={} failed, reason: {}",
                        input.getOriginalMeter().getMeterId(),t);
            }
        });
        return resultFuture;
    }

    @Override
    public Future<RpcResult<RemoveMeterOutput>> removeMeter(final RemoveMeterInput input) {
        removeMeter.getDeviceRegistry().getDeviceMeterRegistry().markToBeremoved(input.getMeterId());
        final ListenableFuture<RpcResult<RemoveMeterOutput>> resultFuture = removeMeterMessage.isSupported()
            ? removeMeterMessage.handleServiceCall(input)
            : removeMeter.handleServiceCall(input);

        Futures.addCallback(resultFuture, new FutureCallback<RpcResult<RemoveMeterOutput>>() {
            @Override
            public void onSuccess(@Nullable RpcResult<RemoveMeterOutput> result) {
                if (result.isSuccessful()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Meter remove with id={} finished without error", input.getMeterId());
                    }
                    removeIfNecessaryFromDS(input.getMeterId());
                } else {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Meter remove with id={} failed, errors={}", input.getMeterId(),
                                ErrorUtil.errorsToString(result.getErrors()));
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
                LOG.warn("Service call for removing meter={} failed, reason: {}",input.getMeterId(),t);
            }
        });
        return resultFuture;
    }

    private void removeIfNecessaryFromDS(final MeterId meterId) {
        if (itemLifecycleListener != null) {
            KeyedInstanceIdentifier<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter, MeterKey> meterPath
                    = createMeterPath(meterId, deviceContext.getDeviceInfo().getNodeInstanceIdentifier());
            itemLifecycleListener.onRemoved(meterPath);
        }
    }

    private void addIfNecessaryToDS(final MeterId meterId, final Meter data) {
        if (itemLifecycleListener != null) {
            KeyedInstanceIdentifier<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter, MeterKey> groupPath
                    = createMeterPath(meterId, deviceContext.getDeviceInfo().getNodeInstanceIdentifier());
            itemLifecycleListener.onAdded(groupPath, new MeterBuilder(data).build());
        }
    }

    private static KeyedInstanceIdentifier<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter, MeterKey> createMeterPath(final MeterId meterId, final KeyedInstanceIdentifier<Node, NodeKey> nodePath) {
        return nodePath.augmentation(FlowCapableNode.class).child(org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter.class, new MeterKey(meterId));
    }
}
