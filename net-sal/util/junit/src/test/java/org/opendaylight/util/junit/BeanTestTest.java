/*
 * (c) Copyright 2011 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.junit;

import junit.framework.Assert;
import org.junit.Test;

import java.util.Date;

import static org.opendaylight.util.junit.TestTools.EOL;
import static org.opendaylight.util.junit.TestTools.print;

/**
 * Test of the bean test.  How's that for meta-meta?
 *
 * @author Thomas Vachuska
 */
public class BeanTestTest extends BeanTest {
    
    public static class Foo {
        
        private String name;
        private int id;
        private boolean bar;
        private boolean goo;
        private short mode;
        private long mask;
        private float ohms;
        private double impedance;
        private char separator;
        private Date date;
        private byte led;
        private int special;
                
        public char getSeparator() {
            return separator;
        }
        
        public void setSeparator(char separator) {
            this.separator = separator;
        }
        
        public Date getDate() {
            return date;
        }
        
        protected void setDate(Date date) {
            this.date = date;
        }
        
        public byte getLed() {
            return led;
        }

        void setLed(byte led) {
            this.led = led;
        }

        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public int id() {
            return id;
        }
        
        public int getId() {
            return id;
        }
        
        public void setId(int id) {
            this.id = id;
        }
        
        public boolean isBar() {
            return bar;
        }
        
        public void setBar(boolean bar) {
            this.bar = bar;
        }
        
        public boolean isGoo() {
            return goo;
        }
        
        public void setIsGoo(boolean goo) {
            this.goo= goo;
        }
        
        public short getMode() {
            return mode;
        }
        
        public void setMode(short mode) {
            this.mode = mode;
        }
        
        public long getMask() {
            return mask;
        }
        
        public void setMask(long mask) {
            this.mask = mask;
        }
        
        public float getOhms() {
            return ohms;
        }

        public void setOhms(float ohms) {
            this.ohms = ohms;
        }
        
        public double getImpedance() {
            return impedance;
        }
        
        public void setImpedance(double impedance) {
            this.impedance = impedance;
        }

        public int getSpecial() {
            return special;
        }

        @BeanTestIgnore
        public void setSpecial(int special) {
            this.special = special;
        }
    }

    @Test
    public void test() throws Exception {
        testGettersAndSetters(new Foo());
    }

    // TODO: add some tests of the config

    @Test
    public void nullVerbosityTest() throws Exception {
        print(EOL + "nullVerbosityTest()");
        String result = testGettersAndSetters(new Foo(), DEFAULT_CONFIG, null);
        print(result);
        Assert.assertEquals("not empty", "", result);
    }

    @Test
    public void silentTest() throws Exception {
        print(EOL + "silentTest()");
        String result = testGettersAndSetters(new Foo(), DEFAULT_CONFIG, Verbosity.SILENT);
        print(result);
        Assert.assertEquals("not empty", "", result);
    }

    @Test
    public void terseTest() throws Exception {
        print(EOL + "terseTest()");
        String result = testGettersAndSetters(new Foo(), DEFAULT_CONFIG, Verbosity.TERSE);
        print(result);
        // TODO: assert output result
    }

    @Test
    public void verboseTest() throws Exception {
        print(EOL + "verboseTest()");
        String result = testGettersAndSetters(new Foo(), DEFAULT_CONFIG, Verbosity.VERBOSE);
        print(result);
        // TODO: assert output result
    }

}
