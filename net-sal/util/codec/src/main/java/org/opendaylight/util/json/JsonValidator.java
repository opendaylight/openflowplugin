/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.json;

import java.io.Reader;
import java.io.StringReader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
//import com.github.fge.jsonschema.exceptions.ProcessingException;
//import com.github.fge.jsonschema.main.JsonSchema;
//import com.github.fge.jsonschema.main.JsonSchemaFactory;
//import com.github.fge.jsonschema.report.ProcessingReport;

// FIXME: put the json schema stuff back once we get an OSGI bundle

/**
 * A JSON validator utility class.
 * 
 * @author Liem Nguyen
 */
public class JsonValidator {

    public static final String SCHEMA = "schemas/model.json";

    private static final String SCHEMA_URI = "resource:/" + SCHEMA + "#/";

    private static final ObjectMapper mapper = new ObjectMapper();

//    private static final JsonSchemaFactory jsf = JsonSchemaFactory.byDefault();

    // No instantiation
    private JsonValidator() {
    }

    /**
     * Validate the given json with the given json schema.
     * 
     * @param schemaUri schema file to validate with
     * @param json JSON text to validate
     * @param jsonRoot root node of JSON text
     */
    public static void validate(String schemaUri, Reader json, String jsonRoot) {
//        try {
//            validate(jsf.getJsonSchema(schemaUri + "#/" + jsonRoot), json,
//                    jsonRoot);
//        } catch (ProcessingException e) {
//            throw new JsonValidationException("Failed to validate " + jsonRoot,
//                    e);
//        }
    }

    /**
     * Validate the given json with the given schema.
     * 
     * @param json JSON text to validate
     * @param jsonRoot root node of JSON text
     */
    public static void validate(String json, String jsonRoot) {
//        try {
//            validate(jsf.getJsonSchema(SCHEMA_URI + jsonRoot),
//                    new StringReader(json), jsonRoot);
//        } catch (ProcessingException e) {
//            throw new JsonValidationException("Failed to validate " + jsonRoot,
//                    e);
//        }
    }

    // validation work horse
//    private static void validate(JsonSchema js, Reader json, String jsonRoot) {
//        boolean isValid = false;
//        StringBuffer errorMsg = new StringBuffer();
//        try {
//            // Read json, strip off root node
//            final JsonNode jNode = mapper.readTree(json).get(jsonRoot);
//
//            // Do it!
//            final ProcessingReport report = js.validate(jNode);
//            isValid = report.isSuccess();
//            if (!isValid)
//                errorMsg.append(report.toString());
//        } catch (Throwable t) {
//            throw new JsonValidationException("Failed to validate " + jsonRoot,
//                    t);
//        }
//        // Last check!
//        if (!isValid)
//            throw new JsonValidationException(errorMsg.toString());
//    }
}
