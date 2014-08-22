/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.driver;

import static org.opendaylight.util.junit.TestTools.*;
import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * Suite of tests for the abstract device driver provider.
 *
 * @author Simon Hunt
 */
public class AbstractDeviceDriverProviderTest {


    private AbstractDeviceDriverProvider provider;
    private DefaultDeviceType ddt;
    private DefaultDeviceType otherType;
    private DefaultDeviceType childType;
    private DefaultDeviceType grandChildType;
    private DefaultDeviceInfo ddi;

    @Before
    public void setUp() throws Exception {
        provider = new DeviceDriverTestUtils.MyProvider();
        assertEquals(AM_UXS, 0, provider.getBindingsCount());
        assertEquals(AM_UXS, 0, provider.getTypesCount());
        ddt = DeviceDriverTestUtils.createCannedType(provider, DeviceDriverTestUtils.TYPE_FOO);
        provider.addType(ddt);
        assertEquals(AM_UXS, 1, provider.getBindingsCount());
        assertEquals(AM_UXS, 1, provider.getTypesCount());
    }

    private void printProviderMappings() {
        for (String name: provider.getDeviceTypeNames()) {
            DefaultDeviceType type = provider.getBindings().get(name);
            DefaultDeviceType parent = (DefaultDeviceType) type.getParentType();
            print("    " + name + " -> " + type.toShortDebugString()
                    + " parent=" + (parent == null?"null":parent.getTypeName()));
        }
    }
        
    @Test
    public void basics() {
        print(EOL + "basics()");
        print(provider);
        printProviderMappings();

        ddi = (DefaultDeviceInfo) provider.create(DeviceDriverTestUtils.TYPE_FOO);
        print(ddi);
        print(ddi.toDebugString());
        assertNotNull(AM_HUH, ddi);
    }

    @Test
    public void mismatchedProviderInType() {
        print(EOL + "mismatchedProviderInType()");
        verifyCatchMismatchedProvider(DeviceDriverTestUtils.createEmptyType(null, DeviceDriverTestUtils.TYPE_BAR));
        verifyCatchMismatchedProvider(DeviceDriverTestUtils.createEmptyType(new DeviceDriverTestUtils.MyProvider(), DeviceDriverTestUtils.TYPE_BAR));
    }

    private void verifyCatchMismatchedProvider(DefaultDeviceType type) {
        try {
            provider.addType(type);
            fail(AM_NOEX);
        } catch (IllegalArgumentException e) {
            print(e);
            assertTrue(AM_HUH, e.getMessage().contains(AbstractDeviceDriverProvider.EMSG_NOT_THIS_PROVIDER));
        }
    }

    @Test
    public void typeNameCollision() {
        print(EOL + "typeNameCollision()");

        // lets try adding FOO again
        otherType = DeviceDriverTestUtils.createEmptyType(provider, DeviceDriverTestUtils.TYPE_FOO);
        assertNotSame("should not be the same instance of device type", ddt, otherType);
        print(": " + ddt.toShortDebugString());
        print(": " + otherType.toShortDebugString());

        assertEquals(AM_UXS, 1, provider.getBindingsCount());
        try {
            provider.addType(otherType);
            fail(AM_NOEX);
        } catch (IllegalStateException e) {
            print(e);
            assertTrue(AM_HUH, e.getMessage().contains(AbstractDeviceDriverProvider.EMSG_TYPE_ALREADY_BOUND));
        }
        assertEquals(AM_UXS, 1, provider.getBindingsCount());

        ddi = (DefaultDeviceInfo) provider.create(DeviceDriverTestUtils.TYPE_FOO);
        assertSame("wrong type instance attached to info", ddt, ddi.getType());
    }

    @Test
    public void maintainingLineage() {
        print(EOL + "maintainingLineage()");
        assertEquals(AM_UXS, 0, ddt.getChildTypes().size());

        // create child type of ddt (FooType)
        childType = DeviceDriverTestUtils.createEmptyType(provider, ddt, DeviceDriverTestUtils.TYPE_BAR);
        assertEquals("badly parented", ddt, childType.getParentType());
        provider.addType(childType);
        assertEquals(AM_UXS, 2, provider.getBindingsCount());
        assertTrue(AM_UXS, ddt.getChildTypes().contains(childType));

        // let's go further
        grandChildType = DeviceDriverTestUtils.createEmptyType(provider, childType, DeviceDriverTestUtils.TYPE_BAZ);
        assertEquals("badly parented", childType, grandChildType.getParentType());
        provider.addType(grandChildType);
        assertEquals(AM_UXS, 3, provider.getBindingsCount());
        assertTrue(AM_UXS, childType.getChildTypes().contains(grandChildType));
        printProviderMappings();
    }

