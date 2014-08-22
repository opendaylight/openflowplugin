/*
 * (c) Copyright 2009,2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.util.cache;

import java.lang.ref.ReferenceQueue;

/**
 * This class is used by the WeakValueMapTest class.
 *
 * @author Simon Hunt
 */
class MyValueTypeB extends AbstractMyValue {
    private int number;

    public MyValueTypeB(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(final int number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return "[MyValueType B : " + number + "]";
    }

    /** Returns the shared reference queue for the unit test class.
     *
     * @return the reference queue
     */
    static ReferenceQueue<? super MyValueTypeB> getQ() {
        return getRefQ();
    }

}
