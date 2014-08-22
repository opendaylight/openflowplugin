/*
 * (c) Copyright 2004 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.syntax.usage;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.opendaylight.util.syntax.Syntax;
import org.opendaylight.util.syntax.SyntaxKeyword;
import org.opendaylight.util.syntax.SyntaxNode;
import org.opendaylight.util.syntax.SyntaxPackage;

/**
 * Provides facilities to generate localizable usage help messages from
 * {@link org.opendaylight.util.syntax.SyntaxPackage syntax packages} and
 * {@link org.opendaylight.util.syntax.SyntaxPackage syntaxes}.
 *
 * @author Thomas Vachuska
 */
public class SyntaxUsage {

    public static final int TEXT = 0;
    public static final int NROFF = 1;

    public static final int FILL_LENGTH = 4;
    public static final int MAX_LINE_LENGTH = 78;

    private static final String SYNTAX_USAGE_BUNDLE =
        "org.opendaylight.util.syntax.Syntax";
    // FIXME: "org.opendaylight.util.syntax.usage.SyntaxUsage";
    
    private static final String SYNTAX_SEPARATOR = 
        "----------------------------------------" + 
        "---------------------------------------";

    private ResourceBundle resources;
    private Set<SyntaxPackage> packages;
    private Map<String, Set<Syntax>> topicSyntaxes;
    private Set<String> primaryTopics;

    /**
     * Default constructor
     * 
     * @param locale context locale for generating usage string
     */
    public SyntaxUsage(Locale locale) {
        this.resources = ResourceBundle.getBundle(SYNTAX_USAGE_BUNDLE, locale);
        packages = new HashSet<SyntaxPackage>();
        topicSyntaxes = new HashMap<String, Set<Syntax>>();
        primaryTopics = new HashSet<String>();
    }

    /**
     * Registers the given package with the usage generator.
     * 
     * @param syntaxPackage syntax package to be added
     */
    public void addPackage(SyntaxPackage syntaxPackage) {
        packages.add(syntaxPackage);
        
        // Now register all concrete syntaxes according to their topic(s) 
        for (Syntax s : syntaxPackage.getSyntaxes()) {
            if (s.getActionName() != null) {
                String topics[] = s.getHelpTopics().split(",");
                for (int i = 0; i < topics.length; i++) {
                    String topic = topics[i].trim();
                    if (i == 0)
                        primaryTopics.add(topic);
                    Set<Syntax> syntaxes = topicSyntaxes.get(topic);
                    if (syntaxes == null) {
                        syntaxes = new HashSet<Syntax>();
                        topicSyntaxes.put(topic, syntaxes);
                    }
                    syntaxes.add(s);
                }
            }
        }
    }

    /**
     * Registers all syntax packages in the given set with the usage
     * generator.
     * 
     * @param packages set of syntax packages to be added
     */
    public void addPackages(Set<SyntaxPackage> packages) {
        for (SyntaxPackage p : packages)
            addPackage(p);
    }

    /**
     * Gets all of the topics associated with this SyntaxUsage.
     *
     * @return A set of topics which are associated with this SyntaxUsage.
     */
    public Set<String> getTopics() {
        return topicSyntaxes.keySet();
    }

    /**
     * Gets a list of all primary topics associated with this SyntaxUsage.
     *
     * @return A set of topics which are associated with this SyntaxUsage.
     */
    public Set<String> getPrimaryTopics() {
        return primaryTopics;
    }


    /**
     * Prints the usage message for the specified syntax.
     *
     * @param topic help topic for which to generate usage help message
     * @param writer PrintWriter to which output should be generated
     * @param format desired output format <code>TEXT</code>, etc.
     */
    public void printUsage(String topic, PrintWriter writer, int format) {
        Set<Syntax> syntaxes = topicSyntaxes.get(topic);
        if (syntaxes == null)
            throw new IllegalArgumentException("No syntaxes for help topic '" +
                                               topic + "' found.");
        for (Syntax syntax : syntaxes)
            printUsage(writer, syntax, format);
    }

    /**
     * Prints the usage message for the specified syntax.
     *
     * @param topic help topic for which to generate usage help message
     * @param writer PrintWriter to which output should be generated
     */
    public void printHelp(String topic, PrintWriter writer) {
        Set<Syntax> syntaxes = topicSyntaxes.get(topic);
        if (syntaxes == null)
            throw new IllegalArgumentException("No syntaxes for help topic '" +
                                               topic + "' found.");
        for (Syntax syntax : syntaxes) {
            printHelp(writer, syntax);
            writer.println(SYNTAX_SEPARATOR);
        }
    }


