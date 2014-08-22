/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.mockswitch;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Concrete class for a delay command.
 *
 * @author Simon Hunt
 */
public class CmdDelay extends ScenarioCommand {

    private static final Pattern RE_TIME = Pattern.compile("(\\d+)(s|ms)");

    private final int msDelay;

    /** Constructs the delay command.
     *
     * @param type the command type
     * @param params the parameters
     */
    public CmdDelay(CommandType type, String params) {
        super(type, params);
        Matcher m = RE_TIME.matcher(params);
        if (m.matches()) {
            String numStr = m.group(1);
            boolean secs = m.group(2).equals("s");
            int num = 0;
            try {
                num = Integer.parseInt(numStr);
                if (num < 0)
                    error = "Negative number: " + numStr;
                else
                    num = secs ? num * 1000 : num;
            } catch (NumberFormatException nfe) {
                error = "Bad number: " + numStr;
            }
            msDelay = num;
        } else {
            msDelay = 0;
            error = "Time not \"nnn{s|ms}\": " + params;
        }
    }

    /** Returns the delay in milliseconds.
     *
     * @return the milliseconds delay
     */
    public int getMsDelay() {
        return msDelay;
    }
}
