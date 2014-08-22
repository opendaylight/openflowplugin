/*
 * (c) Copyright 2001-2002 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.syntax;

import java.io.Serializable;
import java.text.ParsePosition;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/** 
 * Class representing a fragment of syntax, i.e a node that references other
 * syntax nodes via it's own usage string.
 *
 * @author Thomas Vachuska
 */
public class SyntaxFragment extends SyntaxNodeExtension {

    private static final long serialVersionUID = -3517660548995099231L;

    /** Separator between components in usage String.  */
    public static final String USAGE_STRING_SEPARATOR = " ";

    /** List of anchored and positional nodes.  */
    private List<SyntaxNode> anchoredNodes = null;

    /** List of floating and non-positional nodes. */
    private List<SyntaxNode> floatingNodes = null;
    
    /** Help topic.  */
    private String helpTopics = null;

    
    /** 
     * Default constructor.
     */
    protected SyntaxFragment() {
    }

    /**
     * Constructs a new syntax fragment.
     * 
     * @param node originating node
     * @param parent parent node
     * @param anchoredNodes list of anchored nodes
     * @param floatingNodes list of floating nodes
     */
    public SyntaxFragment(SyntaxNode node, SyntaxNodeExtension parent,
                          List<SyntaxNode> anchoredNodes, 
                          List<SyntaxNode> floatingNodes) {
        super(node, parent);
        this.anchoredNodes = anchoredNodes;
        this.floatingNodes = floatingNodes;
        
        //  Get the help topic, if one is specified.
        Properties db = getDB();
        helpTopics = translate(db.getProperty(KW_HELP_TOPICS));
        if (helpTopics == null) {
            SyntaxNode firstNode = getFirstAnchoredNode();
            if (firstNode != null)
                setHelpTopics(firstNode.toString());
        }
    }

    /**
     * Get the list of anchored, i.e. positional, nodes in this fragment
     *
     * @return The list of anchored {@link SyntaxNode syntax nodes}
     */
    public List<SyntaxNode> getAnchoredNodes() { 
        return anchoredNodes;
    }
    
    /**
     * Get the list of floating nodes in this fragment.
     *
     * @return The list of floating {@link SyntaxNode syntax nodes}
     */
    public List<SyntaxNode> getFloatingNodes () {
        return floatingNodes;
    }


    /**
     * Get the list of help topics associated with this syntax fragment.
     * @return Returns the help topic list of this fragment.
     */
    public String getHelpTopics() {
        return helpTopics;
    }
    
    /**
     * Sets the help topics associated with this syntax fragment.
     * @param helpTopics The help topic list to set for this fragment.
     */
    public void setHelpTopics(String helpTopics) {
        this.helpTopics = helpTopics;
    }
    
    /**
     * Get the first anchored node or null if there is not one.
     * @return The first anchored node; null if none.
     */
    public SyntaxNode getFirstAnchoredNode() {
        if (anchoredNodes.size() < 1)
            return null;
        SyntaxNode node = anchoredNodes.get(0);
        if (node instanceof SyntaxFragment)
            return ((SyntaxFragment) node).getFirstAnchoredNode();
        return node;
    }
    
    /**
     * Augments the given lists with "flattened" lists of anchored and
     * floating nodes, where SyntaxFragments are replaced by their constituent
     * SyntaxNode subclasses. Note that the anchored nodes are in parsing
     * order.
     *
     * @param anchored List into which anchored nodes should be added
     * @param floating List into which floating nodes should be added
     */
    public void getCanonicNodeLists(List<SyntaxNode> anchored, 
                                    List<SyntaxNode> floating) {
        Iterator<SyntaxNode> anchoredList = 
            (getAnchoredNodes() != null) ? getAnchoredNodes().iterator() : null;
        Iterator<SyntaxNode> floatingList = 
            (getFloatingNodes() != null) ? getFloatingNodes().iterator() : null;
        while (anchoredList != null && anchoredList.hasNext()) {
            SyntaxNode nextSubnode = anchoredList.next();
            if (nextSubnode instanceof SyntaxFragment) {
                SyntaxFragment sf = (SyntaxFragment) nextSubnode;
                if (sf.isFloating())
                    sf.getCanonicNodeLists(null, floating);
                else
                    sf.getCanonicNodeLists(anchored, floating);
            } else if (nextSubnode.isFloating() || anchored == null) {
                floating.add(nextSubnode);
            } else {
                anchored.add(nextSubnode);
            }
        }

        while (floatingList != null && floatingList.hasNext()) {
            SyntaxNode nextSubnode = floatingList.next();
            if (nextSubnode instanceof SyntaxFragment) {
                SyntaxFragment sf = (SyntaxFragment) nextSubnode;
                sf.getCanonicNodeLists(null, floating);
            } else {
                floating.add(nextSubnode);
            }
        }
    }


