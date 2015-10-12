/**
 * Copyright (c) 2013, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.translator;

import java.math.BigInteger;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.errors.rev131116.ErrorType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.BadActionErrorNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.BadInstructionErrorNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.BadMatchErrorNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.BadRequestErrorNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.ExperimenterErrorNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.FlowModErrorNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.GroupModErrorNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.HelloFailedErrorNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.MeterModErrorNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.PortModErrorNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.QueueOpErrorNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.RoleRequestErrorNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.SwitchConfigErrorNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.TableFeaturesErrorNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.TableModErrorNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage;

/**
 * OF-1.3 errorMessage support
 */
public class ErrorTranslator extends AbstractErrorTranslator {

    @Override
    public ErrorType decodeErrorType(int type) {
        return ErrorType.forValue(type);
    }

    @Override
    public org.opendaylight.yang.gen.v1.urn.opendaylight.flow.errors.rev131116.ErrorMessage getGranularNodeErrors(ErrorMessage message, ErrorType errorType, NodeRef node){
        org.opendaylight.yang.gen.v1.urn.opendaylight.flow.errors.rev131116.ErrorMessage outErrorMessage = null;
        TransactionId txnId = new TransactionId(BigInteger.valueOf(message.getXid()));

        //currently in yang , generation of builders does not support an interface
        //so each notification is put in a separate if-else
        if (errorType == ErrorType.HelloFailed) {
            HelloFailedErrorNotificationBuilder builder = new HelloFailedErrorNotificationBuilder();
            builder.setTransactionId(txnId);
            builder.setType(errorType);
            builder.setCode(message.getCode());
            builder.setNode(node);
            if (message.getData() != null) {
                builder.setData(new String(message.getData()));
            }
            outErrorMessage = builder.build();
        } else if (errorType == ErrorType.BadRequest) {
            BadRequestErrorNotificationBuilder builder = new BadRequestErrorNotificationBuilder ();
            builder.setTransactionId(txnId);
            builder.setType(errorType);
            builder.setCode(message.getCode());
            builder.setNode(node);
            if (message.getData() != null) {
                builder.setData(new String(message.getData()));
            }
            outErrorMessage = builder.build();
        } else if (errorType == ErrorType.BadAction) {
            BadActionErrorNotificationBuilder builder = new BadActionErrorNotificationBuilder();
            builder.setTransactionId(txnId);
            builder.setType(errorType);
            builder.setCode(message.getCode());
            builder.setNode(node);
            if (message.getData() != null) {
                builder.setData(new String(message.getData()));
            }
            outErrorMessage = builder.build();
        } else if (errorType == ErrorType.BadInstruction) {
            BadInstructionErrorNotificationBuilder builder = new BadInstructionErrorNotificationBuilder();
            builder.setTransactionId(txnId);
            builder.setType(errorType);
            builder.setCode(message.getCode());
            builder.setNode(node);
            if (message.getData() != null) {
                builder.setData(new String(message.getData()));
            }
            outErrorMessage = builder.build();
        } else if (errorType == ErrorType.BadMatch) {
            BadMatchErrorNotificationBuilder builder = new BadMatchErrorNotificationBuilder();
            builder.setTransactionId(txnId);
            builder.setType(errorType);
            builder.setCode(message.getCode());
            builder.setNode(node);
            if (message.getData() != null) {
                builder.setData(new String(message.getData()));
            }
            outErrorMessage = builder.build();
        } else if (errorType == ErrorType.FlowModFailed) {
            FlowModErrorNotificationBuilder builder = new FlowModErrorNotificationBuilder();
            builder.setTransactionId(txnId);
            builder.setType(errorType);
            builder.setCode(message.getCode());
            builder.setNode(node);
            if (message.getData() != null) {
                builder.setData(new String(message.getData()));
            }
            outErrorMessage = builder.build();
        } else if (errorType == ErrorType.GroupModFailed) {
            GroupModErrorNotificationBuilder builder = new GroupModErrorNotificationBuilder();
            builder.setTransactionId(txnId);
            builder.setType(errorType);
            builder.setCode(message.getCode());
            builder.setNode(node);
            if (message.getData() != null) {
                builder.setData(new String(message.getData()));
            }
            outErrorMessage = builder.build();
        } else if (errorType == ErrorType.PortModFailed) {
            PortModErrorNotificationBuilder builder = new PortModErrorNotificationBuilder();
            builder.setTransactionId(txnId);
            builder.setType(errorType);
            builder.setCode(message.getCode());
            builder.setNode(node);
            if (message.getData() != null) {
                builder.setData(new String(message.getData()));
            }
            outErrorMessage = builder.build();
        } else if (errorType == ErrorType.QueueOpFailed) {
            QueueOpErrorNotificationBuilder builder = new QueueOpErrorNotificationBuilder();
            builder.setTransactionId(txnId);
            builder.setType(errorType);
            builder.setCode(message.getCode());
            builder.setNode(node);
            if (message.getData() != null) {
                builder.setData(new String(message.getData()));
            }
            outErrorMessage = builder.build();
        } else if (errorType == ErrorType.SwitchConfigFailed) {
            SwitchConfigErrorNotificationBuilder builder = new SwitchConfigErrorNotificationBuilder();
            builder.setTransactionId(txnId);
            builder.setType(errorType);
            builder.setCode(message.getCode());
            builder.setNode(node);
            if (message.getData() != null) {
                builder.setData(new String(message.getData()));
            }
            outErrorMessage = builder.build();
        } else if (errorType == ErrorType.RoleRequestFailed) {
            RoleRequestErrorNotificationBuilder builder = new RoleRequestErrorNotificationBuilder();
            builder.setTransactionId(txnId);
            builder.setType(errorType);
            builder.setCode(message.getCode());
            builder.setNode(node);
            if (message.getData() != null) {
                builder.setData(new String(message.getData()));
            }
            outErrorMessage = builder.build();
        } else if (errorType == ErrorType.MeterModFailed) {
            MeterModErrorNotificationBuilder builder = new MeterModErrorNotificationBuilder();
            builder.setTransactionId(txnId);
            builder.setType(errorType);
            builder.setCode(message.getCode());
            builder.setNode(node);
            if (message.getData() != null) {
                builder.setData(new String(message.getData()));
            }
            outErrorMessage = builder.build();
        } else if (errorType == ErrorType.TableModFailed) {
            TableModErrorNotificationBuilder builder = new TableModErrorNotificationBuilder();
            builder.setTransactionId(txnId);
            builder.setType(errorType);
            builder.setCode(message.getCode());
            builder.setNode(node);
            if (message.getData() != null) {
                builder.setData(new String(message.getData()));
            }
            outErrorMessage = builder.build();
        } else if (errorType == ErrorType.TableFeaturesFailed) {
            TableFeaturesErrorNotificationBuilder builder = new TableFeaturesErrorNotificationBuilder();
            builder.setTransactionId(txnId);
            builder.setType(errorType);
            builder.setCode(message.getCode());
            builder.setNode(node);
            if (message.getData() != null) {
                builder.setData(new String(message.getData()));
            }
            outErrorMessage = builder.build();
        } else if (errorType == ErrorType.Experimenter) {
            ExperimenterErrorNotificationBuilder builder = new ExperimenterErrorNotificationBuilder();
            builder.setTransactionId(txnId);
            builder.setType(errorType);
            builder.setCode(message.getCode());
            builder.setNode(node);
            if (message.getData() != null) {
                builder.setData(new String(message.getData()));
            }
            outErrorMessage = builder.build();
        }

        return outErrorMessage;
    }

}
