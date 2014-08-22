/*
 * (c) Copyright 2004 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.syntax;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Allows compilation of XML-encoded syntax into {@link SyntaxPackage} entity.
 * <p>
 * <li><a href="http://na.know.hp.com/teams/quake/clif/Shared%20Documents/CLI%20Functional%20Specification.doc">CLIF Functional Specification Document</a>
 * describes in more detail the XML-encoded CLIF definition syntax. 
 * 
 * @author Thomas Vachuska
 */
public class SyntaxCompiler {

    /** URI of the XML schema.  */
    private String schemaLocation = null;
    
    /**
     * Default constructor.
     */
    public SyntaxCompiler() {
    }

    /**
     * Compiles the specified XML-encoded input stream into a syntax package
     * that can be used to match and parse command-line arguments.
     *
     * @param xmlInput input stream containing the XML-encoded syntax
     * definitions.
     * @param locale Locale context for interpretting the locale-independent
     * syntax definitions.
     * @return {@link SyntaxPackage} instance created based on the XML
     * content and suitable for use by {@link SyntaxMatcher} class.
     * @throws BadSyntaxException thrown if the specified input stream
     * contained a malformed syntax definition.
     * @throws SAXException thrown if the specified input stream
     * contained a malformed XML.
     * @throws IOException thrown if there was a problem reading data from the
     * specified input stream.
     */
    public SyntaxPackage compile(InputStream xmlInput, Locale locale) 
                        throws BadSyntaxException, SAXException, IOException {
        // Turn the input stream into an XML input source and parse it.
        InputSource in = new InputSource(xmlInput);
        
        try {
            SAXParser saxp = SAXParserFactory.newInstance().newSAXParser();
            XMLReader parser = saxp.getXMLReader();

            // Enable validation and provide a hint as to the location of the 
            // XML schema definition, provided we have the schema location, 
            // of course.
            if (schemaLocation != null) {
                parser.setFeature("http://xml.org/sax/features/validation", true);
                parser.setFeature("http://apache.org/xml/features/validation/schema", 
                                  true);
                parser.setProperty("http://apache.org/xml/properties/schema/" + 
                                   "external-noNamespaceSchemaLocation", 
                                   schemaLocation);
            }
            
            Compiler compiler = new Compiler(locale);
            parser.setContentHandler(compiler);
            parser.setErrorHandler(compiler);
            parser.parse(in);
            
            // Post-process the results.
            return compiler.resolveSyntax();
            
        } catch (ParserConfigurationException e) {
            throw new SAXException("Unable to compile syntax", e);
        }
    }
    
    /**
     * Gets the URI of the XML schema for the syntax files.
     * 
     * @return URI of the location where syntax files are located
     */
    public String getSchemaLocation() {
        return schemaLocation;
    }
    
    /**
     * Sets the URI where to find the XML schema for validating syntax files.
     * @param schemaLocation URI of the XML schema
     */
    public void setSchemaLocation(String schemaLocation) {
        this.schemaLocation = schemaLocation;
    }

}
