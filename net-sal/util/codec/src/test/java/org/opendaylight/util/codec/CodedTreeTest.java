/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.codec;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.opendaylight.util.junit.TestTools.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * This JUnit test class tests the CodedTree class.
 *
 * @author Simon Hunt
 */
public class CodedTreeTest {

    @Test
    public void basic() {
        print(EOL + "basic()");
        CodedTree ct = new CodedTree();
        checkEncodeDecode(ct);
    }

    private void checkEncodeDecode(CodedTree ct) {
        print(ct);
        String asString = ct.toString();
        CodedTree decoded = CodedTree.valueOf(asString);
        assertEquals("trees not equal", ct, decoded);
    }

    @Test
    public void oneLevel() {
        print(EOL + "oneLevel()");
        List<CodedTree.Node> kids = new ArrayList<CodedTree.Node>();
        kids.add(new CodedTree.Node(1));
        kids.add(new CodedTree.Node(2));
        kids.add(new CodedTree.Node(3));
        CodedTree ct = new CodedTree(new CodedTree.Node(0, kids));
        print(ct);

        CodedTree.Node copy = new CodedTree.Node(0);
        copy.addChildren(1,2,3);
        CodedTree ct2 = new CodedTree(copy);

        print(ct2);
        print(ct.equals(ct2));

        assertEquals(AM_HUH, ct, ct2);

        checkEncodeDecode(ct);
    }

    @Test
    public void twoLevels() {
        print(EOL + "twoLevels()");

        CodedTree.Node node1 = new CodedTree.Node(0);
        node1.addChildren(1,2,3);

        CodedTree.Node node2 = new CodedTree.Node(2);
        node2.addChildren(5);

        CodedTree.Node nodeA = new CodedTree.Node(0);
        nodeA.addChildren(node1, node2);

        CodedTree.Node node3 = new CodedTree.Node(4);
        node3.addChildren(3,5);

        CodedTree.Node nodeB = new CodedTree.Node(1);
        nodeB.addChildren(node3);

        CodedTree ct = new CodedTree(nodeA, nodeB);

        checkEncodeDecode(ct);
    }

    @Test
    public void threeLevels() {
        print(EOL + "threeLevels()");

        CodedTree.Node node1 = new CodedTree.Node(500);
        node1.addChildren(501, 502, 503);

        CodedTree.Node node2 = new CodedTree.Node(700);
        node2.addChildren(777, 778, 779);

        CodedTree.Node node3 = new CodedTree.Node(333, node1, node2);


        CodedTree.Node node4 = new CodedTree.Node(100);
        node4.addChildren(101, 103);

        CodedTree.Node node5 = new CodedTree.Node(200);
        node5.addChildren(228, 229);

        CodedTree.Node node6 = new CodedTree.Node(444, node4, node5);

        CodedTree.Node node7 = new CodedTree.Node(999, node3, node6);

        CodedTree ct = new CodedTree(node7);
        checkEncodeDecode(ct);
    }

    @Test
    public void equality() {
        CodedTree.Node a = new CodedTree.Node(5);
        a.addChildren(1, 2, 4);

        CodedTree.Node b = new CodedTree.Node(5);
        b.addChildren(1, 2, 4);

        print("NODES A & B: " + b);
        assertEquals("nodes not equal", a, b);
        assertEquals("nodes not equal", b, a);
        

        CodedTree.Node c = new CodedTree.Node(4);
        c.addChildren(1, 2, 4);
        print("NODE C: "+ c);
        assertFalse("should not be equal", a.equals(c));
        assertFalse("should not be equal", c.equals(a));

        CodedTree.Node d = new CodedTree.Node(5);
        d.addChildren(1, 2);
        print("NODE D: "+ d);
        assertFalse("should not be equal", a.equals(d));
        assertFalse("should not be equal", d.equals(a));


        CodedTree ctA = new CodedTree(a);
        CodedTree ctB = new CodedTree(b);
        print("TREE A: " + ctA);
        assertEquals("Trees not equal", ctA, ctB);
        assertEquals("Trees not equal", ctB, ctA);

        CodedTree ctC = new CodedTree(a, c);
        print("TREE C: " + ctC);
        assertFalse("Should not be equal", ctA.equals(ctC));
        assertFalse("Should not be equal", ctC.equals(ctA));

        checkEncodeDecode(ctA);
        checkEncodeDecode(ctB);
        checkEncodeDecode(ctC);
    }


    // TODO: Need more unit tests for coverage, and negative testing (check exceptions for bad strings)

}
