/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.stage;

/**
 * Fixture for data types of things processed through a flow. For simplicity,
 * it's just something that has a name.
 * 
 * @author Thomas Vachuska
 * @author Simon Hunt
 */
public abstract class Data {

    private String name;

    public Data(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
    
    public static class A extends Data {
        public A(String name) { super("A: " + name); }
    }

    public static class B extends Data {
        public B(String name) { super("B: " + name); }
    }

    public static class C extends Data {
        public C(String name) { super("C: " + name); }
    }

    public static class D extends Data {
        public D(String name) { super("D: " + name); }
    }

}
