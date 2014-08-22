/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import static org.opendaylight.util.junit.TestTools.AM_NEQ;
import static org.opendaylight.util.junit.TestTools.AM_UXS;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import org.opendaylight.util.junit.EqualityTester;
import org.opendaylight.util.SortSpecification.SortComponent;

/**
 * Set of tests for the SortSpecification
 * 
 * @author Scott Simes
 * @author Fabiel Zuniga
 */
public class SortSpecificationTest {
    private static final String SORT_UP = "Sort Up";
    private static final String SORT_DOWN = "Sort Down";

    @Test
    public void testBasic() {
        SortSpecification<String> stringSort = new SortSpecification<String>();
        stringSort.addSortComponent(SORT_UP, SortOrder.ASCENDING);

        List<SortComponent<String>> stringSortCriteria = stringSort.getSortComponents();
        assertEquals(AM_UXS, 1, stringSortCriteria.size());
        
        SortComponent<String> stringCrit = stringSortCriteria.get(0);
        assertEquals(AM_NEQ, SORT_UP, stringCrit.getSortBy());
        assertEquals(AM_NEQ, SortOrder.ASCENDING, stringCrit.getSortOrder());
    }
    
    @Test
    public void testEqualityAndHashCode() {
        SortSpecification<String> baseObjToTest = new SortSpecification<String>();
        SortSpecification<String> equalsToBase1 = new SortSpecification<String>();
        SortSpecification<String> equalsToBase2 = new SortSpecification<String>();
        SortSpecification<String> unequalToBase1 = new SortSpecification<String>();
        SortSpecification<String> unequalToBase2 = new SortSpecification<String>();
        SortSpecification<String> unequalToBase3 = new SortSpecification<String>();
        SortSpecification<Long> unequalToBase4 = new SortSpecification<Long>();
        SortSpecification<String> unequalToBase5 = new SortSpecification<String>();
        SortSpecification<String> unequalToBase6 = new SortSpecification<String>();
        
        baseObjToTest.addSortComponent(SORT_UP, SortOrder.ASCENDING);
        equalsToBase1.addSortComponent(SORT_UP, SortOrder.ASCENDING);
        equalsToBase2.addSortComponent(SORT_UP, SortOrder.ASCENDING);
        unequalToBase1.addSortComponent(SORT_UP, SortOrder.DESCENDING);
        unequalToBase2.addSortComponent(SORT_UP, SortOrder.ASCENDING);
        unequalToBase2.addSortComponent(SORT_DOWN, SortOrder.DESCENDING);
        unequalToBase4.addSortComponent(15L, SortOrder.ASCENDING);
        unequalToBase5.addSortComponent(null, SortOrder.ASCENDING);
        unequalToBase6.addSortComponent(SORT_UP, null);
        
        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1, equalsToBase2, unequalToBase1);
        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1, equalsToBase2, unequalToBase2);
        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1, equalsToBase2, unequalToBase3);
        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1, equalsToBase2, unequalToBase4);
        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1, equalsToBase2, unequalToBase5);
        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1, equalsToBase2, unequalToBase6);
        
        baseObjToTest = new SortSpecification<String>();
        equalsToBase1 = new SortSpecification<String>();
        equalsToBase2 = new SortSpecification<String>();
        unequalToBase1 = new SortSpecification<String>();
        unequalToBase1.addSortComponent(SORT_UP, SortOrder.ASCENDING);
        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1, equalsToBase2, unequalToBase1);
        
        baseObjToTest.addSortComponent(SORT_UP, SortOrder.ASCENDING);
        equalsToBase1.addSortComponent(SORT_UP, SortOrder.ASCENDING);
        equalsToBase2.addSortComponent(SORT_UP, SortOrder.ASCENDING);
        baseObjToTest.addSortComponent(SORT_DOWN, SortOrder.ASCENDING);
        equalsToBase1.addSortComponent(SORT_DOWN, SortOrder.ASCENDING);
        equalsToBase2.addSortComponent(SORT_DOWN, SortOrder.ASCENDING);
        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1, equalsToBase2, unequalToBase1);
        
        baseObjToTest = new SortSpecification<String>();
        equalsToBase1 = new SortSpecification<String>();
        equalsToBase2 = new SortSpecification<String>();
        unequalToBase1 = new SortSpecification<String>();
        
        baseObjToTest.addSortComponent(null, SortOrder.ASCENDING);
        equalsToBase1.addSortComponent(null, SortOrder.ASCENDING);
        equalsToBase2.addSortComponent(null, SortOrder.ASCENDING);
        unequalToBase1.addSortComponent(SORT_UP, SortOrder.ASCENDING);
        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1, equalsToBase2, unequalToBase1);
        
        baseObjToTest = new SortSpecification<String>();
        equalsToBase1 = new SortSpecification<String>();
        equalsToBase2 = new SortSpecification<String>();
        unequalToBase1 = new SortSpecification<String>();
        
        baseObjToTest.addSortComponent(SORT_UP, null);
        equalsToBase1.addSortComponent(SORT_UP, null);
        equalsToBase2.addSortComponent(SORT_UP, null);
        unequalToBase1.addSortComponent(SORT_UP, SortOrder.ASCENDING);
        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1, equalsToBase2, unequalToBase1);
    }
}