    /**
     * Returns true if the node matches the given arguments starting with the
     * specified position. If a match is found, the position will be updated
     * to the next unparsed argument. If no match is found the
     * <code>farthestMismatchPosition</code> parameter will hold the index of
     * the farthest argument that failed to match and the reference to the
     * syntax node against which that match failed.
     * 
     * @see org.opendaylight.util.syntax.SyntaxNode#matches
     */
    @Override
    public boolean matches(String args[], Parameters parameters, 
                           Parameters committedParameters,
                           ParsePosition position, ParsePosition start,
                           SyntaxPosition farthestMismatchPosition,
                           boolean indexParameters) {
        int oldStart = start.getIndex();
        int oldPosition = position.getIndex();

        //  How many occurrences of this fragment do we have to match?
        int maxOccurrences = getMaxOccurrences();
        int minOccurrences = getMinOccurrences();
        int occurrence = 0;
        ParsePosition localStart = new ParsePosition(0);
        while (occurrence < maxOccurrences && position.getIndex() < args.length) {
            localStart.setIndex(0);
            boolean ok = 
                matchesOnce(args, parameters, committedParameters, 
                            position, localStart, farthestMismatchPosition,
                            indexParameters || maxOccurrences > 1);
            if (!ok)
                break;
            occurrence++;
        }

        //  If we have a match upon reaching the minimum of required matches, 
        //  then we have a match.
        if (occurrence >= minOccurrences)
            return true;

        farthestMismatchPosition.update(position, this);
        position.setIndex(oldPosition);
        start.setIndex(oldStart);
        return false;
    }  

    
    /**
     * Returns true if the fragment matches once the given arguments starting
     * with the specified position. If a match is found, the position will be
     * updated to the next unparsed argument. If no match is found the
     * <code>farthestMismatchPosition</code> parameter will hold the index of
     * the farthest argument that failed to match and the reference to the
     * syntax node against which that match failed.
     * 
     * @param args arguments to be matched
     * @param parameters parameters tentatively parsed thus far
     * @param committedParameters parameters definitely parsed thus far
     * @param position current parse position
     * @param start starting parse position
     * @param farthestMismatchPosition farthest parse position where mismatch
     *        occurred
     * @param indexParameters true if parameters are to be indexed; false
     *        otherwise
     * @return true of the arguments match the syntax fragment
     * 
     * @see org.opendaylight.util.syntax.SyntaxNode#matches
     */
    protected boolean matchesOnce(String args[], Parameters parameters,
                                  Parameters committedParameters,
                                  ParsePosition position, ParsePosition start,
                                  SyntaxPosition farthestMismatchPosition,
                                  boolean indexParameters) {
        //  Let's force at least one anchored node to be preceding any
        //  floating ones.
        boolean firstAnchoredNodeProcessed = false;
        
        //  Let's remember the position at which we entered this match attempt.
        int oldPosition = position.getIndex();

        //  Let's not commit the parameters until we are dead sure we match
        //  all the way. As parameters match they will be placed into the
        //  newParameters object, but it is possible that several matches may
        //  occur before a mismatch is found. In such a case we want to
        //  discard the match up to that point so we keep the match in the
        //  newParameters object; if the match is complete we copy it to the
        //  parameters object later. If it is not a match then we don't do
        //  the copy and parameters does not have to have the non-matching
        //  list of parameters removed.
        Parameters newParameters = new Parameters();

        //  To enable the parsing of arguments into parameters which are
        //  dependent on other, previously parsed parameters we must pass a
        //  running tally of what has been parsed thus far down into the
        //  match() methods. This should include everything that has been
        //  committed prior to this method being called, and should have
        //  each parameter added to it as they are parsed through the life
        //  of this syntax. This will enable dependent-parameter parsing to
        //  happen. Like the other parameters, we only want to pass a
        //  modified copy back up to the caller once we're certain that the
        //  fragment syntax is a complete match.
        Parameters committedHere = new Parameters();
        committedHere.add(committedParameters);

        //  Let's not consider options to be the farthest matches, until we
        //  have processed all the anchored nodes.
        SyntaxPosition farthestOptionMismatch = new SyntaxPosition();
        farthestOptionMismatch.update(farthestMismatchPosition);

        //  How many anchored nodes is this fragment to have at least?
        int nodeCount = anchoredNodes != null ? anchoredNodes.size() : 0;

        while ((position.getIndex() >= 0 && position.getIndex() < args.length)
                || (start.getIndex() >= 0 && start.getIndex() < nodeCount)) {
            //  Does the the current argument match a floating syntax node?
            if (!firstAnchoredNodeProcessed
                    || !matchesFloater(args, newParameters, committedHere,
                                       position, farthestOptionMismatch,
                                       indexParameters)) {
                //  If not, then it must be part of an anchored argument usage.
                if (nodeCount <= start.getIndex()) {
                    //  This is not a root syntax and we matched thus far so
                    //  therefore let's bail with matching status.
                    //  We've matched all anchored nodes thus far, but now
                    //  we have an option mismatch, so let's make sure we
                    //  promote the farthest option mismatch.
                    farthestMismatchPosition.update(farthestOptionMismatch);
                    break;

                } else if (!matchesAnchor(args, newParameters, committedHere,
                                          position, start,
                                          farthestMismatchPosition,
                                          indexParameters)) {
                    //  If the arguments do not match the parameter at the
                    //  current parsing position, we don't have a match.
                    //  Therefore bail out with negative response.
                    farthestMismatchPosition.update(position, this);
                    position.setIndex(oldPosition);
                    return false;
                }
                firstAnchoredNodeProcessed = true;
            }
        }

        //  If we have a match upon processing all input nodes and there are
        //  no unparsed anchored nodes left in the node, then we have a
        //  match.
        if (nodeCount <= start.getIndex()) {
            //  Let's import the new parameters into the master map before we
            //  bail with OK status.
            parameters.add(newParameters);
            committedParameters.add(newParameters);
            return true;
        }
        position.setIndex(oldPosition);
        return false;
    }