    /**
     * Prints the usage help message for the specified syntax.
     *
     * @param writer PrintWriter to which output should be generated
     * @param syntax syntax for which to generate usage help message
     */
    public void printHelp(PrintWriter writer, Syntax syntax) {
        printUsage(writer, syntax, TEXT);
        printDescription(writer, syntax, TEXT);
        printUsageDetails(writer, syntax, TEXT);
        printHelpText(writer, syntax, TEXT);
    }


    /**
     * Prints a short message describing the command associated with
     * the specified syntax.
     *
     * @param writer PrintWriter to which output should be generated
     * @param syntax syntax for which to generate description message
     * @param format desired output format <code>TEXT</code>, etc.
     */
    public void printDescription(PrintWriter writer, Syntax syntax, int format) {
        String description = syntax.getDescription();
        if (description != null) {
            printWrapped(writer, description, 0, MAX_LINE_LENGTH);
            writer.println();
        }
    }

    /**
     * Prints a long help message describing the behaviour of the command
     * associated with the specified syntax.
     *
     * @param writer PrintWriter to which output should be generated
     * @param syntax syntax for which to generate help message
     * @param format desired output format <code>TEXT</code>, etc.
     */
    public void printHelpText(PrintWriter writer, Syntax syntax, int format) {
        String helpText = syntax.getHelpText();
        if (helpText != null)
            printWrapped(writer, helpText, 0, MAX_LINE_LENGTH);
    }


    /**
     * Prints the usage message for the specified syntax.
     *
     * @param writer PrintWriter to which output should be generated
     * @param syntax syntax for which to generate usage help message
     * @param format desired output format <code>TEXT</code>, etc.
     */
    public void printUsage(PrintWriter writer, Syntax syntax, int format) {
        if (format == TEXT)
            writer.print(resources.getString("usage_prefix") + " ");
        writer.println(syntax);
        writer.println();
    }


    /**
     * Print the usage details message for the specified syntax.
     *
     * @param writer PrintWriter to which output should be generated
     * @param syntax syntax for which to generate usage details message
     * @param format desired output format <code>TEXT</code>, etc.
     */
    public void printUsageDetails(PrintWriter writer, Syntax syntax,
                                  int format) {
        List<SyntaxNode> nodes = new ArrayList<SyntaxNode>();
        nodes.addAll(syntax.getAnchoredNodes());
        nodes.addAll(syntax.getFloatingNodes());

        int maxOptionLength = getLongestNodeLength(nodes);

        // Now iterate over the combined list and print description for each
        // node that has one.
        for (SyntaxNode node : nodes) {
            if (node instanceof SyntaxKeyword)
                continue;

            String image = node.toString();
            String helpText = node.getHelpText();
            if (helpText == null)
                continue;

            printFill(writer, FILL_LENGTH);
            writer.print(image);
            printFill(writer, maxOptionLength - image.length() + FILL_LENGTH);
            printWrapped(writer, helpText,
                         maxOptionLength + 2*FILL_LENGTH, MAX_LINE_LENGTH);
        }
    }

    /**
     * Prints the given message broken into multiple lines adjusted to fit
     * between the given offset and the specified maximum line length.
     * @param writer print writer
     * @param message message to be printed
     * @param indent size of indent
     * @param maxLineLength maximum line length before wrapping
     */
    private void printWrapped(PrintWriter writer, String message,
                              int indent, int maxLineLength) {
        int lineLength = maxLineLength - indent;
        int messageLength = message.length();
        for (int i = 0; i < messageLength; ) {
            int max = Math.min(i + lineLength, messageLength);
            if (i > 0)
                printFill(writer, indent);

            //  If the max is not past the end of message,
            //  adjust max to fall onto a space.
            int breakIndex = (max < messageLength) ?
                    message.lastIndexOf(' ', max) : max;
            if (breakIndex > 0 && breakIndex >= i) {
                writer.println(message.substring(i, breakIndex));
                i = breakIndex + 1;
            } else {
                writer.println(message.substring(i));
                i = messageLength;
            }
        }
    }

    /**
     * Prints the given number of spaces using the specified writer.
     * @param writer print writer
     * @param fillLength number of spaces to write
     */
    private void printFill(PrintWriter writer, int fillLength) {
        for (int i = 0; i < fillLength; i++)
            writer.print(' ');
    }

    /**
     * Get the length of the longest node string image.
     * 
     * @param nodes list of nodes to evaluate
     * @return number of characters in the longest name
     */
    private int getLongestNodeLength(List<SyntaxNode> nodes) {
        int length = 0;
        for (SyntaxNode node : nodes) {
            if (node.getHelpText() != null) {
                String image = node.toString();
                if (image.length() > length)
                    length = image.length();
            }
        }
        return length;
    }

}
