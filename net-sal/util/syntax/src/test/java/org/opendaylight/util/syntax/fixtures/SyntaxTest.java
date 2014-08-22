/*
 * (c) Copyright 2004 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.util.syntax.fixtures;

import org.opendaylight.util.syntax.Parameters;

import org.junit.Test;


/**
 * Entity representing a single  command-line syntax test case.
 * 
 * @author Thomas Vachuska
 */
public class SyntaxTest {

    private String name = null;
    private String args[] = null;
    private String syntaxName = null;
    private String actionName = null;
    private int errorIndex = -1;
    private Parameters parameters = null;
    
    /**
     * @return Returns the syntaxName.
     */
    public String getSyntaxName() {
        return syntaxName;
    }
    /**
     * @param syntaxName The syntaxName to set.
     */
    public void setSyntaxName(String syntaxName) {
        this.syntaxName = syntaxName;
    }
    /**
     * @return Returns the actionName.
     */
    public String getActionName() {
        return actionName;
    }
    /**
     * @param actionName The actionName to set.
     */
    public void setActionName(String actionName) {
        this.actionName = actionName;
    }
    /**
     * @return Returns the args.
     */
    public String[] getArgs() {
        return args;
    }
    /**
     * @param args The args to set.
     */
    public void setArgs(String[] args) {
        this.args = args;
    }
    /**
     * @return Returns the errorIndex.
     */
    public int getErrorIndex() {
        return errorIndex;
    }
    /**
     * @param errorIndex The errorIndex to set.
     */
    public void setErrorIndex(int errorIndex) {
        this.errorIndex = errorIndex;
    }
    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }
    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * @return Returns the parameters.
     */
    public Parameters getParameters() {
        return parameters;
    }
    /**
     * @param parameters The parameters to set.
     */
    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    @Test
    public void testNothing() {}
    
}
