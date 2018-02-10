/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChainClosedException;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceRegistry;
import org.opendaylight.openflowplugin.api.openflow.device.TxFacade;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.DeviceFlowRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.group.DeviceGroupRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.meter.DeviceMeterRegistry;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.EventIdentifier;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.StatisticsGatherer;
import org.opendaylight.openflowplugin.impl.common.MultipartReplyTranslatorUtil;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProvider;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableStatisticsGatheringStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableStatisticsGatheringStatusBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.snapshot.gathering.status.grouping.SnapshotGatheringStatusEnd;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.snapshot.gathering.status.grouping.SnapshotGatheringStatusEndBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.snapshot.gathering.status.grouping.SnapshotGatheringStatusStartBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utils for gathering statistics.
 */
public final class StatisticsGatheringUtils {

    private static final String DATE_AND_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    private static final Logger LOG = LoggerFactory.getLogger(StatisticsGatheringUtils.class);
    private static final String QUEUE2_REQCTX = "QUEUE2REQCTX-";

    private StatisticsGatheringUtils() {
        throw new IllegalStateException("This class should not be instantiated.");
    }

    static <T extends OfHeader> ListenableFuture<Boolean> gatherStatistics(final StatisticsGatherer<T>
                                                                                   statisticsGatheringService,
                                                                          final DeviceInfo deviceInfo,
                                                                          final MultipartType type,
                                                                          final TxFacade txFacade,
                                                                          final DeviceRegistry registry,
                                                                          final ConvertorExecutor convertorExecutor,
                                                                          final MultipartWriterProvider
                                                                                   statisticsWriterProvider) {
        return Futures.transform(
                statisticsGatheringService.getStatisticsOfType(
                        new EventIdentifier(QUEUE2_REQCTX + type.toString(), deviceInfo.getNodeId().toString()),
                        type),
                new AsyncFunction<RpcResult<List<T>>, Boolean>() {
                    @Nullable
                    @Override
                    public ListenableFuture<Boolean> apply(@Nonnull final RpcResult<List<T>> rpcResult) {
                        boolean isMultipartProcessed = Boolean.TRUE;

                        if (rpcResult.isSuccessful()) {
                            LOG.debug("Stats reply successfully received for node {} of type {}",
                                    deviceInfo.getNodeId(), type);

                            // TODO: in case the result value is null
                            // then multipart data probably got processed on the fly -
                            // TODO: this contract should by clearly stated
                            // and enforced - now simple true value is returned
                            if (Objects.nonNull(rpcResult.getResult()) && !rpcResult.getResult().isEmpty()) {
                                final List<DataContainer> allMultipartData;

                                try {
                                    allMultipartData = rpcResult
                                            .getResult()
                                            .stream()
                                            .map(reply ->  MultipartReplyTranslatorUtil
                                                    .translate(reply, deviceInfo, convertorExecutor, null))
                                            .filter(java.util.Optional::isPresent)
                                            .map(java.util.Optional::get)
                                            .collect(Collectors.toList());
                                } catch (final TransactionChainClosedException | ClassCastException  e) {
                                    LOG.warn("Stats processing of type {} for node {}"
                                                    + "failed during transformation step",
                                            type, deviceInfo.getLOGValue(), e);
                                    return Futures.immediateFailedFuture(e);
                                }

                                try {
                                    return Futures.immediateFuture(processStatistics(
                                            type,
                                            allMultipartData,
                                            txFacade,
                                            registry,
                                            deviceInfo,
                                            statisticsWriterProvider));
                                } catch (final TransactionChainClosedException e) {
                                    LOG.warn("Stats processing of type {} for node {} failed during processing step",
                                            type, deviceInfo.getNodeId(), e);
                                    return Futures.immediateFailedFuture(e);
                                }
                            } else {
                                LOG.debug("Stats reply was empty for node {} of type {}", deviceInfo.getNodeId(), type);
                            }
                        } else {
                            LOG.warn("Stats reply FAILED for node {} of type {}: {}", deviceInfo.getNodeId(), type,
                                    rpcResult.getErrors());
                            isMultipartProcessed = Boolean.FALSE;
                        }

                        return Futures.immediateFuture(isMultipartProcessed);
                    }
                });
    }

