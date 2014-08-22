/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.codec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class models an arbitrary depth tree where each node in the tree has a positive integer associated
 * with it, and has zero or more child nodes.
 * <p>
 * This class is not synchronized. If synchronization is required, it must be applied
 * externally.
 *
 * @author Simon Hunt
 */
public class CodedTree {

    private static final String COLON = ":";
    private static final String COLON_OPEN_B = ":[";
    private static final String CLOSE_B = "]";

    private List<Node> nodes;

    /** Constructs an empty coded tree. */
    public CodedTree() { }

    /** Constructs a coded tree with the specified root nodes.
     *
     * @param nodes the root nodes
     */
    public CodedTree(Node... nodes) {
        if (nodes != null && nodes.length > 0) {
            this.nodes = new ArrayList<Node>();
            this.nodes.addAll(Arrays.asList(nodes));
        }
    }


    @Override
    public String toString() {
        return nodes == null ? "[]" : nodes.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final CodedTree codedTree = (CodedTree) o;

        return !(nodes != null ? !nodes.equals(codedTree.nodes) : codedTree.nodes != null);
    }

    @Override
    public int hashCode() {
        return (nodes != null ? nodes.hashCode() : 0);
    }

    /** Returns the number of "root" nodes for this tree.
     *
     * @return the number of root nodes
     */
    public int nodeCount() {
        return nodes == null ? 0 : nodes.size();
    }

    /** Returns a reference to the list of "root" nodes in this tree.
     *
     * @return the root nodes
     */
    public List<Node> getNodes() {
        return nodes;
    }

    //=======================================================================================
    /** This inner class represents a node in a coded tree. */
    public static class Node {

        private int index;
        private Node parent;
        private List<Node> children;

        /** Constructs a node with the given index.
         *
         * @param index the node index
         */
        public Node(int index) {
            this.index = index;
        }

        /** Constructs a node with the given index and child nodes.
         *
         * @param index the node index
         * @param children the child nodes to be attached to this node
         */
        public Node(int index, Node... children) {
            this(index, Arrays.asList(children));
        }

        /** Constructs a node with the given index and child nodes.
         *
         * @param index the node index
         * @param children the child nodes to be attached to this node
         */
        public Node(int index, List<Node> children) {
            this.index = index;
            this.children = children;
            if (children != null) {
                for (Node cn: children) {
                    cn.setParent(this);
                }
            }
        }

        /** Returns the "index" value for this node.
         *
         * @return the index
         */
        public int getIndex() {
            return index;
        }

        /** Returns the parent node for this node. Returns null if this is a
         * "root" node.
         *
         * @return the parent node
         */
        public Node getParent() {
            return parent;
        }

        // internal method to allow the parent node to be set on this node
        private void setParent(Node parent) {
            this.parent = parent;
        }


        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            sb.append(index);
            if (children != null) {
                sb.append(COLON).append(children);
            }

            return sb.toString();
        }

        /** Adds the specified nodes as children of this node.
         *
         * @param nodes the child nodes
         */
        public void addChildren(Node... nodes) {
            if (nodes != null) {
                if (children == null) {
                    children = new ArrayList<Node>();
                }
                for (Node n: nodes) {
                    n.setParent(this);
                    children.add(n);
                }
            }
        }

        /** Creates leaf nodes with the specified indices and attaches them
         * as child nodes to this node.
         *
         * @param indices the indices of the child nodes
         */
        public void addChildren(int... indices) {
            if (indices != null) {
                if (children == null) {
                    children = new ArrayList<Node>();
                }
                for (int i: indices) {
                    Node n = new Node(i);
                    n.setParent(this);
                    children.add(n);
                }
            }
        }

        /** Returns the list of child nodes for this node. Will return null if this
         * is a leaf node.
         *
         * @return the child nodes.
         */
        public List<Node> getChildren() {
            return children;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final Node node = (Node) o;

            if (index != node.index) return false;
            return !(children != null ? !children.equals(node.children) : node.children != null);

        }

