/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.binding.api.TransactionChainClosedException;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
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
import org.opendaylight.yangtools.binding.DataContainer;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utils for gathering statistics.
 */
public final class StatisticsGatheringUtils {
    private static final Logger LOG = LoggerFactory.getLogger(StatisticsGatheringUtils.class);
    private static final String QUEUE2_REQCTX = "QUEUE2REQCTX-";

    private StatisticsGatheringUtils() {
        // Hidden on purpose
    }

    static <T extends OfHeader> ListenableFuture<Boolean> gatherStatistics(
            final StatisticsGatherer<T> statisticsGatheringService, final DeviceInfo deviceInfo,
            final MultipartType type, final TxFacade txFacade, final DeviceRegistry registry,
            final ConvertorExecutor convertorExecutor, final MultipartWriterProvider statisticsWriterProvider,
            final Executor executor) {
        return Futures.transform(statisticsGatheringService.getStatisticsOfType(
            new EventIdentifier(QUEUE2_REQCTX + type.toString(), deviceInfo.getNodeId().toString()), type),
            rpcResult -> {
                final boolean rpcResultIsNull = rpcResult == null;

                if (!rpcResultIsNull && rpcResult.isSuccessful()) {
                    LOG.debug("Stats reply successfully received for node {} of type {}", deviceInfo.getNodeId(), type);
                        // TODO: in case the result value is null then multipart data probably got processed
                        // TODO: on the fly. This contract should by clearly stated and enforced.
                        // TODO: Now simple true value is returned
                    if (rpcResult.getResult() != null && !rpcResult.getResult().isEmpty()) {
                        final List<DataContainer> allMultipartData = rpcResult.getResult().stream()
                                .map(reply -> MultipartReplyTranslatorUtil
                                                    .translate(reply, deviceInfo, convertorExecutor, null))
                                .filter(Optional::isPresent).map(Optional::orElseThrow)
                                .collect(Collectors.toList());

                        return processStatistics(type, allMultipartData, txFacade, registry, deviceInfo,
                                        statisticsWriterProvider);
                    } else {
                        LOG.debug("Stats reply was empty for node {} of type {}", deviceInfo.getNodeId(), type);
                    }
                } else {
                    LOG.warn("Stats reply FAILED for node {} of type {}: {}", deviceInfo.getNodeId(), type,
                                rpcResultIsNull ? "" : rpcResult.getErrors());
                }
                return false;
            }, executor);
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private static boolean processStatistics(final MultipartType type, final List<? extends DataContainer> statistics,
                                             final TxFacade txFacade, final DeviceRegistry deviceRegistry,
                                             final DeviceInfo deviceInfo,
                                             final MultipartWriterProvider statisticsWriterProvider) {
        final var instanceIdentifier = deviceInfo.getNodeInstanceIdentifier().toBuilder()
                .augmentation(FlowCapableNode.class)
                .build();
        try {
            txFacade.acquireWriteTransactionLock();
            switch (type) {
                case OFPMPFLOW:
                    deleteAllKnownFlows(txFacade, instanceIdentifier, deviceRegistry.getDeviceFlowRegistry());
                    deviceRegistry.getDeviceFlowRegistry().processMarks();
                    break;
                case OFPMPMETERCONFIG:
                    deleteAllKnownMeters(txFacade, instanceIdentifier, deviceRegistry.getDeviceMeterRegistry());
                    deviceRegistry.getDeviceMeterRegistry().processMarks();
                    break;
                case OFPMPGROUPDESC:
                    deleteAllKnownGroups(txFacade, instanceIdentifier, deviceRegistry.getDeviceGroupRegistry());
                    deviceRegistry.getDeviceGroupRegistry().processMarks();
                    break;
                default:
                    // no operation
            }

            if (writeStatistics(type, statistics, deviceInfo, statisticsWriterProvider)) {
                txFacade.submitTransaction();

                LOG.debug("Stats reply added to transaction for node {} of type {}", deviceInfo.getNodeId(), type);
                return true;
            }
        } catch (Exception e) {
            LOG.error("Exception while writing statistics to operational inventory for the device {}",
                    deviceInfo.getLOGValue(), e);
        } finally {
            txFacade.releaseWriteTransactionLock();
        }

        LOG.warn("Stats processing of type {} for node {} " + "failed during write-to-tx step", type, deviceInfo);
        return false;
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private static boolean writeStatistics(final MultipartType type, final List<? extends DataContainer> statistics,
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
        } catch (final Exception ex) {
            LOG.warn("Stats processing of type {} for node {} " + "failed during write-to-tx step", type, deviceInfo,
                     ex);
        }

        return result.get();
    }

    public static void deleteAllKnownFlows(final TxFacade txFacade,
                                           final DataObjectIdentifier<FlowCapableNode> instanceIdentifier,
                                           final DeviceFlowRegistry deviceFlowRegistry) {
        if (!txFacade.isTransactionsEnabled()) {
            return;
        }

        final ListenableFuture<Optional<FlowCapableNode>> future;
        try (ReadTransaction readTx = txFacade.getReadTransaction()) {
            future = readTx.read(LogicalDatastoreType.OPERATIONAL, instanceIdentifier);
        }

        try {
            Futures.transform(Futures.catchingAsync(future, Throwable.class, Futures::immediateFailedFuture,
                MoreExecutors.directExecutor()), flowCapNodeOpt -> {
                    // we have to read actual tables with all information before we set empty Flow list,
                    // merge is expensive and not applicable for lists
                    if (flowCapNodeOpt != null && flowCapNodeOpt.isPresent()) {
                        for (var tableData : flowCapNodeOpt.orElseThrow().nonnullTable().values()) {
                            txFacade.writeToTransaction(LogicalDatastoreType.OPERATIONAL,
                                instanceIdentifier.toBuilder().child(Table.class, tableData.key()).build(),
                                new TableBuilder(tableData).setFlow(Map.of()).build());
                        }
                    }
                    return null;
                }, MoreExecutors.directExecutor()).get();
        } catch (InterruptedException | ExecutionException ex) {
            LOG.debug("Failed to delete {} flows", deviceFlowRegistry.size(), ex);
        }
    }

    public static void deleteAllKnownMeters(final TxFacade txFacade,
                                            final DataObjectIdentifier<FlowCapableNode> instanceIdentifier,
                                            final DeviceMeterRegistry meterRegistry) {
        meterRegistry.forEach(meterId -> {
            txFacade.addDeleteToTxChain(LogicalDatastoreType.OPERATIONAL,
                instanceIdentifier.toBuilder().child(Meter.class, new MeterKey(meterId)).build());
            meterRegistry.addMark(meterId);
        });
    }

    public static void deleteAllKnownGroups(final TxFacade txFacade,
                                            final DataObjectIdentifier<FlowCapableNode> instanceIdentifier,
                                            final DeviceGroupRegistry groupRegistry) {
        LOG.debug("deleteAllKnownGroups on device targetType {}", instanceIdentifier.lastStep().type());
        groupRegistry.forEach(groupId -> {
            txFacade.addDeleteToTxChain(LogicalDatastoreType.OPERATIONAL,
                instanceIdentifier.toBuilder().child(Group.class, new GroupKey(groupId)).build());
            groupRegistry.addMark(groupId);
        });
    }

    /**
     * Writes snapshot gathering start timestamp + cleans end mark.
     *
     * @param deviceInfo device info
     * @param txFacade tx manager
     */
    static void markDeviceStateSnapshotStart(final DeviceInfo deviceInfo, final TxFacade txFacade) {
        final var statusPath = deviceInfo.getNodeInstanceIdentifier().toBuilder()
            .augmentation(FlowCapableStatisticsGatheringStatus.class)
            .build();

        final FlowCapableStatisticsGatheringStatus gatheringStatus = new FlowCapableStatisticsGatheringStatusBuilder()
                .setSnapshotGatheringStatusStart(new SnapshotGatheringStatusStartBuilder().setBegin(now()).build())
                .setSnapshotGatheringStatusEnd(null) // TODO: reconsider if really need to clean end mark here
                .build();
        try {
            txFacade.writeToTransaction(LogicalDatastoreType.OPERATIONAL, statusPath, gatheringStatus);
        } catch (final TransactionChainClosedException e) {
            LOG.warn("Can't write to transaction, transaction chain probably closed.");
            LOG.trace("Write to transaction exception: ", e);
        }

        txFacade.submitTransaction();
    }

    /**
     * Writes snapshot gathering end timestamp + outcome.
     *
     * @param deviceInfo device info
     * @param txFacade tx manager
     * @param succeeded     outcome of currently finished gathering
     */
    static void markDeviceStateSnapshotEnd(final DeviceInfo deviceInfo,
                                           final TxFacade txFacade, final boolean succeeded) {
        final var statusEndPath = deviceInfo.getNodeInstanceIdentifier().toBuilder()
            .augmentation(FlowCapableStatisticsGatheringStatus.class)
            .child(SnapshotGatheringStatusEnd.class)
            .build();

        final SnapshotGatheringStatusEnd gatheringStatus = new SnapshotGatheringStatusEndBuilder()
                .setEnd(now())
                .setSucceeded(succeeded)
                .build();
        try {
            txFacade.writeToTransaction(LogicalDatastoreType.OPERATIONAL, statusEndPath, gatheringStatus);
        } catch (TransactionChainClosedException e) {
            LOG.warn("Can't write to transaction, transaction chain probably closed.");
            LOG.trace("Write to transaction exception: ", e);
        }

        txFacade.submitTransaction();
    }

    private static DateAndTime now() {
        // FIXME: modernize with Java 8 time utilities
        return new DateAndTime(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(new Date()));
    }
}