    private static boolean processStatistics(final MultipartType type,
                                             final List<? extends DataContainer> statistics,
                                             final TxFacade txFacade,
                                             final DeviceRegistry deviceRegistry,
                                             final DeviceInfo deviceInfo,
                                             final MultipartWriterProvider statisticsWriterProvider) {
        final InstanceIdentifier<FlowCapableNode> instanceIdentifier = deviceInfo
                .getNodeInstanceIdentifier()
                .augmentation(FlowCapableNode.class);

        switch (type) {
            case OFPMPFLOW:
                LOG.debug("deleteAllKnownFlows device {}, flow size - {}",
                        deviceInfo,deviceRegistry.getDeviceFlowRegistry().size());
                deleteAllKnownFlows(txFacade, instanceIdentifier, deviceRegistry.getDeviceFlowRegistry());
                deviceRegistry.getDeviceFlowRegistry().processMarks();
                break;
            case OFPMPMETERCONFIG:
                LOG.debug("deleteAllKnownMeters device {}",deviceInfo);
                deviceRegistry.getDeviceMeterRegistry().processMarks();
                deleteAllKnownMeters(txFacade, instanceIdentifier, deviceRegistry.getDeviceMeterRegistry());
                deviceRegistry.getDeviceMeterRegistry().processMarks();
                break;
            case OFPMPGROUPDESC:
                LOG.debug("deleteAllKnownGroups OFPMPGROUPDESC device {}, group size - {}, ",
                        deviceInfo,deviceRegistry.getDeviceGroupRegistry().size());
               // deviceRegistry.getDeviceGroupRegistry().processMarks();
                deleteAllKnownGroups(txFacade, instanceIdentifier, deviceRegistry.getDeviceGroupRegistry());
                deviceRegistry.getDeviceGroupRegistry().processMarks();
                break;
            default:
                //no op
        }

        if (writeStatistics(type, statistics, deviceInfo, statisticsWriterProvider)) {
            txFacade.submitTransaction();
            LOG.debug("Stats reply added to transaction for node {} of type {}", deviceInfo.getNodeId(), type);
            return true;
        }

        LOG.warn("Stats processing of type {} for node {} failed during write-to-tx step",
                type, deviceInfo.getLOGValue());
        return false;
    }

    private static boolean writeStatistics(final MultipartType type,
                                           final List<? extends DataContainer> statistics,
                                           final DeviceInfo deviceInfo,
                                           final MultipartWriterProvider statisticsWriterProvider) {
        final AtomicBoolean result = new AtomicBoolean(false);

        try {
            statistics.forEach(stat -> statisticsWriterProvider.lookup(type).ifPresent(p -> {
                final boolean write = p.write(stat, false);

                if (!result.get()) {
                    result.set(write);
                }
            }));
        } catch (final TransactionChainClosedException ex) {
            LOG.warn("Stats processing of type {} for node {} failed during write-to-tx step",
                    type, deviceInfo.getLOGValue(), ex);
        }

        return result.get();
    }

    public static void deleteAllKnownFlows(final TxFacade txFacade,
                                           final InstanceIdentifier<FlowCapableNode> instanceIdentifier,
                                           final DeviceFlowRegistry deviceFlowRegistry) {
        if (!txFacade.isTransactionsEnabled()) {
            return;
        }

        final CheckedFuture<Optional<FlowCapableNode>, ReadFailedException> future;
        try (ReadOnlyTransaction readTx = txFacade.getReadTransaction()) {
            future = readTx.read(LogicalDatastoreType.OPERATIONAL, instanceIdentifier);
        }

        try {
            Futures.transform(Futures
                    .withFallback(future, t -> {
                        // we wish to close readTx for fallBack
                        return Futures.immediateFailedFuture(t);
                    }), (Function<Optional<FlowCapableNode>, Void>)
                flowCapNodeOpt -> {
                        // we have to read actual tables with all information
                       // before we set empty Flow list, merge is expensive and
                        // not applicable for lists
                    if (flowCapNodeOpt != null && flowCapNodeOpt.isPresent()) {
                        for (final Table tableData : flowCapNodeOpt.get().getTable()) {
                            final Table table = new TableBuilder(tableData).setFlow(Collections.emptyList()).build();
                            final InstanceIdentifier<Table> iiToTable = instanceIdentifier.child(Table.class,
                                    tableData.getKey());
                            txFacade.writeToTransaction(LogicalDatastoreType.OPERATIONAL, iiToTable, table);
                        }
                    }

                    return null;
                }).get();
        } catch (InterruptedException | ExecutionException ex) {
            LOG.debug("Failed to delete {} flows, exception: {}", deviceFlowRegistry.size(), ex);
        }
    }

