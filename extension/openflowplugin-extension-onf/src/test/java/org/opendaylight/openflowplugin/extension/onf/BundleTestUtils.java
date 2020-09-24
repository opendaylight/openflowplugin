/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.onf;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundlePropertyType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.common.grouping.BundleProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.common.grouping.BundlePropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.property.grouping.bundle.property.entry.BundlePropertyExperimenterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.property.grouping.bundle.property.entry.bundle.property.experimenter.BundlePropertyExperimenterData;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Abstract class for common Converter util methods.
 */
public abstract class BundleTestUtils {

    public static BundleProperty createExperimenterProperty(final BundlePropertyExperimenterData data) {
        return new BundlePropertyBuilder()
                .setType(BundlePropertyType.ONFETBPTEXPERIMENTER)
                .setBundlePropertyEntry(new BundlePropertyExperimenterBuilder()
                        .setExperimenter(new ExperimenterId(Uint32.ONE))
                        .setExpType(Uint32.TWO)
                        .setBundlePropertyExperimenterData(data)
                        .build())
                .build();
    }

}
