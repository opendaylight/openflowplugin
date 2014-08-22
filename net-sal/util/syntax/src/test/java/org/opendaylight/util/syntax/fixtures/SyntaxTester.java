/*
 * (c) Copyright 2004 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.util.syntax.fixtures;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.opendaylight.util.syntax.BadUsageException;
import org.opendaylight.util.syntax.Parameters;
import org.opendaylight.util.syntax.Syntax;
import org.opendaylight.util.syntax.SyntaxCompiler;
import org.opendaylight.util.syntax.SyntaxMatcher;
import org.opendaylight.util.syntax.SyntaxPackage;
import org.opendaylight.util.syntax.usage.SyntaxUsage;

import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;


/**
 * Utility to aid in developing command-line syntax definitions.
 *
 * @author Thomas Vachuska
 */
public class SyntaxTester {
    
    public static final String DEFAULT_SCHEMA_LOCATION = 
        "src/main/resources/org/opendaylight/util/syntax/";

    public static final String SYNTAX_SCHEMA = 
        System.getProperty("syntaxSchema"); 
    
    public static final String SYNTAX_LOCATION = 
        System.getProperty("syntaxLocation"); 
    
    public static final String LOCALE = 
        System.getProperty("locale");
    
    private SyntaxCompiler compiler;
    private SyntaxMatcher matcher;
    private SyntaxUsage usage;
    
    @Test
    public void stfuStupidAntRunner() {}

    /**
     * Sets up the tester by creating the syntax compiler and syntax matcher
     * using the specified environment.
     * 
     * @param syntaxSchema location of the syntax definition schema
     * @param syntaxDirectory location of the syntax definitions to be tested
     * @param locale locale in which to test
     */
    private void setupTester(String syntaxSchema, File syntaxDirectory, Locale locale) {
        //  Let's make sure that the given syntax schema file exists.
        if (new File(syntaxSchema).length() < 512)
            die("XML Syntax schema " + syntaxSchema + " does not exist.");
        
        //  Setup the compiler and compile XML syntaxes...
        compiler = new SyntaxCompiler();
        compiler.setSchemaLocation(syntaxSchema);
        Set<SyntaxPackage> packages = compile(compiler, locale, syntaxDirectory, null);
        if (packages.size() == 0)
            die("No packages were compiled.");

        //  Setup the matcher...
        matcher = new SyntaxMatcher(locale);
        matcher.addPackages(packages);
        if (matcher.getSyntaxes().size() == 0)
            die("No root syntaxes were compiled.");

        //  Setup the usage generator...
        usage = new SyntaxUsage(locale);
        usage.addPackages(packages);
    }

    /**
     * Main entry point for testing from command-line.
     * 
     * @param args command line arguments
     */
    public static void main(String args[]) {
        String syntaxSchema = ((SYNTAX_SCHEMA != null) ? 
                SYNTAX_SCHEMA : "./syntax-schema.xsd");
        File syntaxLocation = ((SYNTAX_LOCATION != null) ? 
                new File(SYNTAX_LOCATION) : new File("."));
        Locale locale = ((LOCALE != null) ? 
                new Locale(LOCALE) : Locale.getDefault());
    
        try {
            SyntaxTester tester = new SyntaxTester();
            tester.setupTester(syntaxSchema, syntaxLocation, locale);
            
            output("packageCount=" + tester.matcher.getPackages().size());
            output("syntaxCount=" + tester.matcher.getSyntaxes().size());
            try {
                //  Attempt to match user arguments...
                Parameters parameters = new Parameters();
                Syntax syntax = tester.matcher.getMatchingSyntax(args, parameters);
                output("syntax=" + syntax.getName());
                output("actionName=" + syntax.getActionName());
                
                Iterator<String> it = parameters.getNames().iterator();
                while (it.hasNext()) {
                    String name = it.next();
                    output(name + "=" + parameters.get(name));
                }
                
            } catch (BadUsageException e) {
                tester.handleUsage(e, args);
                die(e);
            }
        } catch (Throwable t) {
            error(t);
            System.exit(1);
        }
    }