    /**
     * Returns true if the currently parsed argument matches any floating,
     * non-positional syntax node. If a match is found, the position will be
     * updated to the next unparsed argument.
     * 
     * @param args arguments to be matched
     * @param parameters parameters tentatively parsed thus far
     * @param committedParameters parameters definitely parsed thus far
     * @param position current parse position
     * @param farthestMismatchPosition farthest parse position where mismatch
     *        occurred
     * @param indexParameters true if parameters are to be indexed; false
     *        otherwise
     * @return true of the arguments match the syntax fragment
     */
    protected boolean matchesFloater(String args[], Parameters parameters,
                                     Parameters committedParameters,
                                     ParsePosition position,
                                     SyntaxPosition farthestMismatchPosition,
                                     boolean indexParameters) {
        // Does this node have any floating nodes?
        if (floatingNodes == null)
            return false;
        
        //  Iterate over all floating non-positional nodes and see if one
        //  matches.  Since we loop in this method create a copy of the
        //  committed Parameters list which is initialized to the contents
        //  that were passed in; the new clone will be modified on iterations
        //  through this loop to include parameters that were committed during
        //  parsing of this floating node.  The final list of committed
        //  Parameters that ends up in newParameters will be added to the
        //  committed Parameters list we were passed before returning; this
        //  indirection protects the caller from Parameters that were
        //  committed prior to being backed out due to a syntax mismatch
        //  discovered after their addition.
        Parameters newParameters = new Parameters();
        Parameters committedHere = new Parameters();
        committedHere.add (committedParameters);

        Iterator<SyntaxNode> it = floatingNodes.iterator();
        while (it.hasNext() && 
               position.getIndex() >= 0 && position.getIndex() < args.length) {
            int oldPosition = position.getIndex();
            SyntaxNode node = it.next();
            newParameters.clear();

            ParsePosition start = new ParsePosition(0);
            if (node.matches(args, newParameters, committedHere,
                             position, start, farthestMismatchPosition,
                             indexParameters)) {
                parameters.add(newParameters);
                committedParameters.add(newParameters);
                return true;
            }
            position.setIndex(oldPosition);
        }
        return false;
    }
    
    
    /**
     * Returns true if the currently parsed argument matches the next anchored
     * positional syntax node. If a match is found, the position will be
     * updated to the next unparsed argument.
     * 
     * @param args arguments to be matched
     * @param parameters parameters tentatively parsed thus far
     * @param committedParameters parameters definitely parsed thus far
     * @param position current parse position
     * @param start starting parse position
     * @param farthestMismatchPosition farthest parse position where mismatch
     *        occurred
     * @param indexParameters true if parameters are to be indexed; false
     *        otherwise
     * @return true of the arguments match the syntax fragment
     */
    protected boolean matchesAnchor(String args[], Parameters parameters,
                                    Parameters committedParameters,
                                    ParsePosition position,
                                    ParsePosition start,
                                    SyntaxPosition farthestMismatchPosition,
                                    boolean indexParameters) {
        int i = start.getIndex();

        //  Are there any unparsed anchored nodes left?
        if (anchoredNodes == null || i >= anchoredNodes.size())
            return false; // If not, bail.

        //  If so, try to match the arguments to the next anchored node.
        SyntaxNode node = anchoredNodes.get(i);

        //  See if it matches from the current starting position.
        ParsePosition newStart = new ParsePosition(0);

        // Insert any matching parameters directly into the provided
        // Parameters list. Since there is no looping in this method (a
        // one-shot deal) it is not necessary to mask off a "private" copy of
        // the committed Parameters list either.
        if (node.matches(args, parameters, committedParameters, position,
                         newStart, farthestMismatchPosition, indexParameters)) {
            start.setIndex(i + 1);
            return true;
        }
        return false;
    }

