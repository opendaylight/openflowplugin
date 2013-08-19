package org.openflow.codec.util;

import org.openflow.codec.protocol.factory.OFPBasicFactoryImpl;
import org.openflow.codec.protocol.factory.OFPMessageFactory;

import junit.framework.TestCase;

public class OFTestCase extends TestCase {
    public OFPMessageFactory messageFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        messageFactory = new OFPBasicFactoryImpl();
    }

    public void test() throws Exception {
    }
}
