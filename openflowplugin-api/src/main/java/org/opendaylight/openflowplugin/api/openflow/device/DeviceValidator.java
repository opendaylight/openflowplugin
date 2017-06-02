/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.device;

import javax.annotation.Nonnull;
import org.apache.commons.lang3.tuple.Pair;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.multipart.reply.multipart.reply.body.MultipartReplyDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.OpenflowProviderConfig;

public interface DeviceValidator {

    /**
     * Check if device is valid when connection behalf on actual configuration
     * @param description device description
     * @param configuration plugin configuration
     * @return pair where key is false if device is not valid to connect and error message
     */
    Pair<Boolean, String> valid(@Nonnull MultipartReplyDesc description,
                                @Nonnull OpenflowProviderConfig configuration);
    
}
