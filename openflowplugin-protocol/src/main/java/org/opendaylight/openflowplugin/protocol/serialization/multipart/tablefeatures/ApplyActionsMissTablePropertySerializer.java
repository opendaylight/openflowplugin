/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.protocol.serialization.multipart.tablefeatures;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableFeaturesPropType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplyActionsMiss;

public class ApplyActionsMissTablePropertySerializer extends AbstractActionTablePropertySerializer<ApplyActionsMiss> {
    public ApplyActionsMissTablePropertySerializer(final ExtensionConverterProvider extensionConverterProvider) {
        super(extensionConverterProvider);
    }

    @Override
    protected void serializeProperty(final ApplyActionsMiss property, final ByteBuf byteBuf) {
        writeActions(property.getApplyActionsMiss(), EncodeConstants.OF13_VERSION_ID, byteBuf);
    }

    @Override
    protected TableFeaturesPropType getType() {
        return TableFeaturesPropType.OFPTFPTAPPLYACTIONSMISS;
    }

    @Override
    protected Class<ApplyActionsMiss> getClazz() {
        return ApplyActionsMiss.class;
    }
}