    private void handleUsage(BadUsageException e, String args[]) {
        String lastArg = args[args.length-1];
        PrintWriter pw = new PrintWriter(System.err);
        if (lastArg.equals("--help")) {
            usage.printHelp(pw, e.getSyntax());
        } else {
            usage.printUsage(args[0], pw, SyntaxUsage.TEXT);
        }
        pw.flush();
    }

    /**
     * Compiles the command-line syntaxes and returns a {@link java.util.Set}
     * of {@link org.opendaylight.util.syntax.SyntaxPackage} instances.
     * 
     * @param compiler syntax compiler
     * @param locale context locale
     * @param syntaxLocation directory where syntax definitions are located
     * @param logger logger to be used
     * @return set of parsed syntax packages
     */
    public Set<SyntaxPackage> compile(SyntaxCompiler compiler, Locale locale, 
                                      File syntaxLocation, Logger logger) {
        Set<SyntaxPackage> packages = new HashSet<SyntaxPackage>();
        
        if (syntaxLocation.isDirectory()) {
            // File filter to accept only .xml files.
            FileFilter xmlFiles = new FileFilter() {
                @Override
                public boolean accept(File path) {
                    return path.getName().endsWith(".xml");
                };
            };
            
            // Iterate over all syntax XML files and compile each of them into
            // a syntax package and add it into the set.
            File[] files = syntaxLocation.listFiles(xmlFiles);
            for (int i = 0; i < files.length; i++)
                    packages.add(compile(files[i], locale, logger));
        } else {
            packages.add(compile(syntaxLocation, locale, logger));
        }
        return packages;
    }

    private SyntaxPackage compile(File file, Locale locale, Logger logger) {
        try {
            FileInputStream fis = new FileInputStream(file);
            return compiler.compile(fis, locale);
        } catch (Exception e) {
            if (logger != null)
                logger.log(Level.SEVERE, "Unable to compile syntax.", e);
            else
                error("Unable to compile syntax: " + e);
        }
        return null; 
    }

    @SuppressWarnings("unused")
    private static void output(Object o) { 
        // System.out.println(o); 
    }
    
    private static void error(Object o) {
        System.err.println(o);
    }

    private static void die(Object o) {
        error(o);
        fail(o.toString());
    }

    
    public void runTest(SyntaxTest test) {
        int error = test.getErrorIndex();
        String name = test.getName();
        try {
            //  Attempt to match user arguments...
            Parameters parameters = new Parameters();
            Syntax syntax = matcher.getMatchingSyntax(test.getArgs(), 
                                                      parameters);
            assertFalse(name + " should have been an error at " + error, 
                        error >= 0);
            assertEquals("incorrect syntax for test " + name, 
                         test.getSyntaxName(), syntax.getName());
            String actionName = test.getActionName();
            if (actionName != null)
                assertEquals("incorrect actionName for test " + name, 
                             actionName, syntax.getActionName());
            
            Parameters p = test.getParameters();
            if (p != null) {
                assertEquals("incorrect parameter count for test " + name,
                             p.size(), parameters.size());
                Iterator<String> it = p.getNames().iterator();
                while (it.hasNext()) {
                    String pn = it.next();
                    assertTrue("parameter " + pn + " was not found for test " + 
                               name, parameters.isPresent(pn));
                    String pv = (String) p.get(pn);
                    if (pv != null)
                        assertEquals("incorrect value for parameter " + pn +
                                     " for test " + name,
                                     pv, parameters.get(pn).toString());
                }
            }
            
        } catch (BadUsageException e) {
            assertFalse(name + " should not have been an error: " + e, 
                        error < 0);
            assertEquals("incorrect error index for error " + e, 
                         error, e.getErrorOffset());
        }
    }
    
