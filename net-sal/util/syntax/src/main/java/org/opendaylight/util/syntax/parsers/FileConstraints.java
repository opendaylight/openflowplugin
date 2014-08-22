/*
 * (c) Copyright 2001 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.syntax.parsers;

import java.io.Serializable;
import java.io.File;

/** 
 * Builtin constraint validator for a file value.  
 *
 * @author Thomas Vachuska
 */
public class FileConstraints implements Constraints {
        
    protected String flags = null;
        
    /** 
     * Default constructor.
     */
    public FileConstraints() {
        this.flags = "";
    }

    /**
     * Constructs a new file parameter constraint using the given flags.
     *
     * @param flags String containing one or more of the following flags:
     * <ul>
     * <li><b>f</b> - must be a file (and not a directory)
     * <li><b>d</b> - must be a directory
     * <li><b>e</b> - must already exist
     * <li><b>r</b> - must be readable
     * <li><b>w</b> - must be writeable
     * </ul>
     */
    public FileConstraints(String flags) {
        this.flags = flags;
    }

    /**
     * @see Constraints#isValid
     */
    @Override
    public boolean isValid(Serializable o) {
        if (o != null && o instanceof File) {
            File f = (File) o;
            return (((flags.indexOf("d") >= 0 && f.isDirectory()) ||
                     (flags.indexOf("f") >= 0 && f.isFile()))
                    &&
                    (flags.indexOf("e") < 0 || f.exists()) &&
                    (flags.indexOf("r") < 0 || f.canRead()) &&
                    (flags.indexOf("w") < 0 || f.canWrite()));
        }
        return false;
    }

    /**
     * Get the file constraint flags.
     * 
     * @return current file flags constraints
     * @see FileConstraints#FileConstraints(String)
     */
    public String getFlags() {
        return flags;
    }
        
}
    