    /**
     * Scans through all floating and anchored nodes of the node and if the
     * value of the parameter is not already in the map, it places an entry in
     * the map using the default parameter value of the, if one is specified.
     * 
     * @param parameters parameters with default values
     */
    public void applyDefaults(Parameters parameters) {
        if (anchoredNodes != null)
            applyDefaults(anchoredNodes.iterator(), parameters);
        if (floatingNodes != null)
            applyDefaults(floatingNodes.iterator(), parameters);
    }

    /**
     * Scans through the given enumeration of nodes and if the value of a
     * syntax parameter is not already in the map, it places an entry in the
     * map using the default parameter value of the, if one is specified.
     *
     * @param nodes iterator of nodes whose parameters are to be populated
     * @param parameters parameters with default values
     */
    private void applyDefaults(Iterator<SyntaxNode> nodes, 
                               Parameters parameters) {
        while (nodes.hasNext()) {
            SyntaxNode node = nodes.next();
            if (node instanceof SyntaxParameter) {
                SyntaxParameter parameter = (SyntaxParameter) node;
                //  If the node has default value and there is no value in the
                //  parameters map, put the default value in.  Make sure to
                //  consider the name of the node that references added
                //  parameters, namely this one, in the process to ensure that
                //  the defaults are correctly applied.
                if (parameter.getDefaultValue() != null) {
                    //  If this is an indexed value, let's make sure that
                    //  there was at least one value given.
                    String parameterName = parameter.getName();
                    boolean isIndexed = parameter.getMaxOccurrences() > 1;
                    Serializable o = isIndexed ?
                        parameters.get(parameterName, 0) : 
                        parameters.get(parameterName);
                    if (o == null) {
                        parameters.add(parameterName,
                                       parameter.getDefaultValue(),
                                       isIndexed);
                    }
                }
            } else if (node instanceof SyntaxFragment) {
                ((SyntaxFragment) node).applyDefaults(parameters);
            }
        }
    }

    /** 
     * Returns the string image of this syntax fragment.
     */
    @Override
    public String toString () {
        //  Enumerate over all anchored and floating nodes.
        Iterator<SyntaxNode> r = anchoredNodes != null ? anchoredNodes.iterator() : null;
        Iterator<SyntaxNode> o = floatingNodes != null ? floatingNodes.iterator() : null;
        
        //  Display the anchored nodes first...
        StringBuffer image = new StringBuffer();
        while (r != null && r.hasNext())
            image.append(" ").append(r.next());

        //  Then display all floating nodes...
        while (o != null && o.hasNext())
            image.append("\n\t[").append(o.next()).append("]");
        return image.toString().trim();
    }

}