    @Test
    public void nonRegisteredParent() {
        print(EOL + "nonRegisteredParent()");
        otherType = DeviceDriverTestUtils.createEmptyType(new DeviceDriverTestUtils.MyProvider(), DeviceDriverTestUtils.TYPE_BAR);
        childType = DeviceDriverTestUtils.createEmptyType(provider, otherType, DeviceDriverTestUtils.TYPE_BAZ);
        try {
            provider.addType(childType);
            fail(AM_NOEX);
        } catch (IllegalArgumentException e) {
            print(e);
            assertTrue(AM_HUH, e.getMessage().contains(AbstractDeviceDriverProvider.EMSG_PARENT_NOT_BOUND));
        }
        assertNull(AM_HUH, provider.getBindings().get(DeviceDriverTestUtils.TYPE_BAZ));
    }

    @Test
    public void manyToOneMappings() {
        print(EOL + "manyToOneMappings()");
        assertEquals(AM_UXS, 1, provider.getBindingsCount());
        assertEquals(AM_UXS, 1, provider.getTypesCount());

        // let's map the FooType under a different name
        provider.addType(DeviceDriverTestUtils.TYPE_BAR, ddt);
        assertEquals(AM_UXS, 2, provider.getBindingsCount());
        assertEquals(AM_UXS, 1, provider.getTypesCount());

        provider.addType(DeviceDriverTestUtils.TYPE_BAZ, ddt);
        assertEquals(AM_UXS, 3, provider.getBindingsCount());
        assertEquals(AM_UXS, 1, provider.getTypesCount());

        printProviderMappings();

        assertSame(AM_NSR, ddt, provider.getBindings().get(DeviceDriverTestUtils.TYPE_FOO));
        assertSame(AM_NSR, ddt, provider.getBindings().get(DeviceDriverTestUtils.TYPE_BAR));
        assertSame(AM_NSR, ddt, provider.getBindings().get(DeviceDriverTestUtils.TYPE_BAZ));
    }

    @Test
    public void removeType() {
        print(EOL + "removeType()");
        assertTrue(AM_HUH, provider.getDeviceTypeNames().contains(DeviceDriverTestUtils.TYPE_FOO));
        assertEquals(AM_UXS, 1, provider.getBindingsCount());
        provider.addType(DeviceDriverTestUtils.TYPE_BAR, ddt);
        otherType = DeviceDriverTestUtils.createCannedType(provider, DeviceDriverTestUtils.TYPE_BAZ);
        provider.addType(otherType);
        printProviderMappings();
        assertEquals(AM_UXS, 3, provider.getBindingsCount());
        assertEquals(AM_UXS, 2, provider.getTypesCount());

        print("removing ddt...");
        provider.removeType(ddt);
        printProviderMappings();
        assertEquals(AM_UXS, 1, provider.getBindingsCount());
        assertEquals(AM_UXS, 1, provider.getTypesCount());
        assertSame(AM_NSR, otherType, provider.getDeviceType(DeviceDriverTestUtils.TYPE_BAZ));
    }

