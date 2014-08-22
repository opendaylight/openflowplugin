/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.json;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.opendaylight.util.json.JsonValidator.SCHEMA;

/**
 * An abstract JsonFactory. All sub-classes of this class should be
 * singletons.
 *
 * @author Liem Nguyen
 */
public abstract class AbstractJsonFactory implements JsonFactory {

    private Map<String, JsonCodec<?>> codecs;

    // FIXME: Missing javadocs for protected constructor
    protected AbstractJsonFactory() {
        codecs = new HashMap<String, JsonCodec<?>>();
    }

    // FIXME: Missing javadocs for public methods
    public void addCodecs(Class<?> pojo, JsonCodec<?> codec) {
        codecs.put(pojo.getName(), codec);
    }

    public void removeCodecs(Class<?> pojo) {
        codecs.remove(pojo.getName());
    }

    public void clearCodecs() {
        codecs.clear();
    }

    @Override
    public InputStream schema() {
        // The classloader needs to be retrieved at run-time so that we can get
        // the classloader of the running OSGi bundle
        ClassLoader cl = this.getClass().getClassLoader();
        return cl.getResourceAsStream(SCHEMA);
    }

    @Override
    public JsonCodec<?> codec(Class<?> pojo) {
        JsonCodec<?> codec = _codec(pojo);
        if (codec == null)
            throw new JsonCodecException("No codec found for " + pojo.getName());
        return codec;
    }

    @Override
    public boolean hasCodec(Class<?> pojo) {
        return _codec(pojo) != null;
    }

    @Override
    public int hashCode() {
        return getClass().getName().hashCode();
    }

    private JsonCodec<?> _codec(Class<?> pojo) {
        JsonCodec<?> codec = codecs.get(pojo.getName());
        if (codec == null)
            // Check the interfaces that this pojo implements as well...
            for (Class<?> itf : pojo.getInterfaces()) {
                codec = codecs.get(itf.getName());
                if (codec != null) break;
            }
        return codec;
    }

}
