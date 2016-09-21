/*
 * Copyright (c) 2016 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.api;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterMeterBandSubType;

public class ExperimenterIdMeterBandKeyTest {
    /**
     * Tests {@link ExperimenterIdMeterBandKey#equals(Object)} and {@link ExperimenterIdMeterBandKey#hashCode()}
     */
    @Test
    public void test() {
        ExperimenterIdMeterBandKey<? extends ExperimenterMeterBandSubType> key1 =
                new ExperimenterIdMeterBandKey<>(ExperimenterMeterBandSubType.class, EncodeConstants.OF13_VERSION_ID, 42L);
        ExperimenterIdMeterBandKey<? extends ExperimenterMeterBandSubType> key2 =
                new ExperimenterIdMeterBandKey<>(ExperimenterMeterBandSubType.class, EncodeConstants.OF13_VERSION_ID, 42L);
        Assert.assertTrue("Wrong equals()", key1.equals(key2));
        Assert.assertEquals("Wrong hashCode()", key1.hashCode(), key2.hashCode());

        key2 = new ExperimenterIdMeterBandKey<>(null, EncodeConstants.OF13_VERSION_ID, 42L);
        Assert.assertFalse("Wrong equals()", key1.equals(key2));
        Assert.assertFalse("Wrong hashCode()", key1.hashCode() == key2.hashCode());

        key2 = new ExperimenterIdMeterBandKey<>(ExperimenterMeterBandSubType.class, EncodeConstants.OF10_VERSION_ID, 42L);
        Assert.assertFalse("Wrong equals()", key1.equals(key2));
        Assert.assertFalse("Wrong hashCode()", key1.hashCode() == key2.hashCode());

        key2 = new ExperimenterIdMeterBandKey<>(ExperimenterMeterBandSubType.class, EncodeConstants.OF13_VERSION_ID, 43L);
        Assert.assertFalse("Wrong equals()", key1.equals(key2));
        Assert.assertFalse("Wrong hashCode()", key1.hashCode() == key2.hashCode());
    }
}