    /**
     * Runs the syntax definition tests contained in the specified file.
     * 
     * @param syntaxTestFile XML-encoded syntax definition test file
     * @throws Exception if I/O issues or syntax issues are encountered
     */
    public void runTests(File syntaxTestFile) throws Exception {
        SAXParser saxp = SAXParserFactory.newInstance().newSAXParser();
        XMLReader parser = saxp.getXMLReader();
        TestHandler handler = new TestHandler();
        parser.setContentHandler(handler);
        parser.setErrorHandler(handler);
        
        if (syntaxTestFile.isDirectory() && syntaxTestFile.exists()) {
            // File filter to accept only .st files.
            FileFilter xmlFiles = new FileFilter() {
                @Override
                public boolean accept(File path) {
                    return path.getName().endsWith(".st");
                };
            };
            // Iterate over all syntax test files...
            File[] files = syntaxTestFile.listFiles(xmlFiles);
            for (int i = 0; i < files.length; i++)
                parser.parse(new InputSource(new FileInputStream(files[i])));
        } else {
            // Turn the input stream into an XML input source and parse it.
            parser.parse(new InputSource(new FileInputStream(syntaxTestFile)));
        }
    }

    
    /**
     * Auxiliary class that provides the basic XML parsing capabilities.
     */
    protected class TestHandler extends DefaultHandler {
        
        public static final String DEFAULT_SYNTAX_SCHEMA = 
            DEFAULT_SCHEMA_LOCATION + "syntax-schema.xsd";
        
        public static final String SUITE = "suite";
        public static final String NAME = "name";
        public static final String DIRECTORY = "directory";
        
        public static final String TEST = "test";
        public static final String ARGS = "args";
        public static final String EXPECT = "expect";
        public static final String SYNTAX = "syntax";
        public static final String ACTION_NAME = "actionName";
        public static final String ERROR = "error";
        public static final String PARAMETER = "param";
        public static final String VALUE = "value";
        
        private String name;
        private String directory;
        private SyntaxTest test;
        private int testNumber = 0;

        private void setupTestSuite(Attributes attributes) {
            this.name = attributes.getValue(NAME);
            this.directory = attributes.getValue(DIRECTORY);
            this.testNumber = 0;  // start numbering tests from 0 in each suite
            
            String schema = directory + "syntax-schema.xsd";
            setupTester(schema, new File(directory), Locale.getDefault());
        }
        
        private String[] tokenizeArgs(String args) {
            StringTokenizer st = new StringTokenizer(args);
            List<String> argList = new ArrayList<String>();
            while (st.hasMoreTokens())
                argList.add(st.nextToken());
            return argList.toArray(new String[argList.size()]);
        }

        private SyntaxTest setupTest(Attributes attributes) {
            testNumber++;
            String testName = attributes.getValue(NAME);
            if (testName == null)
                testName = (name != null ? name : "test") + "-" + testNumber;
            SyntaxTest newTest = new SyntaxTest();
            newTest.setName(testName);
            String usage = attributes.getValue(ARGS);
            if (usage != null)
                newTest.setArgs(tokenizeArgs(usage));
            setupResult(newTest, attributes);
            return newTest;
        }

        private void setupResult(SyntaxTest test, Attributes attributes) {
            String syntaxName = attributes.getValue(SYNTAX);
            if (syntaxName != null)
                test.setSyntaxName(syntaxName);
            String actionName = attributes.getValue(ACTION_NAME);
            if (actionName != null)
                test.setActionName(actionName);
            String errorIndex = attributes.getValue(ERROR);
            if (errorIndex != null)
                test.setErrorIndex(Integer.parseInt(errorIndex));
        }

        private void setupParameter(Attributes attributes) {
            Parameters parameters = test.getParameters();
            if (parameters == null) {
                parameters = new Parameters();
                test.setParameters(parameters);
            }
            parameters.add(attributes.getValue(NAME),
                           attributes.getValue(VALUE),
                           name.indexOf("#") > 0);
        }

        @Override
        public void startElement(String namespaceURI, String localName,
                                 String qName, Attributes attributes)
                        throws SAXException {
            String name = localName == null || localName.length() == 0 ? qName : localName;
            if (name.equals(TEST))
                test = setupTest(attributes);
            else if (name.equals(EXPECT))
                setupResult(test, attributes);
            else if (name.equals(PARAMETER))
                setupParameter(attributes);
            else if (name.equals(SUITE))
                setupTestSuite(attributes);
        }

        @Override
        public void endElement(String namespaceURI, String localName,
                               String qName) throws SAXException {
            String name = localName == null || localName.length() == 0 ? qName : localName;
            if (name.equals(TEST))
                runTest(test);
        }

        @Override
        public void error(SAXParseException e) throws SAXException {
            throw e;
        }

    }
}