    @Test
    public void removeTypeCheckLineage() {
        print (EOL + "removeTypeCheckLineage()");
        // let's build a type hierarchy
        DefaultDeviceType bar = DeviceDriverTestUtils.createEmptyType(provider, ddt, DeviceDriverTestUtils.TYPE_BAR);
        provider.addType(bar);
        DefaultDeviceType baz = DeviceDriverTestUtils.createEmptyType(provider, ddt, DeviceDriverTestUtils.TYPE_BAZ);
        provider.addType(baz);

        DefaultDeviceType a = DeviceDriverTestUtils.createEmptyType(provider, bar, DeviceDriverTestUtils.TYPE_A);
        provider.addType(a);
        provider.addType(DeviceDriverTestUtils.createEmptyType(provider, bar, DeviceDriverTestUtils.TYPE_B));
        provider.addType(DeviceDriverTestUtils.createEmptyType(provider, bar, DeviceDriverTestUtils.TYPE_C));

        provider.addType(DeviceDriverTestUtils.createEmptyType(provider, baz, DeviceDriverTestUtils.TYPE_FRIK));
        provider.addType(DeviceDriverTestUtils.createEmptyType(provider, baz, DeviceDriverTestUtils.TYPE_FRAK));
        printProviderMappings();
        // FOO
        //   + BAR
        //   |   + A
        //   |   + B
        //   |   + C
        //   + BAZ
        //       + FRIK
        //       + FRAK
        assertEquals(AM_UXS, 8, provider.getBindingsCount());
        assertEquals(AM_UXS, 8, provider.getTypesCount());
        assertEquals(AM_UXS, 2, ddt.getChildTypes().size());
        assertEquals(AM_UXS, 3, bar.getChildTypes().size());
        assertEquals("missing type A", a, provider.getDeviceType(DeviceDriverTestUtils.TYPE_A));

        print(EOL + "Removing type A...");
        provider.removeType(a);
        printProviderMappings();
        // FOO
        //   + BAR
        //   |   + B
        //   |   + C
        //   + BAZ
        //       + FRIK
        //       + FRAK
        assertEquals(AM_UXS, 7, provider.getBindingsCount());
        assertEquals(AM_UXS, 7, provider.getTypesCount());
        assertEquals(AM_UXS, 2, bar.getChildTypes().size());
        assertNull("type A wasn't removed", provider.getDeviceType(DeviceDriverTestUtils.TYPE_A));

        print(EOL + "Removing type BAZ...");
        provider.removeType(baz);
        printProviderMappings();
        // FOO
        //   + BAR
        //   |   + B
        //   |   + C
        assertEquals(AM_UXS, 4, provider.getBindingsCount());
        assertEquals(AM_UXS, 4, provider.getTypesCount());
        assertEquals(AM_UXS, 1, ddt.getChildTypes().size());
        assertNull("baz not removed", provider.getDeviceType(DeviceDriverTestUtils.TYPE_BAZ));
        assertNull("frik not removed", provider.getDeviceType(DeviceDriverTestUtils.TYPE_FRIK));
        assertNull("frak not removed", provider.getDeviceType(DeviceDriverTestUtils.TYPE_FRAK));

        // removing non-bound type
        print(EOL + "Removing non-bound type...");
        otherType = DeviceDriverTestUtils.createEmptyType(provider, "Some non-bound type");
        try {
            provider.removeType(otherType);
            fail(AM_NOEX);
        } catch (IllegalStateException e) {
            print(e);
            assertTrue(AM_HUH, e.getMessage().contains(AbstractDeviceDriverProvider.EMSG_REMOVING_NON_BOUND_TYPE));
        }

        // our grand finale

        print(EOL + "Removing FOO...");
        provider.removeType(ddt);
        assertEquals(AM_UXS, 0, provider.getBindingsCount());
        assertEquals(AM_UXS, 0, provider.getTypesCount());
        assertEquals(AM_UXS, 0, provider.getDeviceTypeNames().size());
    }

    @Test
    public void switchType() {
        DefaultDeviceType bar = DeviceDriverTestUtils.createEmptyType(provider, DeviceDriverTestUtils.TYPE_BAR);
        provider.addType(bar);

        ddi = (DefaultDeviceInfo)provider.create(DeviceDriverTestUtils.TYPE_FOO);
        print(ddi.toDebugString());
        Assert.assertEquals(DeviceDriverTestUtils.WRONG_TYPE_INSTALLED, DeviceDriverTestUtils.TYPE_FOO, ddi.getTypeName());
        Assert.assertEquals(DeviceDriverTestUtils.WRONG_TYPE_INSTALLED, ddt, ddi.getType());

        print(EOL + "switching type...");
        provider.switchType(ddi, DeviceDriverTestUtils.TYPE_BAR);
        print(ddi.toDebugString());
        Assert.assertEquals(DeviceDriverTestUtils.WRONG_TYPE_INSTALLED, DeviceDriverTestUtils.TYPE_BAR, ddi.getTypeName());
        Assert.assertEquals(DeviceDriverTestUtils.WRONG_TYPE_INSTALLED, bar, ddi.getType());
    }

