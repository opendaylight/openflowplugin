/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.driver;

import static org.opendaylight.util.junit.TestTools.*;

import org.opendaylight.util.net.IpAddress;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This JUnit test class tests the DeviceInfoFactory class.
 *
 * @author Simon Hunt
 */
public class DeviceDriverFactoryTest extends MockDeviceDriverTestUtils {

    private DeviceDriverFactory factory;

    @BeforeClass
    public static void classSetUp() {
    }

    @AfterClass
    public static void classTearDown() {
    }

    @Before
    public void setUp() {
        factory = new DeviceDriverFactory();
    }

    @After
    public void tearDown() {
    }

    private static final IpAddress TARGET = IpAddress.valueOf("1.2.3.4");

    // == TESTS GO HERE ==
    @Test
    public void basic() {
        print(EOL + "basic()");
        print(factory);
        assertTrue("assertion", true);
    }


    @Test
    public void addSingleProvider() {
        // when a provider is added to the factory, it is queried for the names of the device types it
        // supports, and those are used as keys in the central registry
        DeviceDriverProvider mockProvider = createProvider(true, TYPE_A, TYPE_B);

        // do the test steps
        assertEquals(AM_UXS, 0, factory.getRegistrySize());

        factory.addProvider(mockProvider);
        assertEquals(AM_UXS, 2, factory.getRegistrySize());

        assertSame(AM_NSR,  mockProvider, factory.getProvider(TYPE_A));
        assertSame(AM_NSR,  mockProvider, factory.getProvider(TYPE_B));
    }

    @Test
    public void addTwoProviders() {
        DeviceDriverProvider mock1 = createProvider(true, TYPE_A, TYPE_B);
        DeviceDriverProvider mock2 = createProvider(true, TYPE_C, TYPE_D);

        assertEquals(AM_UXS, 0, factory.getRegistrySize());

        factory.addProvider(mock1);
        assertEquals(AM_UXS, 2, factory.getRegistrySize());
        factory.addProvider(mock2);
        assertEquals(AM_UXS, 4, factory.getRegistrySize());

        assertSame(AM_NSR,  mock1, factory.getProvider(TYPE_A));
        assertSame(AM_NSR,  mock1, factory.getProvider(TYPE_B));
        assertSame(AM_NSR,  mock2, factory.getProvider(TYPE_C));
        assertSame(AM_NSR,  mock2, factory.getProvider(TYPE_D));
    }

    @Test
    public void addTwoProvidersTypeClash() {
        DeviceDriverProvider mock1 = createProvider(true, TYPE_A, TYPE_B);
        DeviceDriverProvider mock2 = createProvider(true, TYPE_C, TYPE_D, TYPE_A);

        assertEquals(AM_UXS, 0, factory.getRegistrySize());

        factory.addProvider(mock1);
        assertEquals(AM_UXS, 2, factory.getRegistrySize());
        try {
            factory.addProvider(mock2);
            fail(AM_NOEX);
        } catch (IllegalStateException e) {
            assertTrue(WRMSG, e.getMessage().startsWith("provider already exists"));
        }
        assertEquals(AM_UXS, 2, factory.getRegistrySize());

        assertSame(AM_NSR,  mock1, factory.getProvider(TYPE_A));
        assertSame(AM_NSR,  mock1, factory.getProvider(TYPE_B));
        // C and D should NOT have been added
        assertNull(AM_NSR,  factory.getProvider(TYPE_C));
        assertNull(AM_NSR,  factory.getProvider(TYPE_D));
    }

    @Test
    public void removeProvider() {
        DeviceDriverProvider mock1 = createProvider(true, TYPE_A, TYPE_B);
        DeviceDriverProvider mock2 = createProvider(true, TYPE_C, TYPE_D);

        assertEquals(AM_UXS, 0, factory.getRegistrySize());

        factory.addProvider(mock1);
        assertEquals(AM_UXS, 2, factory.getRegistrySize());
        factory.addProvider(mock2);
        assertEquals(AM_UXS, 4, factory.getRegistrySize());

        assertSame(AM_NSR,  mock1, factory.getProvider(TYPE_A));
        assertSame(AM_NSR,  mock1, factory.getProvider(TYPE_B));
        assertSame(AM_NSR,  mock2, factory.getProvider(TYPE_C));
        assertSame(AM_NSR,  mock2, factory.getProvider(TYPE_D));

        // now for the remove
        factory.removeProvider(mock1);
        assertEquals(AM_UXS, 2, factory.getRegistrySize());

        // A and B have gone
        assertNull(AM_NSR,  factory.getProvider(TYPE_A));
        assertNull(AM_NSR,  factory.getProvider(TYPE_B));
        // but C and D remain
        assertSame(AM_NSR,  mock2, factory.getProvider(TYPE_C));
        assertSame(AM_NSR,  mock2, factory.getProvider(TYPE_D));
    }

