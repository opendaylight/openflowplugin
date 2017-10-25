/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.onf.converter;

import org.opendaylight.openflowplugin.extension.api.ConvertorMessageFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConverterMessageToOFJava;
import org.opendaylight.openflowplugin.extension.api.path.MessagePath;
import org.opendaylight.openflowplugin.extension.onf.OnfConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.send.experimenter.input.experimenter.message.of.choice.BundleControlSal;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.send.experimenter.input.experimenter.message.of.choice.BundleControlSalBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.send.experimenter.input.experimenter.message.of.choice.bundle.control.sal.SalControlDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.experimenter.input.experimenter.data.of.choice.BundleControlOnf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.experimenter.input.experimenter.data.of.choice.BundleControlOnfBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.experimenter.input.experimenter.data.of.choice.bundle.control.onf.OnfControlGroupingDataBuilder;

/**
 * Converter for BundleControl messages (ONF approved extension #230).
 */
public class BundleControlConverter implements
        ConverterMessageToOFJava<BundleControlSal, BundleControlOnf>,
        ConvertorMessageFromOFJava<BundleControlOnf, MessagePath> {

    @Override
    public BundleControlOnf
            convert(final BundleControlSal experimenterMessageCase) {
        return new BundleControlOnfBuilder()
                .setOnfControlGroupingData(new OnfControlGroupingDataBuilder(experimenterMessageCase.getSalControlData()).build())
                .build();
    }

    @Override
    public BundleControlSal convert(BundleControlOnf input, MessagePath path) {
        return new BundleControlSalBuilder()
                .setSalControlData(
                        new SalControlDataBuilder(input.getOnfControlGroupingData()).build()
                )
                .build();
    }

    @Override
    public ExperimenterId getExperimenterId() {
        return new ExperimenterId(OnfConstants.ONF_EXPERIMENTER_ID);
    }

    @Override
    public long getType() {
        return OnfConstants.ONF_ET_BUNDLE_CONTROL;
    }
}
