/*
 * (c) Copyright 2001-2002 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.syntax;

import java.util.Locale;
import java.util.Properties;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Comparator;

import org.opendaylight.util.syntax.parsers.TypedParser;

/**
 * Class representing a package of syntax definitions. This class is not
 * thread-safe and relies on external synchronization, should its instances be
 * used by multiple simultaneous threads.
 * 
 * @author Thomas Vachuska
 */
public class SyntaxPackage extends SyntaxNode implements ParserLoader {

    private static final long serialVersionUID = 1825926030149946316L;

    /** Prioritized list of root syntax nodes in this package. */
    private List<Syntax> syntaxes = new ArrayList<Syntax>();

    /**
     * Flag indicating whether or not the list of syntaxes has been
     * prioritized yet or not.
     */
    private boolean isPrioritized = true;
    
    /** Comparator for sorting syntaxes based on their priority. */
    private Comparator<Syntax> prioritizer = new SyntaxPrioritizer();

    /** Name-to-syntax node bindings. */
    private Map<String, SyntaxNode> nodes = new HashMap<String, SyntaxNode>();

    /** Type Token-to-parser bindings defined in the scope of this package. */
    private Map<Class<?>, Map<String, TypedParser>> parserPools = new HashMap<Class<?>, Map<String, TypedParser>>();

    /**
     * Default constructor.
     */
    protected SyntaxPackage() {
    }

    /**
     * Constructs a new syntax package using the given properties.
     * 
     * @param db Properties containing definition of this node.
     * @param locale Locale context for this node's attributes.
     */
    public SyntaxPackage(Properties db, Locale locale) {
        super(null, db, locale);
        setResolved();
    }

    /**
     * Returns the parser with the specified name and from the specified
     * intefrace pool.
     * 
     * @param name Type token associated with this parser.
     * @param parserInterface
     *        {@link org.opendaylight.util.syntax.parsers.ConstraintsParser} or
     *        {@link org.opendaylight.util.syntax.parsers.ParameterParser} class object
     *        that identifies the general interface pool.
     * @return Concrete instance that implements the specified interface.
     */
    @Override
    public TypedParser getParser(String name, Class<?> parserInterface) {
        // Do we have a pool for this class of a parser yet?
        Map<String, TypedParser> parserPool = parserPools.get(parserInterface);
        if (parserPool != null)
            return parserPool.get(name);
        return null;
    }

    /**
     * Registers the specified parser, using its type token, in the pool of
     * parsers that implement the specified interface.
     * 
     * @param parser Class that implements the given parser interface and
     *        should therefore be added to the corresponding pool
     * @param parserInterface
     *        {@link org.opendaylight.util.syntax.parsers.ConstraintsParser} or
     *        {@link org.opendaylight.util.syntax.parsers.ParameterParser} class object
     *        that identifies the general interface pool.
     */
    @Override
    public void addParser(TypedParser parser, Class<?> parserInterface) {
        // Do we have a pool for this interface class yet?
        Map<String, TypedParser> parserPool = parserPools.get(parserInterface);
        if (parserPool == null) {
            // If not create the pool and add it to the list of pools.
            parserPool = new HashMap<String, TypedParser>();
            parserPools.put(parserInterface, parserPool);
        }

        // Now add the parser to the pool using its type token as the key.
        parserPool.put(parser.getTypeToken(), parser);
    }

    /**
     * Get prioritized list of syntaxes (instances of {@link Syntax}) defined
     * within this package.
     * 
     * @return List of syntaxes ordered by their priority.
     */
    public List<Syntax> getSyntaxes() {
        if (!isPrioritized) {
            Collections.sort(syntaxes, prioritizer);
            isPrioritized = true;
        }
        return Collections.unmodifiableList(syntaxes);
    }

    /**
     * Get a map of name/node bindings for all syntax nodes defined in this
     * package.
     * 
     * @return Map of name/syntax node bindings.
     */
    public Map<String, SyntaxNode> getNodes() {
        return Collections.unmodifiableMap(nodes);
    }

    /**
     * Adds the given node to the syntax package. If the node is
     * {@link Syntax} then it will also be added, according to its priority to
     * the ordered list of syntaxes.
     * 
     * @param node Syntax node to be added to this package.
     */
    public void addNode(SyntaxNode node) {
        if (node != null) {
            nodes.put(node.getName(), node);
            if (node instanceof Syntax) {
                syntaxes.add((Syntax) node);
                isPrioritized = false;
            }
        }
    }

    /**
     * Get the named node using its fully qualified node name.
     * 
     * @param name Fully qualified node name.
     * @return Syntax node instance with the specified name; null if there is
     *         no node registered using that name.
     */
    public SyntaxNode getNode(String name) {
        return nodes.get(name);
    }

}
