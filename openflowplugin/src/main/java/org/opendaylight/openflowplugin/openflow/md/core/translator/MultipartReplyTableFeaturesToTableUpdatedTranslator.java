/**
 * Copyright (c) 2014 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.translator;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import org.opendaylight.openflowplugin.api.openflow.md.core.IMDMessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyTableFeaturesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table.features._case.MultipartReplyTableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.TableUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultipartReplyTableFeaturesToTableUpdatedTranslator implements
        IMDMessageTranslator<OfHeader, List<DataObject>> {

    private static final Logger LOG = LoggerFactory
            .getLogger(MultipartReplyTableFeaturesToTableUpdatedTranslator.class);
    private final ConvertorExecutor convertorExecutor;

    public MultipartReplyTableFeaturesToTableUpdatedTranslator(ConvertorExecutor convertorExecutor) {
        this.convertorExecutor = convertorExecutor;
    }

    @Override
    public List<DataObject> translate(SwitchConnectionDistinguisher cookie, SessionContext sc, OfHeader msg) {
        if (msg instanceof MultipartReply && ((MultipartReply) msg).getType() == MultipartType.OFPMPTABLEFEATURES) {
            LOG.debug("MultipartReply Being translated to TableUpdated ");
            MultipartReplyMessage mpReply = (MultipartReplyMessage) msg;

            List<DataObject> listDataObject = new CopyOnWriteArrayList<>();

            TableUpdatedBuilder message = new TableUpdatedBuilder();
            message.setNode((new NodeRef(InventoryDataServiceUtil.identifierFromDatapathId(sc.getFeatures()
                    .getDatapathId()))));
            message.setMoreReplies(mpReply.getFlags().isOFPMPFREQMORE());
            message.setTransactionId(new TransactionId(BigInteger.valueOf(mpReply.getXid())));
            MultipartReplyTableFeaturesCase caseBody = (MultipartReplyTableFeaturesCase) mpReply.getMultipartReplyBody();
            MultipartReplyTableFeatures body = caseBody.getMultipartReplyTableFeatures();

            final VersionConvertorData data = new VersionConvertorData(sc.getPrimaryConductor().getVersion());
            final Optional<List<TableFeatures>> tableFeaturesList = convertorExecutor.convert(body, data);
            message.setTableFeatures(tableFeaturesList.orElse(Collections.emptyList()));
            listDataObject.add( message.build()) ;

            return listDataObject ;

        }
        return Collections.emptyList();

    }

}