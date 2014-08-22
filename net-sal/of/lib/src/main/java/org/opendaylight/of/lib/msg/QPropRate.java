/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

/**
 * Abstract superclass of the {@link QPropMinRate} and {@link QPropMaxRate}
 * properties.
 *
 * @author Simon Hunt
 */
public abstract class QPropRate extends QueueProperty {

    private static final int PERCENT_100 = 1000;

    /** Rate, in 1/10 of a percent; >1000 -> disabled. */
    int rate;

    /** Constructor invoked by QueueFactory.
     *
     * @param header the property header
     */
    QPropRate(Header header) {
        super(header);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int len = sb.length();
        sb.replace(len-1, len, ",rate=")
                .append(rate).append("}");
        return sb.toString();
    }

    /** Returns true if this rate property designates "disabled".
     *
     * @return true, if this rate property is disabled
     */
    public boolean isDisabled() {
        return rate > PERCENT_100;
    }

    // TODO : review - should we "mask" values >1000, or leave as is?

    /** Returns the rate of this property, in 1/10 of a percent.
     * Values above 1000 indicate "disabled".
     *
     * @return the rate
     */
    public int getRate() {
        return rate;
    }
}