    public static void deleteAllKnownMeters(final TxFacade txFacade,
                                            final InstanceIdentifier<FlowCapableNode> instanceIdentifier,
                                            final DeviceMeterRegistry meterRegistry) {
        meterRegistry.forEach(meterId -> {
            txFacade
                .addDeleteToTxChain(
                        LogicalDatastoreType.OPERATIONAL,
                        instanceIdentifier.child(Meter.class, new MeterKey(meterId)));
            meterRegistry.addMark(meterId);
        });
    }

    public static void deleteAllKnownGroups(final TxFacade txFacade,
                                            final InstanceIdentifier<FlowCapableNode> instanceIdentifier,
                                            final DeviceGroupRegistry groupRegistry) {
        LOG.debug("deleteAllKnownGroups on device targetType {}",instanceIdentifier.getTargetType());
        groupRegistry.forEach(groupId -> {
            txFacade
                .addDeleteToTxChain(
                        LogicalDatastoreType.OPERATIONAL,
                        instanceIdentifier.child(Group.class, new GroupKey(groupId)));
            groupRegistry.addMark(groupId);
        });
    }

    /**
     * Writes snapshot gathering start timestamp + cleans end mark.
     *
     * @param deviceContext txManager + node path keeper
     */

    static void markDeviceStateSnapshotStart(final DeviceContext deviceContext) {
        LOG.debug("markDeviceStateSnapshotStart on device {}",deviceContext.getDeviceInfo());
        final InstanceIdentifier<FlowCapableStatisticsGatheringStatus> statusPath = deviceContext.getDeviceInfo()
                .getNodeInstanceIdentifier().augmentation(FlowCapableStatisticsGatheringStatus.class);

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_AND_TIME_FORMAT);
        final FlowCapableStatisticsGatheringStatus gatheringStatus = new FlowCapableStatisticsGatheringStatusBuilder()
                .setSnapshotGatheringStatusStart(new SnapshotGatheringStatusStartBuilder()
                        .setBegin(new DateAndTime(simpleDateFormat.format(new Date())))
                        .build())
                .setSnapshotGatheringStatusEnd(null) // TODO: reconsider if really need to clean end mark here
                .build();
        try {
            deviceContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, statusPath, gatheringStatus);
        } catch (final TransactionChainClosedException e) {
            LOG.warn("Can't write to transaction, transaction chain probably closed.");
            LOG.trace("Write to transaction exception: ", e);
        }

        deviceContext.submitTransaction();
    }

    /**
     * Writes snapshot gathering end timestamp + outcome.
     *
     * @param deviceContext txManager + node path keeper
     * @param succeeded     outcome of currently finished gathering
     */
    static void markDeviceStateSnapshotEnd(final DeviceContext deviceContext, final boolean succeeded) {
        LOG.debug("markDeviceStateSnapshotEnd for device {}",deviceContext.getDeviceInfo());
        final InstanceIdentifier<SnapshotGatheringStatusEnd> statusEndPath = deviceContext.getDeviceInfo()
                .getNodeInstanceIdentifier().augmentation(FlowCapableStatisticsGatheringStatus.class)
                .child(SnapshotGatheringStatusEnd.class);

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_AND_TIME_FORMAT);
        final SnapshotGatheringStatusEnd gatheringStatus = new SnapshotGatheringStatusEndBuilder()
                .setEnd(new DateAndTime(simpleDateFormat.format(new Date())))
                .setSucceeded(succeeded)
                .build();
        try {
            deviceContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, statusEndPath, gatheringStatus);
        } catch (TransactionChainClosedException e) {
            LOG.warn("Can't write to transaction, transaction chain probably closed.");
            LOG.trace("Write to transaction exception: ", e);
        }

        deviceContext.submitTransaction();
    }
}
