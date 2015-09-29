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
import java.util.List;
import java.util.Map;

import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SwitchSessionKeyOF;
import org.opendaylight.openflowplugin.openflow.md.core.ConnectionConductorImpl;
import org.opendaylight.openflowplugin.api.openflow.md.core.IMDMessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.api.openflow.md.core.TranslatorKey;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.api.openflow.md.queue.PopListener;
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
    // public static void registerSession(ConnectionConductorImpl connectionConductor,
    public static SessionContext registerSession(ConnectionConductorImpl connectionConductor,
            GetFeaturesOutput features, short version) {
        SwitchSessionKeyOF sessionKey = createSwitchSessionKey(features
                .getDatapathId());
        SessionContext sessionContext = getSessionManager().getSessionContext(sessionKey);
        if (LOG.isDebugEnabled()) {
            LOG.debug("registering sessionKey: {}", Arrays.toString(sessionKey.getId()));
        }

        if (features.getAuxiliaryId() == null || features.getAuxiliaryId() == 0) {
            // handle primary
            if (sessionContext != null) {
                LOG.warn("duplicate datapathId occured while registering new switch session: "
                        + dumpDataPathId(features.getDatapathId()));
                getSessionManager().invalidateSessionContext(sessionKey);
            }
            // register new session context (based primary conductor)
            SessionContextOFImpl context = new SessionContextOFImpl();
            context.setPrimaryConductor(connectionConductor);
            context.setNotificationEnqueuer(connectionConductor);
            context.setFeatures(features);
            context.setSessionKey(sessionKey);
            context.setSeed((int) System.currentTimeMillis());
            connectionConductor.setSessionContext(context);
            getSessionManager().addSessionContext(sessionKey, context);
        } else {
            // handle auxiliary
            if (sessionContext == null) {
                throw new IllegalStateException("unexpected auxiliary connection - primary connection missing: "
                        + dumpDataPathId(features.getDatapathId()));
            } else {
                // register auxiliary conductor into existing sessionContext
                SwitchConnectionDistinguisher auxiliaryKey = createConnectionCookie(features, sessionContext.getSeed());
                if (sessionContext.getAuxiliaryConductor(auxiliaryKey) != null) {
                    LOG.warn("duplicate datapathId+auxiliary occured while registering switch session: "
                            + dumpDataPathId(features.getDatapathId())
                            + " | "
                            + features.getAuxiliaryId());
                    getSessionManager().invalidateAuxiliary(sessionKey,
                            auxiliaryKey);
                }

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
	return(resulContext);
    }

    public static void setRole(SessionContext sessionContext)
    {
            getSessionManager().setRole(sessionContext);
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
    public static SwitchSessionKeyOF createSwitchSessionKey(
            BigInteger datapathId) {
        SwitchSessionKeyOF key = new SwitchSessionKeyOF();
        key.setDatapathId(datapathId);
        return key;
    }

    /**
     * @param features
     * @param seed 
     * @return connection cookie key
     * @see #createConnectionCookie(BigInteger,short, int)
     */
    public static SwitchConnectionDistinguisher createConnectionCookie(
            GetFeaturesOutput features, int seed) {
        return createConnectionCookie(features.getDatapathId(),
                features.getAuxiliaryId(), seed);
    }

    /**
     * @param datapathId
     * @param auxiliaryId
     * @param seed 
     * @return connection cookie key
     */
    public static SwitchConnectionDistinguisher createConnectionCookie(
            BigInteger datapathId, short auxiliaryId, int seed) {
        SwitchConnectionCookieOFImpl cookie = null;
        cookie = new SwitchConnectionCookieOFImpl();
        cookie.setAuxiliaryId(auxiliaryId);
        cookie.init(datapathId.intValue() + seed);
        return cookie;
    }

    /**
     * @return session manager singleton instance
     */
    public static ConjunctSessionManager getSessionManager() {
        return SessionManagerOFImpl.getInstance();
    }
    
    /**
     * release session manager singleton instance
     */
    public static void releaseSessionManager() {
        SessionManagerOFImpl.releaseInstance();
    }
    
    /**
    * @return session manager listener Map
    */
    public static Map<TranslatorKey, Collection<IMDMessageTranslator<OfHeader, List<DataObject>>>> getTranslatorMap() {
        return getSessionManager().getTranslatorMapping();
    }

    /**
     * @return pop listener Map
     */
    public static Map<Class<? extends DataObject>, Collection<PopListener<DataObject>>> getPopListenerMapping() {
        return getSessionManager().getPopListenerMapping();
    }

    /**
     * @return extension converters provider
     */
    public static ExtensionConverterProvider getExtensionConvertorProvider() {
        return getSessionManager().getExtensionConverterProvider();
    }

    /**
     * @return collection of all sessions
     */
    public static Collection<SessionContext> getAllSessions() {
        return getSessionManager().getAllSessions();
    }

}
