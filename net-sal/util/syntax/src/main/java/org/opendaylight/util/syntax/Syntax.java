/*
 * (c) Copyright 2001-2002 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.syntax;

import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opendaylight.util.syntax.parsers.StringSet;
import org.opendaylight.util.syntax.parsers.Utilities;

/** 
 * Class representing a root syntax node, i.e. one that references
 * other syntax nodes via it's own usage string and has a symbolic
 * action associated with it, which makes it a top-level syntax node.
 *
 * @author Thomas Vachuska 
 */
public class Syntax extends SyntaxFragment {

    private static final long serialVersionUID = 5028812558599013415L;

    /** Integer value that specifies the priority of the syntax; lower number
        means higher priority.  */
    private int priority = Integer.MAX_VALUE;

    /** Symbolic action name associated with this syntax pattern.  */
    private String actionName = null;

    /** Flag indicating that the syntax is purely auxiliary and hence it
        should never be considered as the closest matching syntax in case of a
        complete mismatch. Syntax can be designated as an auxiliary one, by
        setting its priority to -1 in its property DB.  */
    private boolean isAuxiliary = false;

    /** 
     * Default constructor.
     */
    protected Syntax() {
    }


    /**
     * Constructs a new syntax node.
     * 
     * @param node originating node
     * @param parent parent node
     * @param anchoredNodes list of anchored nodes
     * @param floatingNodes list of floating nodes
     * @param actionName action name
     */
    public Syntax(SyntaxNode node, SyntaxNodeExtension parent,
                  List<SyntaxNode> anchoredNodes, 
                  List<SyntaxNode> floatingNodes, String actionName) {
        super(node, parent, anchoredNodes, floatingNodes);
        this.actionName = actionName;

        //  Fetch the priority of this syntax and as a fallback, use the
        //  complexity of the syntax to determine its priority, vie the number
        //  of all nodes anchored within this syntax.
        this.priority = Utilities.get(getDB(), KW_PRIORITY, priority);
        if (priority == Integer.MAX_VALUE)
            this.priority = anchoredNodes.size();
        else if (priority < 0) {
            //  Let the user ask for minimum priority by specifying priority
            //  less than 0.
            this.priority = Integer.MAX_VALUE;
            this.isAuxiliary = true;
        }
    }

    /**
     * Get the priority of the syntax, which determines the order in which
     * this syntax will be considered relative to other syntax entities.
     *
     * @return Integer value that specifies the priority of the syntax; lower
     * number means higher priority.
     */
    public int getPriority() { 
        return priority; 
    }
    
    /**
     * Set the priority of the syntax, which determines the order in which
     * this syntax will be considered relative to other syntax entities.
     * 
     * @param priority Integer value that specifies the priority of the
     * syntax; lower number means higher priority.
     */
    protected void setPriority(int priority) {
        this.priority = priority;
    }

    /** 
     * Get the symbolic action name associated with this syntax pattern. 
     * 
     * @return syntax action name
     */
    public String getActionName() {
        return actionName; 
    }

    /** 
     * Set the symbolic action name associated with this syntax pattern.
     * 
     * @param actionName new syntax action name
     */
    protected void setActionName(String actionName) {
        this.actionName = actionName; 
    }

    /**
     * Returns true if the syntax is purely auxiliary and hence it should
     * never be considered as the closest matching syntax in case of a
     * complete mismatch. Syntax can be designated as an auxiliary one, by
     * setting its priority to -1 in its property DB.
     * 
     * @return true if the syntax is to be considered auxiliary
     */
    public boolean isAuxiliary() { 
        return isAuxiliary; 
    }

    /**
     * Returns true if the node matches the given arguments starting with the
     * specified position.  If a match is found, the position will be updated
     * to the next unparsed argument.  If no match is found the
     * <code>farthestMismatchPosition</code> parameter will hold the index of
     * the farthest argument that failed to match and the reference to the
     * syntax node against which that match failed.
     *
     * @see org.opendaylight.util.syntax.SyntaxNode#matches
     *
     */
    @Override
    public boolean matches(String args[], Parameters parameters, 
                           Parameters committedParameters,
                           ParsePosition position, ParsePosition start,
                           SyntaxPosition farthestMismatchPosition,
                           boolean indexParameters) {
        boolean isMatch = false;
        
        if (!isAuxiliary() && 
            super.matches(args, parameters, committedParameters,
                          position, start, farthestMismatchPosition, 
                          indexParameters)) {

            //  We have a match, but were all the arguments parsed?
            if (position.getIndex() == args.length) {
                //  Before we return the node, let's make sure that the
                //  parameter map is primed with all floating parameters
                //  with defaults.
                applyDefaults(parameters);
                isMatch = true;
            } else {
                //  The arguments specified were not all parsed!  Too bad; the
                //  syntax matched up until the extra argument(s).  There may
                //  be another syntax that matches this one but with more
                //  arguments; we don't want to stop searching but do want to
                //  record the fact that this was the closest miss to date!
                //
                //  Suggest a "closest" parameter or keyword that the extra
                //  argument might have been trying to match.  If there are no
                //  canonical floating nodes for the syntax then suggest the
                //  last canonical anchored node.  If there are floating nodes
                //  then walk the canonical list of them, looking for the
                //  first that wasn't matched to suggest.
                int i;
                SyntaxNode closestNode = null;
                List<SyntaxNode> anchoredCanonic = new ArrayList<SyntaxNode>();
                List<SyntaxNode> floatingCanonic = new ArrayList<SyntaxNode>();
                getCanonicNodeLists(anchoredCanonic, floatingCanonic);
                if (floatingCanonic.isEmpty()) {
                    closestNode = anchoredCanonic.get(anchoredCanonic.size() - 1);
                } else {
                    boolean matches = true;
                    Iterator<SyntaxNode> floatingList = floatingCanonic.iterator();
                    while (floatingList.hasNext()) {
                        closestNode = floatingList.next();
                        if (closestNode instanceof SyntaxKeyword) {
                            StringSet ss = 
                                ((SyntaxKeyword) closestNode).getTokens();
                            matches = false;
                            for (i = 0; i < args.length; i ++)
                                matches = matches || ss.contains(args[i]);
                        } else if (closestNode instanceof SyntaxParameter) {
                            matches = parameters.
                                getOccurrences(closestNode.getName()) != 0;
                        }
                        if (!matches)
                            break;
                    }
                }
                farthestMismatchPosition.update(position, closestNode, true);
            }
        }
        return isMatch;
    }

}
