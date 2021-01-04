/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.test;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.errors.rev131116.ErrorType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.BadActionErrorNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.BadInstructionErrorNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.BadMatchErrorNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.BadRequestErrorNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.ExperimenterErrorNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.FlowModErrorNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.GroupModErrorNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.HelloFailedErrorNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.MeterModErrorNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.NodeErrorListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.PortModErrorNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.QueueOpErrorNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.RoleRequestErrorNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.SwitchConfigErrorNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.TableFeaturesErrorNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.TableModErrorNotification;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dummy implementation flushing events into log.
 *
 * @author kramesha
 */
public class NodeErrorListenerLoggingImpl implements NodeErrorListener {

    private static final Logger LOG = LoggerFactory.getLogger(NodeErrorListenerLoggingImpl.class);

    @Override
    @Deprecated
    public void onBadActionErrorNotification(final BadActionErrorNotification notification) {
        LOG.error("Error notification {}", toStr(notification.getType(), notification.getCode(),
                notification.getTransactionId().getValue()));
    }

    @Override
    @Deprecated
    public void onBadInstructionErrorNotification(final BadInstructionErrorNotification notification) {
        LOG.error("Error notification {}", toStr(notification.getType(), notification.getCode(),
                notification.getTransactionId().getValue()));
    }

    @Override
    @Deprecated
    public void onBadMatchErrorNotification(final BadMatchErrorNotification notification) {
        LOG.error("Error notification {}", toStr(notification.getType(), notification.getCode(),
                notification.getTransactionId().getValue()));
    }

    @Override
    @Deprecated
    public void onBadRequestErrorNotification(final BadRequestErrorNotification notification) {
        LOG.error("Error notification {}", toStr(notification.getType(), notification.getCode(),
                notification.getTransactionId().getValue()));
    }

    @Override
    @Deprecated
    public void onExperimenterErrorNotification(final ExperimenterErrorNotification notification) {
        LOG.error("Error notification {}", toStr(notification.getType(), notification.getCode(),
                notification.getTransactionId().getValue()));
    }

    @Override
    @Deprecated
    public void onFlowModErrorNotification(final FlowModErrorNotification notification) {
        LOG.error("Error notification {}", toStr(notification.getType(), notification.getCode(),
                notification.getTransactionId().getValue()));
    }

    @Override
    @Deprecated
    public void onGroupModErrorNotification(final GroupModErrorNotification notification) {
        LOG.error("Error notification {}", toStr(notification.getType(), notification.getCode(),
                notification.getTransactionId().getValue()));
    }

    @Override
    @Deprecated
    public void onHelloFailedErrorNotification(final HelloFailedErrorNotification notification) {
        LOG.error("Error notification {}", toStr(notification.getType(), notification.getCode(),
                notification.getTransactionId().getValue()));
    }

    @Override
    @Deprecated
    public void onMeterModErrorNotification(final MeterModErrorNotification notification) {
        LOG.error("Error notification {}", toStr(notification.getType(), notification.getCode(),
                notification.getTransactionId().getValue()));
    }

    @Override
    @Deprecated
    public void onPortModErrorNotification(final PortModErrorNotification notification) {
        LOG.error("Error notification {}", toStr(notification.getType(), notification.getCode(),
                notification.getTransactionId().getValue()));
    }

    @Override
    @Deprecated
    public void onQueueOpErrorNotification(final QueueOpErrorNotification notification) {
        LOG.error("Error notification {}", toStr(notification.getType(), notification.getCode(),
                notification.getTransactionId().getValue()));
    }

    @Override
    @Deprecated
    public void onRoleRequestErrorNotification(final RoleRequestErrorNotification notification) {
        LOG.error("Error notification {}", toStr(notification.getType(), notification.getCode(),
                notification.getTransactionId().getValue()));
    }

    @Override
    @Deprecated
    public void onSwitchConfigErrorNotification(final SwitchConfigErrorNotification notification) {
        LOG.error("Error notification {}", toStr(notification.getType(), notification.getCode(),
                notification.getTransactionId().getValue()));
    }

    @Override
    @Deprecated
    public void onTableFeaturesErrorNotification(final TableFeaturesErrorNotification notification) {
        LOG.error("Error notification {}", toStr(notification.getType(), notification.getCode(),
                notification.getTransactionId().getValue()));
    }

    @Override
    @Deprecated
    public void onTableModErrorNotification(final TableModErrorNotification notification) {
        LOG.error("Error notification {}", toStr(notification.getType(), notification.getCode(),
                notification.getTransactionId().getValue()));
    }

    private static String toStr(final ErrorType type, final Uint16 code, final Uint64 xid) {
        return "[Type=" + type + ", Code=" + code + ", Xid =" + xid + "]";
    }
}
