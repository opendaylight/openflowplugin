/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.cache;

/**
* Utility stop-watch class for unit tests.
*
* @author Simon Hunt
*/
class Watch {
    private final String label;
    private final long start;
    private long duration = -1;

    public Watch(String label) {
        this.label = label;
        start = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Test: \"").append(label)
                .append("\" ");
        if (duration == -1)
            sb.append("running...");
        else
            sb.append("ENDED: ").append(duration).append("ms");
        return sb.toString();
    }

    public String stop() {
        duration = System.currentTimeMillis() - start;
        return toString();
    }
}
