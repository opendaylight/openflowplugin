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
import java.util.Properties;

import org.opendaylight.util.syntax.parsers.Constraints;
import org.opendaylight.util.syntax.parsers.ConstraintsParser;
import org.opendaylight.util.syntax.parsers.ParameterParser;

/**
 * Class representing a variable syntax parameter.
 *
 *  @author Thomas Vachuska
 */
public class SyntaxParameter extends SyntaxNodeExtension {

    private static final long serialVersionUID = 2721591423927369679L;

    /** Parser for interpreting a string image of a parameter.  */
    private ParameterParser parser = null;

    /** Parser for interpreting a property db as parameter constraints.  */
    private ConstraintsParser constraintsParser = null;

    /** Parser for interpreting property db as a constraint specification.  */
    private transient Constraints constraints = null;

    /** Default parameter value, if any.  */
    private Serializable defaultValue = null;


    /** 
     * Default constructor.
     */
    protected SyntaxParameter() {
    }

    /**
     * Constructs a new syntax node from the data in the given property db.
     * 
     * @param node originating syntax node
     * @param parent parent syntax node
     * @param parserLoader parser loader to be used for constraints and
     *        parameter parsers
     */
    public SyntaxParameter(SyntaxNode node, SyntaxNodeExtension parent,
                           ParserLoader parserLoader) {
        super(node, parent);

        Properties db = getDB();

        //  Get the parser type of this node.
        String valueType = db.getProperty(KW_VALUE_TYPE);

        //  Was it specified?
        if (valueType != null) {
            // If so, get the parser for that value type.
            valueType = valueType.trim();
            parser = (ParameterParser) 
                parserLoader.getParser(valueType, ParameterParser.class);

        } else if (parent != null && parent instanceof SyntaxParameter) {
            //  If the parameter has a parent that is also a parameter, let's
            //  inherit its parser.
            parser = ((SyntaxParameter) parent).parser;
        }

        //  Otherwise default to string parameter parser.
        if (valueType == null && parser == null) 
            parser = (ParameterParser) 
                parserLoader.getParser("string", ParameterParser.class);

        //  By now, we must have a parser, otherwise we have a problem.
        if (parser == null)
            throw new BadSyntaxException("Unable to find parser for " + 
                                         valueType, node);

        // Get the constraints type of this node.
        valueType = parser.getTypeToken();
        String constraintsType = db.getProperty(KW_CONSTRAINTS_TYPE);

        // Was it specified?
        if (constraintsType != null) {
            // If so, get the constraints parser for that value type.
            constraintsType = constraintsType.trim();
            constraintsParser = (ConstraintsParser) 
                parserLoader.getParser(constraintsType, 
                                       ConstraintsParser.class);
        } else if (parent != null && parent instanceof SyntaxParameter) {
            // If the parameter has a parent that is also a parameter, let's
            // inherit its constraints (ConstraintsParser) parser.
            constraintsParser = ((SyntaxParameter) parent).constraintsParser;
        } else {
            // Otherwise default to the constraints parser associated with
            // the value type.
            constraintsParser = (ConstraintsParser) 
                parserLoader.getParser(valueType, ConstraintsParser.class);
        }

        //  By now, we must have a constraints parser, otherwise it's a problem.
        if (constraintsParser == null)
            throw new BadSyntaxException("Unable to find constraints parser " +
                                         "for " + constraintsType, node);

        //  Use the parser to obtain a constraints object using the properties
        //  of this node.  We will use this later to validate the parsed
        //  parameter values.
        setConstraints(constraintsParser.parse(db, this, parserLoader));

        //  If there were no constraints for this node, use the parent set of
        //  constraints if there is a parent.
        if (constraints == null && parent != null
            && parent instanceof SyntaxParameter) {
            setConstraints(((SyntaxParameter) parent).getConstraints());
        }

        //  Retrieve the default value string and convert it, using the
        //  parser, to the object form and using the constraint object for
        //  validation, although that may not be necessary or potentially not
        //  desired if for some reason we wanted the default value not to
        //  comply with the constraints.  For now, let's do it this way
        //  though.
        String defaultValueString = translate(db.getProperty(KW_DEFAULT));
        if (defaultValueString != null) {
            defaultValue = parser.parse(defaultValueString, getConstraints(),
                                        new Parameters());
        } else if (parent != null && parent instanceof SyntaxParameter) {
            defaultValue = ((SyntaxParameter) parent).getDefaultValue();
        }

        //  Disabled! If the description is null, see if we can inherit one.
        //  if (getDescription() == null && parent != null)
        //      setDescription(parent.getDescription());

        //  Use the short name of the parameter as a fall-back description.
        if (getDescription() == null)
            setDescription(getShortName());

        //  Apply the same for the help text.
        if (getHelpText() == null && parent != null)
            setHelpText(parent.getHelpText());
    }

