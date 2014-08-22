/*
 * (c) Copyright 2001 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.syntax;

/**
 * Null interface to hold various keyword definitions.
 *
 * @author Thomas Vachuska
 */
public abstract interface SyntaxKeywords {

    /** Keyword denoting the node or property name; it should generally follow
        the Java identifier conventions. */
    public static final String KW_NAME = "name";

    /** Keyword whose value names the resource bundle that should be used to
        resolve any local-specific values; value tokens prefixed with '%' do
        not represent the actual value, but rather are interpretted as the
        key, using which to obtain the actual value from the resource bundle.
        For example '%verbose' means that a property named 'verbose' would be
        looked up in the resource bundle and its value would be used in place
        of the '%verbose' token.
        <p>
        If not present, the node will inherit its resource bundle from its
        defining container and if that fails from the node which it extends,
        if any.  */
    public static final String KW_RESOURCES = "resources";

    /** Value of this true/false property specifies whether the defining node
        is an anchored/positional one, or a floating/non-positional one.  If
        not present, the node will inherit the value of this property from the
        parent node, i.e. the one it extends, if any.  Default value is false,
        which means the node is an anchored one by default.  */
    public static final String KW_FLOATING = "floating";

    /** The value of this property specifies the name of the node from which
        this node should inherit any of its properties.  The name can be
        either the fully qualified name, such as 'common.verbosity' or the
        short one 'verbosity'.  If the fully qualified name is not specified,
        the parser will look for a definition within the neares encapsulating
        scope all the way to the package scope.  Therefore, references to
        nodes in other packages must always be fully qualified.  */
    public static final String KW_EXTENDS = "extends";

    /** This symbolic syntax node reference, when encountered in the usage
        string of a syntax fragment, will cause the defining fragment to
        inherit all of the floating syntax nodes contained in the parent's
        usage list. Applies only within the context of the usage property
        value.  */
    public static final String KW_SUPER = "super";

    /** Keyword whose value contains a space-separated list of literal or
        resource reference keyword definitions, and references to other syntax
        nodes.  References to all anchored nodes must be specified in the
        order in which they are anticipated on the command line.  The ordering
        of floating nodes is not considered.  If a node has this property, it
        becomes a syntax fragment. */
    public static final String KW_USAGE = "usage";

    /** Specifies the priority of the root syntax definitions. Lower number
        means higher priority.  Attempts to match arguments atr made on root
        syntax nodes, in the order of their priority. Value 0, thus makes the
        node an optional one, which means that if the parameter fails parsing
        and validation, the parser moves onto the next node without
        objections.  */
    public static final String KW_PRIORITY = "priority";

    /** The value of this fragment property indicates whether the fragment is 
        optional or not.  If it is, it forces min occurrences to 0 and 
        max occurrences to 1. If this property is specified as true, the 
        min and max occurrences properties will be ignored.  */
    public static final String OPTIONAL = "optional";

    /** The value of this fragment property, indicates the minimum
        number of times this parameter must be present.  Defaults to value 1,
        i.e. the parameter must occur at least once. Value of -1, translates
        to Integer.MAX_VALUE and therefore (for all practical purposes) to
        unlimited number of occurrences.  */
    public static final String MIN_OCCURRENCES = "minOccurrences";

    /** The value of this fragment property, indicates the maximum
        number of times this parameter can be specified in a row.  Defaults to
        value 1, i.e. the parameter must occur at most once. Value of -1,
        translates to Integer.MAX_VALUE and therefore (for all practical
        purposes) to unlimited number of occurrences.  */
    public static final String MAX_OCCURRENCES = "maxOccurrences";

    /** Value of this property specifies a brief, usually a one-word
        descriptive term for the node. This term is used as part of the usage
        string.  If absent, the short name of the node is taken as its
        description instead, although in that case, is not a localizable
        entity.  */
    public static final String KW_DESCRIPTION = "description";

    /** Value of this property specifies a comma-separated list of single-word 
        help topics. If absent, the string image of the first fixed node 
        parameter is taken instead.  */
    public static final String KW_HELP_TOPICS = "helpTopics";

    /** Keyword property whose value contains a long descriptive text
        for a node.  For root syntax nodes, this text is shown
        immediatelly following the usage string.  For parameter nodes
        and syntax fragments, it is shown in a descriptive list in the
        following form:
        <pre>    description           verbose help text (possibly multi-line)
        </pre> */
    public static final String KW_HELP_TEXT = "helpText";

    /** Value of this keyword property specifies the name of the symbolic
        action. A syntax fragment node that contain this property, becomes a
        root syntax node. How this is used really depends on the mechanism
        which uses the syntax parser.  It could be a simple abstract name or,
        it could be a string specifying the JNDI URI to a particular EJB.  */
    public static final String KW_ACTION_NAME = "actionName";

    /** The value of this optional parameter node-specific property, is a type
        token that indicates which parameter parser should be used to convert
        the argument to the java object.  Type tokens of builtin parameter
        parsers are as follows:
        <li>'string' - parses arg into a String object
        <li>'strings' - parses arg into a Vector of String objects
        <li>'number' - parses arg into a Number object
        <li>'numbers' - parses arg into a Vector of Number objects
        <li>'date' - parses arg into a Date instance
        <li>'dates' - parses arg into a Vector of Date objects
        <li>'file' - parses arg into a File object

        Other parsers can be registered via the KW_VALUE_TYPES
        property. Defaults to 'string' if absent. */
    public static final String KW_VALUE_TYPE = "parser";

    /** This optional parameter node property is similar to the KW_VALUE_TYPE
        property.  It allows one to specify a constraints parser that is
        separate from a parameter parser.  A 'string' value counterpart could
        be a parser that generates a 
        {@link org.opendaylight.util.syntax.parsers.Constraints} implementation which
        allows for validating strings against an ERE pattern for example. If
        absent, value defaults to the value of the KW_VALUE_TYPE property.  */
    public static final String KW_CONSTRAINTS_TYPE = "constraints";

    /** The value of this optional parameter-specific property will be parsed
        into a java object using that parameter's value parser during initial
        load.  That value will serve as the default value in case the
        parameter was an optional one and whose counterpart argument was
        ommited during parsing.  */
    public static final String KW_DEFAULT = "default";

    /** Keyword identifying package element.  */
    public static final String KW_PACKAGE = "package";

    /** Keyword identifying syntax element.  */
    public static final String KW_SYNTAX = "syntax";

    /** Keyword identifying parameter element.  */
    public static final String KW_PARAMETER = "parameter";

    /** Keyword identifying property element.  */
    public static final String KW_PROPERTY = "property";

    /** This package-level property allows one to extend the parameter parsing
        framework by providing a comma separated list of parameter parser
        class names.  */
    public static final String KW_PARAMETER_PARSER = "parameterParser";

    /** This package-level property allows one to extend the parameter
        validating framework by providing a comma separated list of
        constraints parser class names.  */
    public static final String KW_CONSTRAINTS_PARSER = "constraintsParser";

    /** Keyword identifying parser class attribute.  */
    public static final String KW_CLASS = "class";

    /** Keyword denoting the property value attribute. */
    public static final String KW_VALUE = "value";

}
