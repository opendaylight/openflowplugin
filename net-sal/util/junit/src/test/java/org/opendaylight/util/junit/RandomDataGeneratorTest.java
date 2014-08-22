/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.junit;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link org.opendaylight.util.junit.RandomDataGenerator}
 * 
 * @author Fabiel Zuniga
 */
public class RandomDataGeneratorTest {

    private RandomDataGenerator randomDataGenerator;

    /**
     * 
     */
    @Before
    public void setup() {
        this.randomDataGenerator = new RandomDataGenerator();
    }

    // TODO: Include JMockit and uncomment code

    /**
     * @param randomMock random mock.
     */
    /*
    @Test
    public void testGetLong(final @Mocked Random randomMock) {
        new Expectations() {

            {
                randomMock.nextLong();
                returns(Long.valueOf(0));
            }
        };

        Assert.assertEquals(0, this.randomDataGenerator.getLong());
    }
    */

    /**
     * @param randomMock random mock.
     */
    /*
    @Test
    public void testGetInt(final @Mocked Random randomMock) {
        new Expectations() {

            {
                randomMock.nextInt();
                returns(Integer.valueOf(0));
            }
        };

        Assert.assertEquals(0, this.randomDataGenerator.getInt());
    }
    */

    /**
     * @param randomMock random mock.
     */
    /*
    @Test
    public void testGetPositiveInt(final @Mocked Random randomMock) {
        new Expectations() {

            {
                randomMock.nextInt(Integer.MAX_VALUE);
                returns(Integer.valueOf(0));
            }
        };

        Assert.assertEquals(0, this.randomDataGenerator.getPositiveInt());
    }
    */

    /**
     * @param randomMock random mock.
     */
    /*
    @Test
    public void testGetBoolean(final @Mocked Random randomMock) {
        new Expectations() {

            {
                randomMock.nextBoolean();
                returns(Boolean.TRUE);
            }
        };

        Assert.assertEquals(true, this.randomDataGenerator.getBoolean());
    }
    */

    /**
     * 
     */
    @Test
    public void testGetEnum() {
        Assert.assertNotNull(this.randomDataGenerator.getEnum(EnumTest.class));
    }

    private static enum EnumTest {
        ELEMENT_1, ELEMENT_2, ELEMENT_3
    }
}