    @Test
    public void switchTypeNonBoundType() {
        // DON'T add BAR type to provider this time

        ddi = (DefaultDeviceInfo)provider.create(DeviceDriverTestUtils.TYPE_FOO);
        print(ddi.toDebugString());
        Assert.assertEquals(DeviceDriverTestUtils.WRONG_TYPE_INSTALLED, DeviceDriverTestUtils.TYPE_FOO, ddi.getTypeName());
        Assert.assertEquals(DeviceDriverTestUtils.WRONG_TYPE_INSTALLED, ddt, ddi.getType());

        print(EOL + "switching type...");
        try {
            provider.switchType(ddi, DeviceDriverTestUtils.TYPE_BAR);
            fail(AM_NOEX);
        } catch (IllegalStateException e) {
            print(e);
            assertTrue(AM_WREXMSG,
                       e.getMessage().contains(AbstractDeviceDriverProvider.EMSG_SWITCHING_NON_BOUND_TYPE));
        }
    }

    @Test
    public void createInfo() {
        print(EOL + "createInfo()");
        ddi = (DefaultDeviceInfo) provider.create(DeviceDriverTestUtils.TYPE_FOO);
        print(ddi.toDebugString());
        assertNotNull(AM_HUH, ddi);
    }

    private static final String CODE_WORD = "xyyzy";

    private static final String INJECT_INFO =
            "<" + DefaultDeviceInfo.PROPERTIES_ROOT_NAME + ">" + EOL +
            "<mydata>" + EOL +
            " <someValue>3</someValue>" + EOL +
            " <codeWord>"+CODE_WORD+"</codeWord>" + EOL +
            "</mydata>" + EOL +
            "</" + DefaultDeviceInfo.PROPERTIES_ROOT_NAME + ">" + EOL; 

    private static final String SOME_VALUE_KEY = "mydata.someValue";
    private static final String CODE_WORD_KEY = "mydata.codeWord";

    @Test
    public void createInfoWithInjectedData() {
        print(EOL + "createInfoWithInjectedData()");
        otherType = DeviceDriverTestUtils.createEmptyType(provider, DeviceDriverTestUtils.TYPE_BAZ, INJECT_INFO);
        provider.addType(otherType);
        ddi = (DefaultDeviceInfo) provider.create(DeviceDriverTestUtils.TYPE_BAZ);
        print(ddi.toDebugString());
        assertNotNull(AM_HUH, ddi);
        assertEquals("can't get some value?", 3, ddi.getInt(SOME_VALUE_KEY));
        String codeWord = ddi.get(CODE_WORD_KEY);
        print("Code Word = " + codeWord);
        assertEquals("incorrect code word", CODE_WORD, codeWord);
    }

    @Test
    public void createHandler() {
        print(EOL + "createHandler()");

        otherType = DeviceDriverTestUtils.createEmptyType(provider, DeviceDriverTestUtils.TYPE_BAZ, DeviceDriverTestUtils.MyHandler.class, DeviceDriverTestUtils.MyLoader.class);
        provider.addType(otherType);

        DeviceHandler handler = provider.create(DeviceDriverTestUtils.TYPE_BAZ, DeviceDriverTestUtils.IP);
        print(handler);
        assertNotNull(AM_HUH, handler);
        Assert.assertEquals(AM_NEQ, DeviceDriverTestUtils.IP, handler.getIpAddress());
        assertNotNull(AM_HUH, handler.getDeviceInfo());
        Assert.assertEquals(AM_NEQ, DeviceDriverTestUtils.TYPE_BAZ, handler.getDeviceInfo().getTypeName());
    }

    @Test
    public void createLoader() {
        print(EOL + "createLoader()");

        otherType = DeviceDriverTestUtils.createEmptyType(provider, DeviceDriverTestUtils.TYPE_BAZ, DeviceDriverTestUtils.MyHandler.class, DeviceDriverTestUtils.MyLoader.class);
        provider.addType(otherType);

        DeviceLoader loader = provider.create(DeviceDriverTestUtils.TYPE_BAZ, DeviceDriverTestUtils.UID);
        print(loader);
        assertNotNull(AM_HUH, loader);
        Assert.assertEquals(AM_NEQ, DeviceDriverTestUtils.UID, loader.getUID());
        assertNotNull(AM_HUH, loader.getDeviceInfo());
        Assert.assertEquals(AM_NEQ, DeviceDriverTestUtils.TYPE_BAZ, loader.getDeviceInfo().getTypeName());
    }

}
