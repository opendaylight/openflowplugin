/*
 * (c) Copyright 2010 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.util.syntax;

import java.io.Serializable;
import java.util.Comparator;


/**
 * Auxiliary comparator for Syntax instances.
 * 
 * @author Thomas Vachuska
 */
class SyntaxPrioritizer implements Comparator<Syntax>, Serializable {
    
    private static final long serialVersionUID = -4740924156954635218L;

    @Override
    public int compare(Syntax a, Syntax b) {
        return a.getPriority() - b.getPriority();
    }

}
