/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.onf;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev161130.BundlePropertyType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev161130.bundle.common.grouping.BundleProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev161130.bundle.common.grouping.BundlePropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev161130.bundle.property.grouping.bundle.property.entry.BundlePropertyExperimenterBuilder;

/**
 * Abstract class for common Converter util methods.
 */
public abstract class ConverterTestUtils {

    public static BundleProperty createExperimenterProperty() {
        final BundlePropertyBuilder propertyBuilder = new BundlePropertyBuilder();
        propertyBuilder.setType(BundlePropertyType.ONFETBPTEXPERIMENTER);
        propertyBuilder.setBundlePropertyEntry(new BundlePropertyExperimenterBuilder()
                .setExperimenter(new ExperimenterId(1L))
                .setExpType(1L)
                .setBundlePropertyExperimenterData(null)
                .build());
        return propertyBuilder.build();
    }

}
