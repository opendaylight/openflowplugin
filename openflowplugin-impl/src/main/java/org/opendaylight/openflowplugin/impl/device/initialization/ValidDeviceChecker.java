/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.device.initialization;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.tuple.Pair;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceValidator;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.multipart.reply.multipart.reply.body.MultipartReplyDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.OpenflowProviderConfig;

public final class ValidDeviceChecker {

    private final List<DeviceValidator> validators = new ArrayList<>();
    private final OpenflowProviderConfig configuration;

    private ValidDeviceChecker() {
        throw new IllegalArgumentException("Empty constructor not allowed here");
    }

    ValidDeviceChecker(@Nonnull OpenflowProviderConfig configuration) {

        this.configuration = configuration;
        
        //can create own device validators here and register it
        validators.add(new OVSDeviceValidator());

    }

    Map<Class<? extends DeviceValidator>, Pair<Boolean, String>> checkDevice(@Nonnull MultipartReplyDesc description) {
        Map<Class<? extends DeviceValidator>, Pair<Boolean, String>> results = new LinkedHashMap<>();
        for (DeviceValidator validator : validators) {
            Pair<Boolean, String> result = validator.valid(description, configuration);
            results.put(validator.getClass(), result);
        }
        return results;
    }

    public OpenflowProviderConfig getConfiguration() {
        return configuration;
    }
}
