/*
 * (c) Copyright 2001-2002 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.syntax;

import java.io.Serializable;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.util.Properties;
import java.text.ParsePosition;

import org.opendaylight.util.format.TokenTranslator;
import org.opendaylight.util.syntax.parsers.StringSet;

/**
 * Class representing a generic node in a syntax tree which has not yet been
 * fully resolved.
 * 
 * @author Thomas Vachuska
 */
public class SyntaxNode implements Serializable, TokenTranslator,
        SyntaxKeywords {

    private static final long serialVersionUID = 1372452063599255318L;

    public static final String RESOURCE_PREFIX = "%";

    public static final String VALUE_DELIMITER = StringSet.VALUE_DELIMITER;

    public static final String ABBREV_DELIMITER = StringSet.ABBREV_DELIMITER;

    /** True if the node's class has been resolved. */
    private boolean isResolved = false;

    /**
     * Subclass of SyntaxNode into which this node should eventually resolve.
     */
    private Class<?> classHint;

    /** Name of this syntax element. */
    private String name;

    /** Syntax element that contains the definition of this node. */
    private SyntaxNode container = null;

    /** Property database associated with this syntax element. */
    private Properties db = null;

    /**
     * Resource bundle used for resolving localizable tokens specified in the
     * nodes defined within this syntax node.
     */
    private transient ResourceBundle res = null;

    /** Flag to indicate whether the node is floating or anchored. */
    private boolean isFloating = false;

    /** Brief description of this node. */
    private String description = null;

    /** Help text associated with this node. */
    private String helpText = null;

    /** Locale with which this node is associated. */
    private Locale locale = null;

    /**
     * Default constructor.
     */
    protected SyntaxNode() {
    }

    /**
     * Constructs a new syntax node from the data in the given properties.
     * 
     * @param container Syntax node which contains this node.
     * @param db Properties containing definition of this node.
     * @param locale Locale context for this node's attributes.
     * @param classHint Subclass of SyntaxNode into which this instance should
     *        eventually resolve.
     */
    public SyntaxNode(SyntaxNode container, Properties db, Locale locale,
                      Class<?> classHint) {
        this(container, db, locale);
        this.classHint = classHint;
    }

    /**
     * Constructs a new syntax node from the data in the given properties.
     * 
     * @param container Syntax node which contains this node.
     * @param db Properties containing definition of this node.
     * @param locale Locale context for this node's attributes.
     */
    protected SyntaxNode(SyntaxNode container, Properties db, Locale locale) {
        this.container = container;
        this.db = db;
        this.locale = locale;
        this.classHint = getClass();

        String localName = db != null ? db.getProperty(KW_NAME) : "";
        if (localName == null)
            throw error("Node must have a name.", this);

        if (container != null && db != null)
            setName(container.getName() + "." + localName);
        else if (db != null)
            setName(localName);

        if (db != null) {
            // Load the resource bundle, if one was specified.
            String resName = db.getProperty(KW_RESOURCES);
            if (resName != null) {
                resName = resName.trim();
                try {
                    res = ResourceBundle.getBundle(resName, locale);
                } catch (MissingResourceException mre) {
                    throw error("Missing resource bundle for " + this + ": "
                            + mre, this);
                }
            }
        }
    }

    /**
     * Constructs a new syntax node using data from another one.
     * 
     * @param node The original unresolved node from which to construct this
     *        fully resolved node.
     */
    protected SyntaxNode(SyntaxNode node) {
        // First make sure that the actual class is equal to or is a subclass
        // of the remembered hint from the original node.
        if (node.classHint.isAssignableFrom(getClass())) {
            this.isResolved = true;
            setName(node.name);
            this.container = node.container;
            this.db = node.db;
            this.locale = node.locale;
            this.isFloating = node.isFloating;
            this.classHint = getClass();
            setResourceBundle(node.res);
            description = translate(db.getProperty(KW_DESCRIPTION));
            setHelpText(translate(db.getProperty(KW_HELP_TEXT)));
        } else {
            throw error("Unable to convert node " + node + " into " +
                        node.classHint.getName(), node);
        }
    }

    /**
     * Returns the name of this syntax element.
     * 
     * @return name of the syntax element
     */
    public String getName() {
        return name;
    }

    /**
     * Allows the derived classes to override the name.
     * 
     * @param name new syntax element name
     */
    protected void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the short name of this syntax element.
     * 
     * @return short name of the syntax element
     */
    public String getShortName() {
        return getName().substring(getName().lastIndexOf(".") + 1);
    }

    /**
     * Returns the node, which contains the definition of this one.
     * 
     * @return parent node containing the definition of this element
     */
    public SyntaxNode getContainer() {
        return container;
    }

    /**
     * Returns the property database from which this element was created.
     * 
     * @return properties of this syntax element
     */
    public Properties getDB() {
        return db;
    }

    /**
     * Returns the resource bundle defined for this syntax set.
     * 
     * @return resource bundle associated with this syntax element
     */
    public ResourceBundle getResourceBundle() {
        return res;
    }

    /**
     * Allows the derived classes to override the resource bundle.
     * 
     * @param res new resource bundle for this syntax node
     */
    protected void setResourceBundle(ResourceBundle res) {
        this.res = res;
    }

    /**
     * Returns true if this node is floating, i.e. non-positional and false
     * otherwise.
     * 
     * @return true if this node is to be considered floating
     */
    public boolean isFloating() {
        return isFloating;
    }

    /**
     * Allows the derived classes to override the floating flag.
     * 
     * @param yesNo if true this node will be considered floating
     */
    protected void setFloating(boolean yesNo) {
        this.isFloating = yesNo;
    }

    /**
     * Returns the locale with which this node is associated.
     * 
     * @return locale of this node
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Returns the description of this syntax node.
     * @return description of this node
     */
    public String getDescription() {
        return description;
    }

    /**
     * Allows the derived classes to override the description.
     * 
     * @param description new description of this node
     */
    protected void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the help text associated with this syntax node.
     * 
     * @return help text for this node
     */
    public String getHelpText() {
        return helpText;
    }

    /**
     * Allows the derived classes to override the help text.
     * 
     * @param helpText new help text for this node
     */
    protected void setHelpText(String helpText) {
        this.helpText = helpText;
    }

    /**
     * Returns true if this node has already been resolved.
     * 
     * @return true if node is resolved
     */
    boolean isResolved() {
        return isResolved;
    }

    /**
     * Marks this node as resolved.
     */
    void setResolved() {
        isResolved = true;
    }

    /**
     * Translates, if neccessary, the specified token name, using the resource
     * bundle associated with this node. Translation is assumed to be required
     * if the string starts with the string specified by the
     * {@link SyntaxNode#RESOURCE_PREFIX} constant. This prefix is stripped
     * from the string and then the translation is attempted.
     * 
     * @see org.opendaylight.util.format.TokenTranslator#translate
     * @param string Token to be translated, if neccessary
     * @return Translated value, or the token itself if no translation was
     *         required.
     */
    @Override
    public String translate(String string) {
        String translation = string;
        if (string != null && string.startsWith(RESOURCE_PREFIX)) {
            String resource = string.substring(RESOURCE_PREFIX.length()).trim();
            ResourceBundle r = getResourceBundle();
            if (r != null) {
                try {
                    translation = r.getString(resource);
                } catch (MissingResourceException mre) {
                    throw error("No resource " + resource + " in bundle for "
                            + this + ": " + mre, this);
                }
            }
        }
        return translation;
    }

    /**
     * Returns true if the node matches the given arguments starting with the
     * specified position. If a match is found, the position will be updated
     * to the next unparsed argument. If no match is found the
     * <code>farthestMismatchPosition</code> parameter will hold the index of
     * the farthest argument that failed to match and the reference to the
     * syntax node against which that match failed. This base implementation
     * always returns false.
     * 
     * @param args Command-line argument strings
     * @param parameters Parameter map into which this matching attempt should
     *        accumulate its (String, Serializable) bindings.
     * @param committedParameters Parameter map of (String, Serializable)
     *        bindings that has been committed thus far through the entire
     *        syntax matching process.
     * @param position Position within the args, where this matching attempt
     *        should start.
     * @param start Position within the defined syntax, where this matching
     *        attempt should start.
     * @param farthestMismatchPosition The farthest position where mismatch
     *        occured within this matching attempt.
     * @param indexParameters flag indicating whether parameters contained
     *        within this node should be added as indexed.
     * @return true if the arguments matched this syntax node; false otherwise
     */
    public boolean matches(String args[], Parameters parameters,
                           Parameters committedParameters,
                           ParsePosition position, ParsePosition start,
                           SyntaxPosition farthestMismatchPosition,
                           boolean indexParameters) {
        return false;
    }

    /**
     * Returns a simple string image of the syntax node.
     */
    @Override
    public String toString() {
        return description != null ? description : getShortName();
    }

    /**
     * Creates an instance of {@link BadSyntaxException} from the given data.
     * 
     * @param message message string for the exception
     * @param node context syntax node
     * @return new bad syntax exception
     */
    protected BadSyntaxException error(String message, SyntaxNode node) {
        return new BadSyntaxException(message, node);
    }

}
