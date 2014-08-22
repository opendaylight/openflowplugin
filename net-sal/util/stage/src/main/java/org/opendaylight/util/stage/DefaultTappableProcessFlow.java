/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.stage;

import java.util.HashSet;
import java.util.Set;

/**
 * Basic implementation of a process flow with stages interconnected
 * indirectly via fan-out outlets at the top of each stage and with the bottom
 * of each separate stage outlet being tappable via it's own fan-out outlet.
 * 
 * @author Thomas Vachuska
 * @author Simon Hunt
 */
public class DefaultTappableProcessFlow extends DefaultFanoutProcessFlow
        implements TappableProcessFlow {

    @Override
    public <P> Set<Outlet<P>> getTaps(Class<? extends ProcessStageOutlet<?, P>> stageClass,
                                      ProcessStageOutlet<?, P> tappedStageOutlet) {
        FanoutOutlet<P> nfo = (FanoutOutlet<P>) getDownstreamOutlet(stageClass);
        FanoutOutlet<P> tfo = (FanoutOutlet<P>) tappedStageOutlet.getOutlet();

        // Copy the tap outlets and remove the outlet that leads to the next
        // stage fan-out, since it's really not a tap.
        Set<Outlet<P>> taps = new HashSet<Outlet<P>>(tfo.getOutlets());
        taps.remove(nfo);
        return taps;
    }

    @Override
    public <P> int getTapCount(Class<? extends ProcessStageOutlet<?, P>> stageClass,
                               ProcessStageOutlet<?, P> tappedStageOutlet) {
        // Number of taps is the number of outlets, minus the one that leads
        // to the next stage fan-out.
        FanoutOutlet<P> tfo = (FanoutOutlet<P>) tappedStageOutlet.getOutlet();
        return tfo.size() - 1;
    }

    @Override
    public <P> boolean addTap(Class<? extends ProcessStageOutlet<?, P>> stageClass,
                              ProcessStageOutlet<?, P> tappedStageOutlet,
                              Outlet<P> tapOutlet) {
        FanoutOutlet<P> tfo = (FanoutOutlet<P>) tappedStageOutlet.getOutlet();
        return tfo.add(tapOutlet);
    }

    @Override
    public <P> boolean removeTap(Class<? extends ProcessStageOutlet<?, P>> stageClass,
                                 ProcessStageOutlet<?, P> tappedStageOutlet,
                                 Outlet<P> tapOutlet) {
        FanoutOutlet<P> nfo = (FanoutOutlet<P>) getDownstreamOutlet(stageClass);
        FanoutOutlet<P> tfo = (FanoutOutlet<P>) tappedStageOutlet.getOutlet();

        if (nfo == tapOutlet)
            throw new IllegalArgumentException("Outlet '" + tapOutlet +
                                               "' is not a tap outlet");
        return tfo.remove(tapOutlet);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation attaches the given outlet to its downstream
     * counterpart indirectly, via its own tap fan-out.
     */
    @Override
    protected <T, P> boolean connectToDownstreamOutlet(Class<? extends ProcessStageOutlet<T, P>> stageClass,
                                                       StackableOutlet<T, P> outlet) {
        Outlet<P> ndo = getDownstreamOutlet(stageClass);
        if (ndo == null)
            return false;
        // Create a new fan-out for the taps, attach it to the incoming outlet
        // and then add the downstream outlet to the tap fan-out.
        FanoutOutlet<P> tfo = new MeteringFanoutOutlet<P>();
        outlet.setOutlet(tfo);
        return tfo.add(ndo);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation disconnects the given stage outlet's taps fan-out
     * from the downstream outlet, leaving all taps in place.
     */
    @Override
    protected <T, P> boolean disconnectFromDownstreamOutlet(Class<? extends ProcessStageOutlet<T, P>> stageClass,
                                                            ProcessStageOutlet<T, P> stageOutlet) {
        Outlet<P> ndo = getDownstreamOutlet(stageClass);
        if (ndo == null)
            return false;
        FanoutOutlet<P> tfo = (FanoutOutlet<P>) stageOutlet.getOutlet();
        return tfo.remove(ndo);
    }

}