    @Test
    public void removeProviderExceptions() {
        DeviceDriverProvider mock1 = createProvider(true, TYPE_A, TYPE_B);
        factory.addProvider(mock1);
        assertEquals(AM_UXS, 2, factory.getRegistrySize());

        DeviceDriverProvider mockBlackHat = createProvider(true, TYPE_B);
        try {
            factory.removeProvider(mockBlackHat);
            fail(AM_NOEX);
        } catch (IllegalStateException e) {
            assertTrue(WRMSG, e.getMessage().contains("remove other provider reference"));
        }

        mockBlackHat = createProvider(true, TYPE_C);
        try {
            factory.removeProvider(mockBlackHat);
            fail(AM_NOEX);
        } catch (IllegalStateException e) {
            assertTrue(WRMSG, e.getMessage().contains("remove provider for non-supported type"));
        }
    }


    @Test
    public void getTypeNames() {
        DeviceDriverProvider mock1 = createProvider(true, TYPE_A, TYPE_B);
        DeviceDriverProvider mock2 = createProvider(true, TYPE_C, TYPE_D);

        factory.addProvider(mock1);
        factory.addProvider(mock2);

        assertEquals("incorrect all device type names",
                     createSet(TYPE_A, TYPE_B, TYPE_C, TYPE_D), factory.getDeviceTypeNames());
    }


    // === now for some delegated creation methods


    private static final String UTRI = "Unsupported Type returns Impl";

    @Test
    public void createDeviceInfo() {
        DeviceInfo mockInfo = createDeviceInfo(createDeviceType(TYPE_A));

        DeviceDriverProvider mock = createProvider(false, TYPE_A, TYPE_B);
        expect(mock.create(TYPE_A)).andReturn(mockInfo);
        replay(mock);

        // before registration
        DeviceInfo info = factory.create(null);
        assertNull(UTRI, info);
        info = factory.create(TYPE_A);
        assertNull(UTRI, info);

        factory.addProvider(mock);

        // after registration
        info = factory.create(TYPE_A);
        assertSame(AM_NSR, mockInfo, info);
        assertEquals(AM_NEQ, TYPE_A, info.getTypeName());
    }


    @Test
    public void createHandlerWithTypeName() {
        DeviceInfo mockInfo = createDeviceInfo(createDeviceType(TYPE_B));
        DeviceHandler mockHandler = createDeviceHandler(mockInfo, TARGET);

        DeviceDriverProvider mock = createProvider(false, TYPE_B, TYPE_C);
        expect(mock.create(TYPE_B, TARGET)).andReturn(mockHandler);
        replay(mock);

        // before registration
        DeviceHandler handler = factory.create(TYPE_B, TARGET);
        assertNull(UTRI, handler);

        factory.addProvider(mock);

        // after registration
        handler = factory.create(TYPE_B, TARGET);
        assertSame(AM_NSR, mockHandler, handler);
        assertSame(AM_NSR, TARGET, handler.getIpAddress());
        assertSame(AM_NSR, mockInfo, handler.getDeviceInfo());
    }

    @Test
    public void createHandlerWithInfo() {
        DeviceInfo mockInfo = createDeviceInfo(createDeviceType(TYPE_C));
        DeviceHandler mockHandler = createDeviceHandler(mockInfo, TARGET);

        DeviceDriverProvider mock = createProvider(false, TYPE_C, TYPE_D);
        expect(mock.create(mockInfo, TARGET)).andReturn(mockHandler);
        replay(mock);

        // before registration
        DeviceHandler handler = factory.create(mockInfo, TARGET);
        assertNull(UTRI, handler);

        factory.addProvider(mock);

        // after registration
        handler = factory.create(mockInfo, TARGET);
        assertSame(AM_NSR, mockHandler, handler);
        assertSame(AM_NSR, TARGET, handler.getIpAddress());
        assertSame(AM_NSR, mockInfo, handler.getDeviceInfo());
    }

    @Test
    public void createLoaderWithTypeName() {
        DeviceInfo mockInfo = createDeviceInfo(createDeviceType(TYPE_D));
        DeviceLoader mockLoader = createDeviceLoader(mockInfo, UID);

        DeviceDriverProvider mock = createProvider(false, TYPE_D);
        expect(mock.create(TYPE_D, UID)).andReturn(mockLoader);
        replay(mock);

        // before registration
        DeviceLoader loader = factory.create(TYPE_D, UID);
        assertNull(UTRI, loader);

        factory.addProvider(mock);

        // after registration
        loader = factory.create(TYPE_D, UID);
        assertSame(AM_NSR, mockLoader, loader);
        assertSame(AM_NSR, mockInfo, loader.getDeviceInfo());
        assertSame(AM_NSR, UID, loader.getUID());
    }

    @Test
    public void createLoaderWithInfo() {
        DeviceInfo mockInfo = createDeviceInfo(createDeviceType(TYPE_D));
        DeviceLoader mockLoader = createDeviceLoader(mockInfo, UID);

        DeviceDriverProvider mock = createProvider(false, TYPE_D);
        expect(mock.create(mockInfo, UID)).andReturn(mockLoader);
        replay(mock);

        // before registration
        DeviceLoader loader = factory.create(mockInfo, UID);
        assertNull(UTRI, loader);

        factory.addProvider(mock);

        // after registration
        loader = factory.create(mockInfo, UID);
        assertSame(AM_NSR, mockLoader, loader);
        assertSame(AM_NSR, mockInfo, loader.getDeviceInfo());
        assertSame(AM_NSR, UID, loader.getUID());
    }

}

