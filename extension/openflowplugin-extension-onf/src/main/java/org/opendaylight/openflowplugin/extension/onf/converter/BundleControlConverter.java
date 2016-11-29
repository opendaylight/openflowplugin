/**
  Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.

  This program and the accompanying materials are made available under the
  terms of the Eclipse Public License v1.0 which accompanies this distribution,
  and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.onf.converter;

import org.opendaylight.openflowplugin.extension.api.ConvertorMessageFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorMessageToOFJava;
import org.opendaylight.openflowplugin.extension.api.exception.ConversionException;
import org.opendaylight.openflowplugin.extension.api.path.MessagePath;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.experimenter.types.rev151020.experimenter.core.message.ExperimenterMessageOfChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev161130.experimenter.input.experimenter.data.of.choice.BundleControl;

/**
 * Converter for BundleControl messages (ONF approved extension #230).
 */
public class BundleControlConverter implements ConvertorMessageToOFJava<ExperimenterMessageOfChoice, BundleControl>,
        ConvertorMessageFromOFJava<BundleControl, MessagePath> {

    private static final ExperimenterId ONF_EXP_ID = new ExperimenterId(0x4F4E4600L);
    private static final long BUNDLE_CONTROL_TYPE = 2300;

    @Override
    public BundleControl convert(ExperimenterMessageOfChoice input) throws ConversionException {
        return (BundleControl) input;
    }

    @Override
    public ExperimenterMessageOfChoice convert(BundleControl input, MessagePath path) throws ConversionException {
        // TODO new model
        return null;
    }

    @Override
    public ExperimenterId getExperimenterId() {
        return ONF_EXP_ID;
    }

    @Override
    public long getType() {
        return BUNDLE_CONTROL_TYPE;
    }
}
