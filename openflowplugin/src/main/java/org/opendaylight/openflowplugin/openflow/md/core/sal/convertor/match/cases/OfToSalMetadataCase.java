/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases;

import java.math.BigInteger;
import java.util.Optional;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.data.MatchResponseConvertorData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.MetadataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.MetadataCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.metadata._case.Metadata;
import org.opendaylight.yangtools.yang.common.Uint64;

public class OfToSalMetadataCase extends ConvertorCase<MetadataCase, MatchBuilder, MatchResponseConvertorData> {
    public OfToSalMetadataCase() {
        super(MetadataCase.class, true, OFConstants.OFP_VERSION_1_0, OFConstants.OFP_VERSION_1_3);
    }

    @Override
    public Optional<MatchBuilder> process(final MetadataCase source, final MatchResponseConvertorData data,
            final ConvertorExecutor convertorExecutor) {
        final MatchBuilder matchBuilder = data.getMatchBuilder();
        final MetadataBuilder metadataBuilder = new MetadataBuilder();
        final Metadata metadata = source.getMetadata();

        if (metadata != null) {
            metadataBuilder.setMetadata(
                Uint64.valueOf(new BigInteger(OFConstants.SIGNUM_UNSIGNED, metadata.getMetadata())));
            byte[] metadataMask = metadata.getMask();

            if (metadataMask != null) {
                metadataBuilder.setMetadataMask(
                    Uint64.valueOf(new BigInteger(OFConstants.SIGNUM_UNSIGNED, metadataMask)));
            }

            matchBuilder.setMetadata(metadataBuilder.build());
        }

        return Optional.of(matchBuilder);
    }
}