    /**
     * Get the constraints object associated with this parameter.
     * 
     * @return effective constraints for values of this parameters
     */
    public Constraints getConstraints() {
        return constraints;
    }

    /**
     * Returns the parser associated with this syntax parameter.
     * 
     * @return constraints parser used for this parameter
     */
    public ConstraintsParser getConstraintsParser() {
        return constraintsParser;
    }

    /**
     * Returns the parser associated with this syntax parameter.
     * 
     * @param constraintsParser constraints parser to be used by this
     *        parameter
     */
    public void setConstraintsParser(ConstraintsParser constraintsParser) {
        this.constraintsParser = constraintsParser;
    }


    /**
     * Returns the parser associated with this syntax parameter.
     * 
     * @return parameter parser for values of this parameter
     */
    public ParameterParser getParser() {
        return parser;
    }

    /**
     * Sets the parser associated with this syntax parameter.
     * 
     * @param parser parameter parser for values of this parameter
     */
    public void setParser(ParameterParser parser) {
        this.parser = parser;
    }

    /**
     * Returns the constraints object associated with this parameter.
     * 
     * @param constraints new effective constraints for values of this
     *        parameter
     */
    public void setConstraints(Constraints constraints) {
        this.constraints = constraints;
    }

    /**
     * Returns the object representing the default value of the parameter.
     * 
     * @return default value for this parameter
     */
    public Serializable getDefaultValue() {
        return defaultValue;
    }

    /**
     * Returns true if the node matches the given arguments starting with the
     * specified position.  If a match is found, the position will be updated
     * to the next unparsed argument.  If no match is found the
     * <code>farthestMismatchPosition</code> parameter will hold the index of
     * the farthest argument that failed to match and the reference to the
     * syntax node against which that match failed.
     *
     * This base implementation always returns false.
     *
     * @param args Command-line argument strings
     * @param parameters Parameter map into which this matching attempt should
     * accumulate its (String, Serializable) bindings.
     * @param committedParameters Parameter map of (String, Serializable)
     * bindings that has been committed thus far through the entire syntax
     * matching process.
     * @param position Position within the args, where this matching
     * attempt should start.
     * @param start Position within the defined syntax, where this matching
     * attempt should start.
     * @param farthestMismatchPosition The farthest position where mismatch
     * occured within this matching attempt.
     */
    @Override
    public boolean matches(String args[], Parameters parameters, 
                           Parameters committedParameters,
                           ParsePosition position, ParsePosition start,
                           SyntaxPosition farthestMismatchPosition,
                           boolean indexParameters) {
        int occurrences = 0;
        int i = start.getIndex();
        int maxOccurrences = getMaxOccurrences();
        int minOccurrences = getMinOccurrences();
        boolean isIndexed = indexParameters || maxOccurrences > 1;

        for (int p = 0; p < maxOccurrences && position.getIndex() < args.length;
             p++) {
            int j = position.getIndex();

            //  Can the object be parsed and validated?
            Serializable parameter = parser.parse(args[j], constraints, 
                                                  committedParameters);
            if (parameter != null) {
                String name = getName();
                parameters.add(name, parameter, isIndexed);
                committedParameters.add(name, parameter, isIndexed);
                occurrences++;
                position.setIndex(j + 1);
            } else {
                farthestMismatchPosition.update(position, this);
                break;
            }
        }

        if (minOccurrences <= occurrences) {
            start.setIndex(i + 1);
            return true;
        }
        farthestMismatchPosition.update(position, this);
        return false;
    }
    
    @Override
    public String toString() {
        String string = super.toString();
        if (getMinOccurrences() == 1 && getMaxOccurrences() == 1)
            return string;
        else if (getMinOccurrences() == 0 && getMaxOccurrences() == 1)
            return "[" + string + "]";
        else {
            String lbr = getMinOccurrences() == 0 ? "[" : "{";
            String rbr = getMinOccurrences() == 0 ? "]" : "}";
            
            if (getMaxOccurrences() == Integer.MAX_VALUE)
                return lbr + string + "..." + rbr;
            return lbr + string + "x" + getMaxOccurrences() + rbr;
        }
    }

}
