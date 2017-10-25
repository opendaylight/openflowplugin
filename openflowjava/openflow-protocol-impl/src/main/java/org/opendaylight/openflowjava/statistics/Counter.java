/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.statistics;

import java.util.concurrent.atomic.AtomicLong;
/**
 * Counts statistics
 *
 * @author madamjak
 */
public class Counter {

    private AtomicLong counterValue;
    private AtomicLong counterLastReadValue;

    /**
     * Default constructor
     */
    public Counter() {
        counterValue = new AtomicLong(0L);
        counterLastReadValue = new AtomicLong(0L);
    }

    /**
     * Increment current counter value
     */
    public void incrementCounter(){
        counterValue.incrementAndGet();
    }

    /**
     * return the last read value of counter. This value can be set during the reading of current counter value,
     *      for detail see method getCounterValue(boolean modifyLastReadValue).
     * @return the counterLastReadValue
     */
    public long getCounterLastReadValue() {
        return counterLastReadValue.get();
    }

    /**
     * get current value of counter and rewrite CounterLastReadValue by current value
     * @return  the current value of counter
     */
    public long getCounterValue() {
        return getCounterValue(true);
    }

    /**
     * get current counter value
     * @param modifyLastReadValue
     *      true - CounterLastReadValue will be rewritten by current CounterValue
     *      false - no change CounterLastReadValue
     * @return the current value of counter
     */
    public long getCounterValue(boolean modifyLastReadValue) {
        if(modifyLastReadValue){
            counterLastReadValue.set(counterValue.get());
        }
        return counterValue.get();
    }

    /**
     * set current counter value and CounterLastReadValue to 0 (zero)
     */
    public void reset(){
        counterValue.set(0l);
        counterLastReadValue.set(0l);
    }

    /**
     * @return last and current count for specified statistic
     */
    public String getStat() {
        long cntPrevVal = getCounterLastReadValue();
        long cntCurValue = getCounterValue();
        return String.format("+%d | %d",cntCurValue-cntPrevVal,cntCurValue);
    }
}