        @Override
        public int hashCode() {
            int result;
            result = index;
            result = 31 * result + (children != null ? children.hashCode() : 0);
            return result;
        }
    }


    //=======================================================================================
    private static final String RE_BRACKETS = "\\[(.*)\\]";
    private static final Pattern P_BRACKETS = Pattern.compile(RE_BRACKETS);

    private static final String RE_EMPTY = "\\s*";

    private static final String RE_INDEXED = "(\\d+):\\[(.*)\\]";
    private static final Pattern P_INDEXED = Pattern.compile(RE_INDEXED);

    private static final String RE_MULTI_INDEXED = "(" + RE_INDEXED + ")(,\\s*" + RE_INDEXED + ")+";
    private static final Pattern P_MULTI_INDEXED = Pattern.compile(RE_MULTI_INDEXED);

    private static final String RE_NUMBERS_ONLY = "(\\d+)(\\s*,\\s*\\d+)*";

    /** Creates a coded tree from a string representation.
     * For example:
     * <pre>
     * CodedTree tree = ... ;
     * String s = tree.toString();
     * CodedTree copy = CodedTree.valueOf(s);
     * assert(copy.equals(tree));
     * </pre>
     *
     * @param s the string representation
     * @return an equivalent coded tree
     */
    public static CodedTree valueOf(String s) {
        if (s == null)
            throw new NullPointerException("parameter cannot be null");

        Matcher m = P_BRACKETS.matcher(s);
        if (!m.matches())
            throw new IllegalArgumentException("bad syntax: " + s);

        List<Node> nodes = extractNodes(m.group(1));
        return nodes==null ? new CodedTree() : new CodedTree(nodes.toArray(new Node[nodes.size()]));
    }

    // this method recurses, extracting nodes from within bounding [ ] brackets
    private static List<Node> extractNodes(String inner) {
        if (inner.matches(RE_EMPTY))
            return null;

        if (inner.matches(RE_NUMBERS_ONLY))
            return extractLeafNodes(inner);

        Matcher m = P_INDEXED.matcher(inner);
        if (!m.matches())
            throw new IllegalArgumentException("bad internal syntax (,) : " + inner);

        Matcher mm = P_MULTI_INDEXED.matcher(inner);
        List<String> pieces = new ArrayList<String>();
        if (mm.matches()) {
            // need to break apart the comma delimited pieces
            breakApart(inner, pieces);
        } else {
            // work with the one piece
            pieces.add(inner);
        }

        List<Node> kids = new ArrayList<Node>(pieces.size());
        for (String s: pieces) {
            kids.add(extractNode(s));
        }
        return kids;
    }

    // this method extracts comma separated integers and returns them as a list of leaf nodes
    private static List<Node> extractLeafNodes(String numbers) {
        String[] numstrs = numbers.split("\\s*,\\s*");
        List<Node> nodes = new ArrayList<Node>(numstrs.length);
        for (String s: numstrs) {
            try {
                int idx = Integer.valueOf(s);
                nodes.add(new Node(idx));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Bad number format", e);
            }
        }
        return nodes;
    }

    // can't just rely on RE group matching, because we can't easily distinguish which level of the
    // recursion each comma might be at. So, we resort to counting open/close square brackets...
    private static void breakApart(String inner, List<String> pieces) {

        int p = 0;
        List<Integer> indices = new ArrayList<Integer>();
        List<String> innards = new ArrayList<String>();

        while (p < inner.length()) {
            // extract first bracketed node

            // extract the node index first
            int iColon = inner.indexOf(COLON_OPEN_B, p);
            if (iColon < 0)
                throw new IllegalArgumentException("':[' not found");
            int nodeIndex;
            try {
                nodeIndex = Integer.valueOf(inner.substring(p, iColon));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Couldn't parse node index", e);
            }
            boolean found = false;
            int q = iColon + 2;  // inside the open bracket
            int iStart = q;
            int bracketCount = 1;
            String innard = null;
            while (!found && q < inner.length()) {
                final char c = inner.charAt(q++);

                if (c == '[') {
                    bracketCount++;
                } else if (c == ']') {
                    bracketCount--;
                    if (bracketCount == 0) {
                        // found matching closing bracket
                        innard = inner.substring(iStart, q-1);
                        found = true;
                    }
                }
            }

            if (innard == null)
                throw new IllegalArgumentException("incomplete node data");

            indices.add(nodeIndex);
            innards.add(innard);

            // increment the pointer
            p = q;
            // suck up commas and spaces
            while (p < inner.length() &&
                    ( inner.charAt(p) == ' ' || inner.charAt(p) == ',') ) {
                p++;
            }

        }

        // by the time we get here, we should have populated the indices and innards lists
        for (int i=0; i< indices.size(); i++) {
            String piece = new StringBuilder().append(indices.get(i))
                    .append(COLON_OPEN_B).append(innards.get(i)).append(CLOSE_B).toString();
            pieces.add(piece);
        }
    }

    // extracts a node "{idx}:[{innards}]" by recursively calling extractNodes on the innards
    private static Node extractNode(String s) {
        Matcher m = P_INDEXED.matcher(s);
        if (!m.matches())
            throw new IllegalArgumentException("bad internal syntax : " + s);
        int index = Integer.valueOf(m.group(1));
        List<Node> kids = extractNodes(m.group(2));
        return new Node(index, kids);
    }

}
