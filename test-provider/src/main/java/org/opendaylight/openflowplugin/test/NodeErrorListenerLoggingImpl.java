/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.test;

import java.math.BigInteger;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * dummy implementation flushing events into log
 *  @author kramesha
 */
public class NodeErrorListenerLoggingImpl implements NodeErrorListener {

    private static final Logger LOG = LoggerFactory.getLogger(NodeErrorListenerLoggingImpl.class);

    @Override
    public void onBadActionErrorNotification(BadActionErrorNotification notification) {
        LOG.error("Error notification ----" + toStr(notification.getType(), notification.getCode(), notification.getTransactionId().getValue()) );
    }

    @Override
    public void onBadInstructionErrorNotification(BadInstructionErrorNotification notification) {
        LOG.error("Error notification ----" + toStr(notification.getType(), notification.getCode(), notification.getTransactionId().getValue()) );
    }

    @Override
    public void onBadMatchErrorNotification(BadMatchErrorNotification notification) {
        LOG.error("Error notification ----" + toStr(notification.getType(), notification.getCode(), notification.getTransactionId().getValue()) );
    }

    @Override
    public void onBadRequestErrorNotification(BadRequestErrorNotification notification) {
        LOG.error("Error notification ----" + toStr(notification.getType(), notification.getCode(), notification.getTransactionId().getValue()) );
    }

    @Override
    public void onExperimenterErrorNotification(ExperimenterErrorNotification notification) {
        LOG.error("Error notification ----" + toStr(notification.getType(), notification.getCode(), notification.getTransactionId().getValue()) );
    }

    @Override
    public void onFlowModErrorNotification(FlowModErrorNotification notification) {
        LOG.error("Error notification ----" + toStr(notification.getType(), notification.getCode(), notification.getTransactionId().getValue()) );
    }

    @Override
    public void onGroupModErrorNotification(GroupModErrorNotification notification) {
        LOG.error("Error notification ----" + toStr(notification.getType(), notification.getCode(), notification.getTransactionId().getValue()) );
    }

    @Override
    public void onHelloFailedErrorNotification(HelloFailedErrorNotification notification) {
        LOG.error("Error notification ----" + toStr(notification.getType(), notification.getCode(), notification.getTransactionId().getValue()) );
    }

    @Override
    public void onMeterModErrorNotification(MeterModErrorNotification notification) {
        LOG.error("Error notification ----" + toStr(notification.getType(), notification.getCode(), notification.getTransactionId().getValue()) );
    }

    @Override
    public void onPortModErrorNotification(PortModErrorNotification notification) {
        LOG.error("Error notification ----" + toStr(notification.getType(), notification.getCode(), notification.getTransactionId().getValue()) );
    }

    @Override
    public void onQueueOpErrorNotification(QueueOpErrorNotification notification) {
        LOG.error("Error notification ----" + toStr(notification.getType(), notification.getCode(), notification.getTransactionId().getValue()) );
    }

    @Override
    public void onRoleRequestErrorNotification(RoleRequestErrorNotification notification) {
        LOG.error("Error notification ----" + toStr(notification.getType(), notification.getCode(), notification.getTransactionId().getValue()) );
    }

    @Override
    public void onSwitchConfigErrorNotification(SwitchConfigErrorNotification notification) {
        LOG.error("Error notification ----" + toStr(notification.getType(), notification.getCode(), notification.getTransactionId().getValue()) );
    }

    @Override
    public void onTableFeaturesErrorNotification(TableFeaturesErrorNotification notification) {
        LOG.error("Error notification ----" + toStr(notification.getType(), notification.getCode(), notification.getTransactionId().getValue()) );
    }

    @Override
    public void onTableModErrorNotification(TableModErrorNotification notification) {
        LOG.error("Error notification ----" + toStr(notification.getType(), notification.getCode(), notification.getTransactionId().getValue()) );
    }

    private String toStr(ErrorType type, int code, BigInteger xid) {
        return "[Type="+type+", Code="+ code +", Xid ="+xid+"]";
    }
}
