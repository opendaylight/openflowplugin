/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.json;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.opendaylight.util.junit.TestTools.EOL;
import static org.opendaylight.util.junit.TestTools.print;
import static junit.framework.Assert.fail;

/**
 * Unit tests for AbstractJsonFactory.
 *
 * @author Simon Hunt
 */
public class AbstractJsonFactoryTest {
    private static final String AM_MC = "Missing Codec";
    private static final String AM_UC = "Unexpected Codec";

    private static final String FMT_EX = "EX> {}";
    private static final String FMT_POJO_CODEC = "POJO: {} => CODEC: {}";

    private static class MyClass { boolean foo() { return true; } }

    private static class MyOtherClass { boolean bar() { return false; } }

    private static interface MyInterface { boolean foo(); }

    private static class MyAdapter implements MyInterface {
        @Override public boolean foo() { return false; }
    }

    private static class MyAdapterSubclass extends MyAdapter { }

    private static class ClassCodec extends AbstractJsonCodec<MyClass> {
        protected ClassCodec () { super("FOO", "FOOs"); }
        @Override public ObjectNode encode(MyClass pojo) { return null; }
        @Override public MyClass decode(ObjectNode node) { return null; }
        @Override public String toString() { return "ClassCodec"; }
    }

    private static class InterfaceCodec extends AbstractJsonCodec<MyInterface> {
        protected InterfaceCodec() { super("FOO", "FOOs"); }
        @Override public ObjectNode encode(MyInterface pojo) { return null; }
        @Override public MyInterface decode(ObjectNode node) { return null; }
        @Override public String toString() { return "InterfaceCodec"; }
    }

    private static class TestFactory extends AbstractJsonFactory {
        private TestFactory() {
            addCodecs(MyClass.class, new ClassCodec());
            addCodecs(MyInterface.class, new InterfaceCodec());
        }
    }


    private JsonFactory factory;

    @Before
    public void setUp() {
        factory = new TestFactory();
    }

    private void verifyCodec(Class<?> pojo, boolean expected) {
        JsonCodec<?> codec = null;
        try {
            codec = factory.codec(pojo);
            if (expected)
                print(FMT_POJO_CODEC, pojo, codec);
            else
                fail(AM_UC);

        } catch (JsonCodecException e) {
            if (expected)
                fail(AM_MC);
            else
                print(FMT_EX, e);
        }
    }

    @Test
    public void codecForClass() {
        print(EOL + "codecForClass()");
        verifyCodec(MyClass.class, true);
    }

    @Test
    public void codecForInterfaceImplementation() {
        print(EOL + "codecForInterfaceImplementation()");
        verifyCodec(MyAdapter.class, true);
    }

    // FIXME: Needed to allow correct use of adapters in some unit tests
    @Test @Ignore
    public void codecForSubclassOfInterfaceImplementation() {
        print(EOL + "codecForSubclassOfInterfaceImplementation()");
        verifyCodec(MyAdapterSubclass.class, true);
    }

    @Test
    public void noCodecInstalled() {
        print(EOL + "noCodecInstalled()");
        verifyCodec(MyOtherClass.class, false);
    }

    // TODO: Unit tests for the remaining functionality...
}
