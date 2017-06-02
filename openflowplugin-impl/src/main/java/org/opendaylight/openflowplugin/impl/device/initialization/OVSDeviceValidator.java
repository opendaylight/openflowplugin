/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.device.initialization;

import com.google.common.base.Splitter;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceValidator;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.multipart.reply.multipart.reply.body.MultipartReplyDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.OpenflowProviderConfig;

/**
 * This class serves as checker for version ovs 2.0.2 or lower
 * Bug 3549 - old ovs 2.0.2 should be rejected if mandatory
 * switch features are configured as true
 */
public class OVSDeviceValidator implements DeviceValidator {

    private static final String OPEN_VSWITCH = "OPEN VSWITCH";
    private static final String NON_VALID_MESSAGE =
            "Configuration of switch-features-mandatory=true and ovs version 2.0.2 or lower is not enabled. ";
    
    private static final Pair<Boolean, String> VALID_RESULT = new ImmutablePair<>(true, "");
    private static final Pair<Boolean, String> NON_VALID_RESULT = new ImmutablePair<>(false, NON_VALID_MESSAGE);

    OVSDeviceValidator() {
    }

    @Override
    public Pair<Boolean, String> valid(@Nonnull MultipartReplyDesc description,
                                       @Nonnull OpenflowProviderConfig configuration) {
        
        //if switch features is set to false any OVS device can connect
        if (!configuration.isSwitchFeaturesMandatory()) {
            return VALID_RESULT;
        }

        //check only if it is an OVS switch
        if (!description.getHardware().toUpperCase().equals(OPEN_VSWITCH)) {
            return VALID_RESULT;
        }

        List<String> softwareVersionSplit = Splitter
                .on(".")
                .omitEmptyStrings()
                .trimResults()
                .splitToList(description.getSoftware());

        //Standard format should be X.X.X otherwise cannot decide
        if (softwareVersionSplit.size() != 3) {
            return VALID_RESULT;
        }

        //If in version isn't number cannot decide
        List<Integer> softwareVersionInteger = new ArrayList<>();
        try {
            softwareVersionSplit.forEach(s -> softwareVersionInteger.add(Integer.parseInt(s)));
        } catch (NumberFormatException ex) {
            //Not able to convert software version to numbers
            return VALID_RESULT;
        }

        int n = 100;
        int version = 0;

        for (Integer i : softwareVersionInteger) {
            version += i * n;
            n /= 10;
        }

        if (version <= 202) {
            return NON_VALID_RESULT;
        }

        return VALID_RESULT;

    }

}
