/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.driver.impl;

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.opendaylight.util.driver.DefaultDeviceInfo;
import org.opendaylight.util.driver.DefaultDeviceType;
import org.opendaylight.util.driver.DefaultDeviceTypeBuilder;
import org.opendaylight.util.driver.DeviceDriverFactory;
import org.opendaylight.net.driver.impl.DeviceTypeFinder;


public class DeviceTypeFinderTest {

    private DeviceDriverFactory ddp;
    private DeviceTypeFinder finder;
    private Map<String, DefaultDeviceInfo> map;
    private Set<String> emptyOids = Collections.<String>emptySet();

    static final String TYPE_A = "Type A";
    static final String TYPE_B = "Type B";
    static final String TYPE_C = "Type C";
    static final String VENDOR = "Vendor-Packard";
    static final String MODEL_A = "24p Switch";
    static final String MODEL_B = "Chassis Switch";
    static final String FW_1 = "F/W-1.00.01";
    static final String FW_2 = "F/W-1.00.02";


    @Before
    public void setUp() throws Exception {
        ddp = createMock(DeviceDriverFactory.class);
        finder = new DeviceTypeFinder(ddp);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void exactMatch() {
        map = new HashMap<String, DefaultDeviceInfo>();
        createCannedType(TYPE_A);
        expectMap();

        replay(ddp);

        finder.setSearchTerms(VENDOR, MODEL_A, FW_1);
        String typeName = finder.findTypeName();
        assertEquals("Invalid type found", TYPE_A, typeName);

        verify(ddp);
    }

    @Test
    public void failMatch() {
        map = new HashMap<String, DefaultDeviceInfo>();
        createCannedType(TYPE_A);
        expectMap();

        replay(ddp);

        finder.setSearchTerms(VENDOR, MODEL_A, FW_2);
        String typeName = finder.findTypeName();
        assertEquals("Invalid type found", null, typeName);

        verify(ddp);
    }

    @Test
    public void partialMatch() {
        map = new HashMap<String, DefaultDeviceInfo>();
        createCannedType(TYPE_A);
        createTypeFw(TYPE_B, null);
        expectMap();

        replay(ddp);

        finder.setSearchTerms(VENDOR, MODEL_A, FW_2);
        String typeName = finder.findTypeName();
        assertEquals("Invalid type found", TYPE_B, typeName);

        verify(ddp);
    }

    @Test
    public void genericMatch() {
        map = new HashMap<String, DefaultDeviceInfo>();
        createCannedType(TYPE_A);
        createTypeFw(TYPE_B, null);
        createTypeVendor(TYPE_C, VENDOR);
        expectMap();

        replay(ddp);

        finder.setSearchTerms(VENDOR, MODEL_B, FW_1);
        String typeName = finder.findTypeName();
        assertEquals("Invalid type found", TYPE_C, typeName);

        verify(ddp);
    }

    @Test
    public void findGeneric() {
        map = new HashMap<String, DefaultDeviceInfo>();
        createCannedType(TYPE_A);
        createTypeFw(TYPE_B, null);
        createTypeVendor(TYPE_C, VENDOR);
        expectMap();

        replay(ddp);

        finder.setSearchTerms(VENDOR, null, null);
        String typeName = finder.findTypeName();
        assertEquals("Invalid type found", TYPE_C, typeName);

        verify(ddp);
    }

    private void expectMap() {
        expect(ddp.getDeviceTypeNames()).andReturn(map.keySet());
        for (String type : map.keySet()) {
            expect(ddp.create(type)).andReturn(map.get(type));
        }
    }

   private void createCannedType(String typeName) {
       mapPut(new DefaultDeviceTypeBuilder(typeName).vendor(VENDOR)
                  .product(MODEL_A).fw(FW_1).oids(emptyOids).build());
   }
   private void createTypeFw(String typeName, String fw) {
       mapPut(new DefaultDeviceTypeBuilder(typeName).vendor(VENDOR)
                  .product(MODEL_A).fw(fw).oids(emptyOids).build());
   }
   private void createTypeVendor(String typeName, String vendor) {
       mapPut(new DefaultDeviceTypeBuilder(typeName).vendor(vendor).oids(emptyOids).build());
   }
   private void mapPut(DefaultDeviceType ddt) {
       map.put(ddt.getTypeName(), new DefaultDeviceInfo(ddt));
   }

}
