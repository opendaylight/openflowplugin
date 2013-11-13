/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.session;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.opendaylight.openflowplugin.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.openflow.md.core.IMDMessageTranslator;
import org.opendaylight.openflowplugin.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.openflow.md.core.TranslatorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mirehak
 */
public abstract class OFSessionUtil {

    private static final Logger LOG = LoggerFactory
            .getLogger(OFSessionUtil.class);

    /**
     * @param connectionConductor
     * @param features
     * @param version
     */
    public static void registerSession(ConnectionConductor connectionConductor,
            GetFeaturesOutput features, short version) {
        SwitchConnectionDistinguisher sessionKey = createSwitchSessionKey(features
                .getDatapathId());
        SessionContext sessionContext = getSessionManager().getSessionContext(sessionKey);
        if (LOG.isDebugEnabled()) {
            LOG.debug("registering sessionKey: {}", Arrays.toString(sessionKey.getId()));
        }
        
        if (features.getAuxiliaryId() == 0) {
            // handle primary
            if (sessionContext != null) {
                LOG.warn("duplicate datapathId occured while registering new switch session: "
                        + dumpDataPathId(features.getDatapathId()));
                getSessionManager().invalidateSessionContext(sessionKey);
            }
            // register new session context (based primary conductor)
            SessionContextOFImpl context = new SessionContextOFImpl();
            context.setPrimaryConductor(connectionConductor);
            context.setFeatures(features);
            context.setSessionKey(sessionKey);
            connectionConductor.setSessionContext(context);
            context.setValid(true);
            //TODO: retrieve listenerMapping from sessionManager and push it to conductor
            getSessionManager().addSessionContext(sessionKey, context);
        } else {
            // handle auxiliary
            if (sessionContext == null) {
                throw new IllegalStateException("unexpected auxiliary connection - primary connection missing: "
                        + dumpDataPathId(features.getDatapathId()));
            } else {
                // register auxiliary conductor into existing sessionContext
                SwitchConnectionDistinguisher auxiliaryKey = createConnectionCookie(features);
                if (sessionContext.getAuxiliaryConductor(auxiliaryKey) != null) {
                    LOG.warn("duplicate datapathId+auxiliary occured while registering switch session: "
                            + dumpDataPathId(features.getDatapathId())
                            + " | "
                            + features.getAuxiliaryId());
                    getSessionManager().invalidateAuxiliary(sessionKey,
                            auxiliaryKey);
                }

                //TODO: retrieve listenerMapping from sessionManager and push it to conductor
                sessionContext.addAuxiliaryConductor(auxiliaryKey,
                        connectionConductor);
                connectionConductor.setSessionContext(sessionContext);
                connectionConductor.setConnectionCookie(auxiliaryKey);
            }
        }
        
        // check registration result
        SessionContext resulContext = getSessionManager().getSessionContext(sessionKey);
        if (resulContext == null) {
            throw new IllegalStateException("session context registration failed");
        } else {
            if (!resulContext.isValid()) {
                throw new IllegalStateException("registered session context is invalid");
            }
        }
    }

    /**
     * @param datapathId
     * @return readable version of datapathId (hex)
     */
    public static String dumpDataPathId(BigInteger datapathId) {
        return datapathId.toString(16);
    }

    /**
     * @param datapathId
     * @return new session key
     */
    public static SwitchConnectionDistinguisher createSwitchSessionKey(
            BigInteger datapathId) {
        SwitchSessionKeyOFImpl key = new SwitchSessionKeyOFImpl();
        key.setDatapathId(datapathId);
        key.initId();
        return key;
    }

    /**
     * @param features
     * @return connection cookie key
     * @see #createConnectionCookie(BigInteger, short)
     */
    public static SwitchConnectionDistinguisher createConnectionCookie(
            GetFeaturesOutput features) {
        return createConnectionCookie(features.getDatapathId(),
                features.getAuxiliaryId());
    }

    /**
     * @param datapathId
     * @param auxiliaryId
     * @return connection cookie key
     */
    public static SwitchConnectionDistinguisher createConnectionCookie(
            BigInteger datapathId, short auxiliaryId) {
        SwitchConnectionCookieOFImpl cookie = null;
        if (auxiliaryId != 0) {
            cookie = new SwitchConnectionCookieOFImpl();
            cookie.setDatapathId(datapathId);
            cookie.setAuxiliaryId(auxiliaryId);
            cookie.initId();
        }
        return cookie;
    }

    /**
     * @return session manager singleton instance
     */
    public static SessionManager getSessionManager() {
        return SessionManagerOFImpl.getInstance();
    }
    
    /**
    * @return session manager listener Map
    */
    public static Map<TranslatorKey, Collection<IMDMessageTranslator<OfHeader, DataObject>>> getListenersMap() {
        return getSessionManager().getTranslatorMapping();
    }

}
