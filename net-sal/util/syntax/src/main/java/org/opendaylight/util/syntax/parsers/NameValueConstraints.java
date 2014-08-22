/*
 * (c) Copyright 2004 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.util.syntax.parsers;

import java.io.Serializable;


/**
 * Builtin constraints validator for a name/value pair object.  

 *
 * @author Thomas Vachuska
 */
public class NameValueConstraints implements Constraints {
    
    protected StringConstraints nameConstraints;
    protected ParameterParser valueParser;
    protected Constraints valueConstraints;
    protected String separator;

    /** 
     * Default constructor.
     */
    public NameValueConstraints() {
    }
        
    /**
     * Creates a name/value pair constraints enforcement entity.
     * 
     * @param nameConstraints constraints to use for validating name portion
     * or null if none
     * @param valueParser parameter parser to use for parsing the value portion or null of none
     * @param valueConstraints constraints to use for validating value portion
     * or null if none
     * @param separator character used to separate the name from the value, if
     * null, "=" will be used as default.
     */
    public NameValueConstraints(StringConstraints nameConstraints,
                                ParameterParser valueParser,
                                Constraints valueConstraints,
                                String separator) {
        this.nameConstraints = nameConstraints;
        this.valueParser = valueParser;
        this.valueConstraints = valueConstraints;
        this.separator = separator != null ? separator : "="; 
    }

    /**
     * @return Returns the separator.
     */
    public String getSeparator() {
        return separator;
    }
    
    /**
     * @return Returns the nameConstraints.
     */
    public StringConstraints getNameConstraints() {
        return nameConstraints;
    }
    
    /**
     * @return Returns the valueParser.
     */
    public ParameterParser getValueParser() {
        return valueParser;
    }
    
    /**
     * @return Returns the valueConstraints.
     */
    public Constraints getValueConstraints() {
        return valueConstraints;
    }
    
    /**
     * @see Constraints#isValid
     */
    @Override
    public boolean isValid(Serializable object) {
        return false;
    }

}
