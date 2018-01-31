/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.api;

import org.opendaylight.openflowplugin.extension.api.exception.ConversionException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.experimenter.types.rev151020.experimenter.core.message.ExperimenterMessageOfChoice;
import org.opendaylight.yangtools.yang.binding.DataContainer;

/**
 * Convert message from MD-SAL model into OFJava-API model.
 *
 * @param <F> input message model - MD-SAL model
 * @param <T> output message model - OFJava-API
 */
public interface ConverterMessageToOFJava<F extends ExperimenterMessageOfChoice, T extends DataContainer> {

    /**
     * Converts a message to MD-SAL model.
     *
     * @param experimenterMessageCase where is vendor's augmentation
     * @return message converted to OFJava-API
     */
    T convert(F experimenterMessageCase) throws ConversionException;

    /**
     * Returns the corresponding experimenter id (vendor id).
     */
    ExperimenterId getExperimenterId();

    /**
     * Returns the corresponding experimenter message type.
     */
    long getType();
}
