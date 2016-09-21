/*
 * Copyright (c) 2016 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.api;

import org.opendaylight.openflowplugin.extension.api.exception.ConversionException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.BandType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.meter.band.experimenter._case.MeterBandExperimenterBuilder;

/**
 * convert message from MD-SAL model into OFJava-API model
 *
 * @param <IN> input model - MD-SAL model
 * @param <INOUT> input/output model - OFJava-API
 */
public interface ConvertorMeterBandTypeToOFJava<IN extends BandType, INOUT extends MeterBandExperimenterBuilder> {

    void convert(IN value, INOUT meterBandExperimenterBuilder) throws ConversionException;
}
