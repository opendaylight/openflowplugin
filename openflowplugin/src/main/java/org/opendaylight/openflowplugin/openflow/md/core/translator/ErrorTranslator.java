/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.translator;

import org.opendaylight.openflowplugin.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.openflow.md.core.session.TransactionKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.errors.rev131116.ErrorType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.NodeErrorNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.NodeFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.node.error.reference.ObjectReference;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.node.error.reference.object.reference.FlowRefBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.node.error.reference.object.reference.GroupRefBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.node.error.reference.object.reference.MeterRefBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev131103.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev131103.TransactionMetadata;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.NodeGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.NodeMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.BadActionErrorNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.BadInstructionErrorNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.BadInstructionErrorNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.BadMatchErrorNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.BadRequestErrorNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.ExperimenterErrorNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.FlowModErrorNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.FlowModErrorNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.GroupModErrorNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.HelloFailedErrorNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.MeterModErrorNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.PortModErrorNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.QueueOpErrorNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.RoleRequestErrorNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.SwitchConfigErrorNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.SwitchConfigErrorNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.node.error.service.rev140410.TableFeaturesErrorNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.DataObject;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ErrorTranslator extends AbstractErrorTranslator {

    @Override
    public ErrorType decodeErrorType(int type) {
        return ErrorType.forValue(type);
    }

    @Override
    public List<DataObject> getGranularNodeErrors(ErrorMessage message, ErrorType errorType, Uri uri, ObjectReference objRef){
        List<DataObject> list = new CopyOnWriteArrayList<DataObject>();
        TransactionId txnId = new TransactionId(BigInteger.valueOf(message.getXid()));

        //currently in yang , generation of builders does not support an interface
        //so each notification is put in a separate if-else
        if (errorType == ErrorType.HelloFailed) {
            HelloFailedErrorNotificationBuilder builder = new HelloFailedErrorNotificationBuilder();
            builder.setTransactionId(new TransactionId(BigInteger.valueOf(message.getXid())));
            builder.setType(errorType);
            builder.setCode(message.getCode());
            if (message.getData() != null) {
                builder.setData(new String(message.getData()));
            }
            builder.setTransactionUri(uri);
            builder.setObjectReference(objRef);
            list.add(builder.build());

        } else if (errorType == ErrorType.BadRequest) {
            BadRequestErrorNotificationBuilder builder = new BadRequestErrorNotificationBuilder ();
            builder.setTransactionId(new TransactionId(BigInteger.valueOf(message.getXid())));
            builder.setType(errorType);
            builder.setCode(message.getCode());
            if (message.getData() != null) {
                builder.setData(new String(message.getData()));
            }
            builder.setTransactionUri(uri);
            builder.setObjectReference(objRef);
            list.add(builder.build());
        } else if (errorType == ErrorType.BadAction) {
            BadActionErrorNotificationBuilder builder = new BadActionErrorNotificationBuilder();
            builder.setTransactionId(new TransactionId(BigInteger.valueOf(message.getXid())));
            builder.setType(errorType);
            builder.setCode(message.getCode());
            if (message.getData() != null) {
                builder.setData(new String(message.getData()));
            }
            builder.setTransactionUri(uri);
            builder.setObjectReference(objRef);
            list.add(builder.build());
        } else if (errorType == ErrorType.BadInstruction) {
            BadInstructionErrorNotificationBuilder builder = new BadInstructionErrorNotificationBuilder();
            builder.setTransactionId(new TransactionId(BigInteger.valueOf(message.getXid())));
            builder.setType(errorType);
            builder.setCode(message.getCode());
            if (message.getData() != null) {
                builder.setData(new String(message.getData()));
            }
            builder.setTransactionUri(uri);
            builder.setObjectReference(objRef);
            list.add(builder.build());

        } else if (errorType == ErrorType.BadMatch) {
            BadMatchErrorNotificationBuilder builder = new BadMatchErrorNotificationBuilder();
            builder.setTransactionId(new TransactionId(BigInteger.valueOf(message.getXid())));
            builder.setType(errorType);
            builder.setCode(message.getCode());
            if (message.getData() != null) {
                builder.setData(new String(message.getData()));
            }
            builder.setTransactionUri(uri);
            builder.setObjectReference(objRef);
            list.add(builder.build());
        } else if (errorType == ErrorType.FlowModFailed) {
            FlowModErrorNotificationBuilder builder = new FlowModErrorNotificationBuilder();
            builder.setTransactionId(new TransactionId(BigInteger.valueOf(message.getXid())));
            builder.setType(errorType);
            builder.setCode(message.getCode());
            if (message.getData() != null) {
                builder.setData(new String(message.getData()));
            }
            builder.setTransactionUri(uri);
            builder.setObjectReference(objRef);
            list.add(builder.build());
        } else if (errorType == ErrorType.GroupModFailed) {
            GroupModErrorNotificationBuilder builder = new GroupModErrorNotificationBuilder();
            builder.setTransactionId(new TransactionId(BigInteger.valueOf(message.getXid())));
            builder.setType(errorType);
            builder.setCode(message.getCode());
            if (message.getData() != null) {
                builder.setData(new String(message.getData()));
            }
            builder.setTransactionUri(uri);
            builder.setObjectReference(objRef);
            list.add(builder.build());
        } else if (errorType == ErrorType.PortModFailed) {
            PortModErrorNotificationBuilder builder = new PortModErrorNotificationBuilder();
            builder.setTransactionId(new TransactionId(BigInteger.valueOf(message.getXid())));
            builder.setType(errorType);
            builder.setCode(message.getCode());
            if (message.getData() != null) {
                builder.setData(new String(message.getData()));
            }
            builder.setTransactionUri(uri);
            builder.setObjectReference(objRef);
            list.add(builder.build());
        } else if (errorType == ErrorType.QueueOpFailed) {
            QueueOpErrorNotificationBuilder builder = new QueueOpErrorNotificationBuilder();
            builder.setTransactionId(new TransactionId(BigInteger.valueOf(message.getXid())));
            builder.setType(errorType);
            builder.setCode(message.getCode());
            if (message.getData() != null) {
                builder.setData(new String(message.getData()));
            }
            builder.setTransactionUri(uri);
            builder.setObjectReference(objRef);
            list.add(builder.build());
        } else if (errorType == ErrorType.SwitchConfigFailed) {
            SwitchConfigErrorNotificationBuilder builder = new SwitchConfigErrorNotificationBuilder();
            builder.setTransactionId(new TransactionId(BigInteger.valueOf(message.getXid())));
            builder.setType(errorType);
            builder.setCode(message.getCode());
            if (message.getData() != null) {
                builder.setData(new String(message.getData()));
            }
            builder.setTransactionUri(uri);
            builder.setObjectReference(objRef);
            list.add(builder.build());
        } else if (errorType == ErrorType.RoleRequestFailed) {
            RoleRequestErrorNotificationBuilder builder = new RoleRequestErrorNotificationBuilder();
            builder.setTransactionId(new TransactionId(BigInteger.valueOf(message.getXid())));
            builder.setType(errorType);
            builder.setCode(message.getCode());
            if (message.getData() != null) {
                builder.setData(new String(message.getData()));
            }
            builder.setTransactionUri(uri);
            builder.setObjectReference(objRef);
            list.add(builder.build());
        } else if (errorType == ErrorType.MeterModFailed) {
            MeterModErrorNotificationBuilder builder = new MeterModErrorNotificationBuilder();
            builder.setTransactionId(new TransactionId(BigInteger.valueOf(message.getXid())));
            builder.setType(errorType);
            builder.setCode(message.getCode());
            if (message.getData() != null) {
                builder.setData(new String(message.getData()));
            }
            builder.setTransactionUri(uri);
            builder.setObjectReference(objRef);
            list.add(builder.build());
        } else if (errorType == ErrorType.TableFeaturesFailed) {
            TableFeaturesErrorNotificationBuilder builder = new TableFeaturesErrorNotificationBuilder();
            builder.setTransactionId(new TransactionId(BigInteger.valueOf(message.getXid())));
            builder.setType(errorType);
            builder.setCode(message.getCode());
            if (message.getData() != null) {
                builder.setData(new String(message.getData()));
            }
            builder.setTransactionUri(uri);
            builder.setObjectReference(objRef);
            list.add(builder.build());
        } else if (errorType == ErrorType.Experimenter) {
            ExperimenterErrorNotificationBuilder builder = new ExperimenterErrorNotificationBuilder();
            builder.setTransactionId(new TransactionId(BigInteger.valueOf(message.getXid())));
            builder.setType(errorType);
            builder.setCode(message.getCode());
            if (message.getData() != null) {
                builder.setData(new String(message.getData()));
            }
            builder.setTransactionUri(uri);
            builder.setObjectReference(objRef);
            list.add(builder.build());
        }
        return list;
    }

}
