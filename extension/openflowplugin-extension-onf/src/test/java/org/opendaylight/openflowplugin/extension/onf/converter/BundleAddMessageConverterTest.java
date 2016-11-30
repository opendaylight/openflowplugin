/**
 Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.

 This program and the accompanying materials are made available under the
 terms of the Eclipse Public License v1.0 which accompanies this distribution,
 and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.onf.converter;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;

/**
 * Test for {@link org.opendaylight.openflowplugin.extension.onf.converter.BundleAddMessageConverter}.
 */
public class BundleAddMessageConverterTest {

    private final BundleAddMessageConverter converter = new BundleAddMessageConverter();

    @Test
    public void testGetExperimenterId() {
        Assert.assertEquals("Wrong ExperimenterId.", new ExperimenterId(0x4F4E4600L), converter.getExperimenterId());
    }

    @Test
    public void testGetType() {
        Assert.assertEquals("Wrong type.", 2301, converter.getType());
    }

    @Test
    public void convert() {
        // TODO
    }

}