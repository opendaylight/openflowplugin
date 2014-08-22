/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.filter;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import org.opendaylight.util.junit.EqualityTester;
import org.opendaylight.util.Interval;

/**
 * Set of test cases for the util filter package
 * 
 * @author Scott Simes
 */
public class FilterTest {

    private static final String STR_FLTR_VALUE = "Hello";

    private static final Long LONG_VALUE = 10L;

    @Test
    public void testComparableFilter() {
        ComparableCondition<String> compFilter =
                new ComparableCondition<String>(STR_FLTR_VALUE, ComparableCondition.Mode.EQUAL);
        
        assertEquals("Filter Value should equal", STR_FLTR_VALUE, compFilter.getValue());
        assertEquals("Mode should be equal", ComparableCondition.Mode.EQUAL, compFilter.getMode());
        assertEquals(ComparableCondition.Mode.GREATER_THAN, ComparableCondition.Mode.valueOf("GREATER_THAN"));
    }
    
    @Test
    public void testComparableEqualsAndHashCode() {
        ComparableCondition<String> baseObjToTest = new ComparableCondition<String>(STR_FLTR_VALUE, ComparableCondition.Mode.EQUAL);
        ComparableCondition<String> equalsToBase1 = new ComparableCondition<String>(STR_FLTR_VALUE, ComparableCondition.Mode.EQUAL);
        ComparableCondition<String> equalsToBase2 = new ComparableCondition<String>(STR_FLTR_VALUE, ComparableCondition.Mode.EQUAL);
        ComparableCondition<String> unequalToBase1 = new ComparableCondition<String>(STR_FLTR_VALUE, ComparableCondition.Mode.GREATER_THAN);
        ComparableCondition<Long> unequalToBase2 = new ComparableCondition<Long>(10L, ComparableCondition.Mode.EQUAL);
        ComparableCondition<String> unequalToBase3 = new ComparableCondition<String>(null, ComparableCondition.Mode.EQUAL);
                
        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1, equalsToBase2, unequalToBase1);
        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1, equalsToBase2, unequalToBase2);
        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1, equalsToBase2, unequalToBase3);
        
        baseObjToTest = new ComparableCondition<String>(null, ComparableCondition.Mode.LESS_THAN);
        equalsToBase1 = new ComparableCondition<String>(null, ComparableCondition.Mode.LESS_THAN);
        equalsToBase2 = new ComparableCondition<String>(null, ComparableCondition.Mode.LESS_THAN);
        unequalToBase1 = new ComparableCondition<String>(STR_FLTR_VALUE, ComparableCondition.Mode.LESS_THAN);
        unequalToBase3 = new ComparableCondition<String>(STR_FLTR_VALUE, null);
        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1, equalsToBase2, unequalToBase1);
        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1, equalsToBase2, unequalToBase3);
        
        baseObjToTest = new ComparableCondition<String>(STR_FLTR_VALUE, null);
        equalsToBase1 = new ComparableCondition<String>(STR_FLTR_VALUE, null);
        equalsToBase2 = new ComparableCondition<String>(STR_FLTR_VALUE, null);
        unequalToBase1 = new ComparableCondition<String>(STR_FLTR_VALUE, ComparableCondition.Mode.LESS_THAN);
        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1, equalsToBase2, unequalToBase1);
    }

    @Test
    public void testEqualityFilter() {
        EqualityCondition<Long> equFilter = new EqualityCondition<Long>(LONG_VALUE, EqualityCondition.Mode.EQUAL);
        
        assertEquals("Filter Value should equal", LONG_VALUE,equFilter.getValue());
        assertEquals("Mode should be equal", EqualityCondition.Mode.EQUAL, equFilter.getMode());
        assertEquals(EqualityCondition.Mode.UNEQUAL,EqualityCondition.Mode.valueOf("UNEQUAL"));
    }
    
    @Test
    public void testEqualityFilterEqualsAndHashCode() {
        EqualityCondition<Long> baseObjToTest = new EqualityCondition<Long>(LONG_VALUE, EqualityCondition.Mode.EQUAL);
        EqualityCondition<Long> equalsToBase1 = new EqualityCondition<Long>(LONG_VALUE, EqualityCondition.Mode.EQUAL);
        EqualityCondition<Long> equalsToBase2 = new EqualityCondition<Long>(LONG_VALUE, EqualityCondition.Mode.EQUAL);
        EqualityCondition<Long> unequalToBase1 = new EqualityCondition<Long>(LONG_VALUE, EqualityCondition.Mode.UNEQUAL);
        EqualityCondition<Long> unequalToBase2 = new EqualityCondition<Long>(4L, EqualityCondition.Mode.UNEQUAL);
        EqualityCondition<Boolean> unequalToBase3 = new EqualityCondition<Boolean>(new Boolean(true), EqualityCondition.Mode.EQUAL);
        EqualityCondition<Long> unequalToBase4 = new EqualityCondition<Long>(null, EqualityCondition.Mode.UNEQUAL);
        EqualityCondition<Long> unequalToBase5 = new EqualityCondition<Long>(LONG_VALUE, null);
        
        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1, equalsToBase2, unequalToBase1);
        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1, equalsToBase2, unequalToBase2);
        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1, equalsToBase2, unequalToBase3);
        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1, equalsToBase2, unequalToBase4);
        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1, equalsToBase2, unequalToBase5);
        
        baseObjToTest = new EqualityCondition<Long>(null, EqualityCondition.Mode.EQUAL);
        equalsToBase1 = new EqualityCondition<Long>(null, EqualityCondition.Mode.EQUAL);
        equalsToBase2 = new EqualityCondition<Long>(null, EqualityCondition.Mode.EQUAL);
        unequalToBase1 = new EqualityCondition<Long>(LONG_VALUE, EqualityCondition.Mode.EQUAL);
        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1, equalsToBase2, unequalToBase1);
        
        baseObjToTest = new EqualityCondition<Long>(LONG_VALUE, null);
        equalsToBase1 = new EqualityCondition<Long>(LONG_VALUE, null);
        equalsToBase2 = new EqualityCondition<Long>(LONG_VALUE, null);
        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1, equalsToBase2, unequalToBase1);
    }

    @Test
    public void testIntervalFilter() {
        Long left = 5L;
        Long right = 10L;
        Interval<Long> interval = Interval.valueOfOpen(left, right);
        IntervalCondition<Long> interFilter =
                new IntervalCondition<Long>(interval, IntervalCondition.Mode.NOT_IN);
        
        assertEquals("Filter Value should equal", interval,
                     interFilter.getValue());
        assertEquals("Mode should be equal", IntervalCondition.Mode.NOT_IN,
                     interFilter.getMode());
        assertEquals(IntervalCondition.Mode.IN, IntervalCondition.Mode.valueOf("IN"));
    }
    
    @Test
    public void testIntervalFilterEqualityAndHashCode() {
        Long left = 5L;
        Long right = 10L;
        Interval<Long> interval = Interval.valueOfOpen(left, right);
        Interval<Long> interval2 = Interval.valueOfLeftClosedRightOpen(left, right);
        IntervalCondition<Long> baseObjToTest = new IntervalCondition<Long>(interval, IntervalCondition.Mode.NOT_IN);
        IntervalCondition<Long> equalsToBase1 = new IntervalCondition<Long>(interval, IntervalCondition.Mode.NOT_IN);
        IntervalCondition<Long> equalsToBase2 = new IntervalCondition<Long>(interval, IntervalCondition.Mode.NOT_IN);
        IntervalCondition<Long> unequalToBase1 = new IntervalCondition<Long>(interval, IntervalCondition.Mode.IN);
        IntervalCondition<Long> unequalToBase2 = new IntervalCondition<Long>(interval2, IntervalCondition.Mode.IN);
        IntervalCondition<Long> unequalToBase3 = new IntervalCondition<Long>(null, IntervalCondition.Mode.NOT_IN);
        IntervalCondition<Long> unequalToBase4 = new IntervalCondition<Long>(interval, null);
        
        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1, equalsToBase2, unequalToBase1);
        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1, equalsToBase2, unequalToBase2);
        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1, equalsToBase2, unequalToBase3);
        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1, equalsToBase2, unequalToBase4);
        
        baseObjToTest = new IntervalCondition<Long>(null, IntervalCondition.Mode.NOT_IN);
        equalsToBase1 = new IntervalCondition<Long>(null, IntervalCondition.Mode.NOT_IN);
        equalsToBase2 = new IntervalCondition<Long>(null, IntervalCondition.Mode.NOT_IN);
        unequalToBase1 = new IntervalCondition<Long>(interval, IntervalCondition.Mode.NOT_IN);
        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1, equalsToBase2, unequalToBase1);
        
        
        baseObjToTest = new IntervalCondition<Long>(interval, null);
        equalsToBase1 = new IntervalCondition<Long>(interval, null);
        equalsToBase2 = new IntervalCondition<Long>(interval, null);
        unequalToBase1 = new IntervalCondition<Long>(interval, IntervalCondition.Mode.NOT_IN);
        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1, equalsToBase2, unequalToBase1);
        
    }

    @Test
    public void testSetFilter() {
        HashSet<Long> longSet = new HashSet<Long>();
        longSet.add(5L);
        longSet.add(10L);
        longSet.add(15L);
        SetCondition<Long> setFilter = new SetCondition<Long>(longSet, SetCondition.Mode.IN);
        
        assertEquals("Filter Value should equal", longSet, setFilter.getValues());
        assertEquals("Mode should be equal", SetCondition.Mode.IN, setFilter.getMode());
        assertEquals(SetCondition.Mode.NOT_IN, SetCondition.Mode.valueOf("NOT_IN"));
    }
    
    @Test
    public void testSetFilterEqualsAndHashCode() {
        Set<Long> longSet = new HashSet<Long>();
        longSet.add(5L);
        longSet.add(10L);
        longSet.add(15L);
        
        Set<Long> longSetTwo = new HashSet<Long>(longSet);
        longSetTwo.add(20L);
        
        Set<String> stringSet = new HashSet<String>();
        stringSet.add(STR_FLTR_VALUE);
        
        Set<Long> emptySet = new HashSet<Long>();
        
        SetCondition<Long> baseObjToTest = new SetCondition<Long>(longSet, SetCondition.Mode.IN);
        SetCondition<Long> equalsToBase1 = new SetCondition<Long>(longSet, SetCondition.Mode.IN);
        SetCondition<Long> equalsToBase2 = new SetCondition<Long>(longSet, SetCondition.Mode.IN);
        SetCondition<Long> unequalToBase1 = new SetCondition<Long>(longSet, SetCondition.Mode.NOT_IN);
        SetCondition<String> unequalToBase2 = new SetCondition<String>(stringSet, SetCondition.Mode.IN);
        SetCondition<Long> unequalToBase3 = new SetCondition<Long>(emptySet, SetCondition.Mode.IN);
        SetCondition<Long> unequalToBase4 = new SetCondition<Long>(longSet, null);
        
        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1, equalsToBase2, unequalToBase1);
        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1, equalsToBase2, unequalToBase2);
        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1, equalsToBase2, unequalToBase3);
        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1, equalsToBase2, unequalToBase4);
        
        
        baseObjToTest = new SetCondition<Long>(longSet, null);
        equalsToBase1 = new SetCondition<Long>(longSet, null);
        equalsToBase2 = new SetCondition<Long>(longSet, null);
        unequalToBase1 = new SetCondition<Long>(longSet, SetCondition.Mode.IN);
        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1, equalsToBase2, unequalToBase1);
    }

    @Test
    public void testStringFilter() {
        StringCondition strFilter = new StringCondition(STR_FLTR_VALUE, StringCondition.Mode.CONTAINS);
        
        assertEquals("Filter Value should equal", STR_FLTR_VALUE,strFilter.getValue());
        assertEquals("Mode should be equal", StringCondition.Mode.CONTAINS,strFilter.getMode());
        assertEquals(StringCondition.Mode.ENDS_WITH, StringCondition.Mode.valueOf("ENDS_WITH"));
    }
    
    @Test
    public void testStringFilterEqualityAndHashCode() {
        StringCondition baseObjToTest = new StringCondition(STR_FLTR_VALUE, StringCondition.Mode.CONTAINS); 
        StringCondition equalsToBase1 = new StringCondition(STR_FLTR_VALUE, StringCondition.Mode.CONTAINS);
        StringCondition equalsToBase2 = new StringCondition(STR_FLTR_VALUE, StringCondition.Mode.CONTAINS);
        StringCondition unequalToBase1 = new StringCondition(STR_FLTR_VALUE, StringCondition.Mode.ENDS_WITH);
        StringCondition unequalToBase2 = new StringCondition("Goodbye", StringCondition.Mode.CONTAINS);
        StringCondition unequalToBase3 = new StringCondition(null, StringCondition.Mode.CONTAINS);
        StringCondition unequalToBase4 = new StringCondition(STR_FLTR_VALUE, null);
        
        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1, equalsToBase2, unequalToBase1);
        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1, equalsToBase2, unequalToBase2);
        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1, equalsToBase2, unequalToBase3);
        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1, equalsToBase2, unequalToBase4);
        
        baseObjToTest = new StringCondition(null, StringCondition.Mode.CONTAINS); 
        equalsToBase1 = new StringCondition(null, StringCondition.Mode.CONTAINS);
        equalsToBase2 = new StringCondition(null, StringCondition.Mode.CONTAINS);
        unequalToBase1 = new StringCondition(STR_FLTR_VALUE, StringCondition.Mode.CONTAINS);
        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1, equalsToBase2, unequalToBase1);
        
        baseObjToTest = new StringCondition(STR_FLTR_VALUE, null); 
        equalsToBase1 = new StringCondition(STR_FLTR_VALUE, null);
        equalsToBase2 = new StringCondition(STR_FLTR_VALUE, null);
        unequalToBase1 = new StringCondition(STR_FLTR_VALUE, StringCondition.Mode.CONTAINS);
        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1, equalsToBase2, unequalToBase1);
    }
}
