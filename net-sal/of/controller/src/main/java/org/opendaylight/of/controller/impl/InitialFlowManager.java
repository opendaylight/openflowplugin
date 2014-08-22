/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.controller.impl;

import org.opendaylight.of.controller.InitialFlowContributor;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.DataPathInfo;
import org.opendaylight.of.lib.msg.OfmFlowMod;
import org.opendaylight.of.lib.msg.OfmMutableFlowMod;
import org.opendaylight.util.AbstractValidator;
import org.opendaylight.util.Log;
import org.opendaylight.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.opendaylight.of.lib.CommonUtils.notNull;

/**
 * Handles the registration and management of
 * {@link InitialFlowContributor initial flow contributors}.
 *
 * @author Simon Hunt
 */
class InitialFlowManager {
    private static final Logger LOG = LoggerFactory.getLogger(InitialFlowManager.class);
    static Logger log = LOG;

    private static final String IFM = "{InitialFlowManager}: ";
    private static final String E_BAD_CONTRIBUTION =
            IFM + "bad contribution from {}: {}";
    private static final String MSG_CONTRIB_OP = IFM + "{}: {}";
    private static final String REGISTERED = "registered";
    private static final String UNREGISTERED = "unregistered";

    private final Set<InitialFlowContributor> ifcCache = new HashSet<>();

    // for unit test support
    static void setLogger(Logger logger) {
        log = logger;
    }

    static void restoreLogger() {
        log = LOG;
    }

    @Override
    public String toString() {
        return "InitialFlowManager{contribs = " + ifcCache.size() + "}";
    }

    /**
     * Add the given contributor to the registration cache.
     *
     * @param ifc the contributor
     */
    void register(InitialFlowContributor ifc) {
        notNull(ifc);
        synchronized (ifcCache) {
            ifcCache.add(ifc);
            logOp(REGISTERED, ifc);
        } // sync
    }

    /**
     * Remove the given contributor from the registration cache.
     *
     * @param ifc the contributor
     */
    void unregister(InitialFlowContributor ifc) {
        notNull(ifc);
        synchronized (ifcCache) {
            ifcCache.remove(ifc);
            logOp(UNREGISTERED, ifc);
        } // sync
    }

    /**
     * Returns the number of registered contributors.
     *
     * @return the number of contributors
     */
    int size() {
        synchronized (ifcCache) {
            return ifcCache.size();
        }
    }

    private void logOp(String op, InitialFlowContributor ifc) {
        log.info(fmt(MSG_CONTRIB_OP, op, ifc.getClass().getName()));
    }

    private String fmt(String fmt, Object... items) {
        return StringUtils.format(fmt, items);
    }

    /**
     * Collate all initial flow mods from all contributors, for the given
     * datapath.
     *
     * @param info the info for the datapath
     * @param isHybrid whether the controller is in hybrid mode
     * @return the collated list of initial flows
     */
    List<OfmFlowMod> collateFlowMods(DataPathInfo info, boolean isHybrid) {
        List<OfmFlowMod> flowMods = new ArrayList<>();
        List<OfmFlowMod> toBeAdded;

        synchronized (ifcCache) {
            for (InitialFlowContributor ifc: ifcCache) {
                FlowValidator fv = new FlowValidator();
                try {
                    toBeAdded = ifc.provideInitialFlows(info, isHybrid);
                    flowMods.addAll(fv.validate(info, toBeAdded));

                } catch (Exception e) {
                    String who = ifc.getClass().getName();
                    log.warn(E_BAD_CONTRIBUTION, who, Log.stackTraceSnippet(e));
                }
            }
            return flowMods;
        } // sync
    }

    private static final String E_MUTABLE_FLOWMOD = "Mutable FlowMod: {}";
    private static final String E_VER_MISMATCH =
            "Version mismatch. Expected {} but found {}: {}";
    private static final String E_INCOMPLETE =
            "FlowMod Incomplete: {}";

    // use a validator so we throw one exception for (possibly) multiple issues
    private class FlowValidator extends AbstractValidator {
        private List<OfmFlowMod> validate(DataPathInfo info,
                                          List<OfmFlowMod> toBeAdded) {
            List<OfmFlowMod> adjustedFms = new ArrayList<>(toBeAdded.size());

            ProtocolVersion pv = info.negotiated();
            for (OfmFlowMod f: toBeAdded) {
                // verify that each flow is immutable, that it is the same
                // version as the datapath negotiated version, and that it
                // has all mandatory fields filled in...
                if (f instanceof OfmMutableFlowMod)
                    addError(fmt(E_MUTABLE_FLOWMOD, f));

                if (!f.getVersion().equals(pv))
                    addError(fmt(E_VER_MISMATCH, pv, f.getVersion(), f));

                // add the adjusted flow mod to our results list
                adjustedFms.add(f);

                // NOTE:
                // We cannot use OfmFlowMod.validate() to validate the message
                // as this requires table-id to be non-null; but we may have
                // table-id as null since this is an indication that the app
                // wants the device drivers to fill in the table-id for them.
                try {
                    notNull(f.getCommand(), f.getMatch());
                } catch (NullPointerException e) {
                    addError(fmt(E_INCOMPLETE, f));
                }
            }
            throwExceptionIfMessages();
            return adjustedFms;
        }

    }
}
