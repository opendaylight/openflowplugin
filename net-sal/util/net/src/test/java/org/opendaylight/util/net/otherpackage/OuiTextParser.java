/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.ip.otherpackage;

import org.opendaylight.util.StringUtils;
import org.opendaylight.util.net.MacAddress;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.opendaylight.util.StringUtils.EOL;
import static org.opendaylight.util.StringUtils.normalizeEOL;

/**
 * Parses the oui.txt file distributed by the IEEE at
 * http://standards.ieee.org/develop/regauth/oui/oui.txt
 * to produce a properties file to be used by the {@link MacAddress} for
 * the company name look up by OUI.
 *
 * @author Simon Hunt
 */
public class OuiTextParser {

    private static final ClassLoader CL = OuiTextParser.class.getClassLoader();
    private static final String OUI_TXT = "oui.txt";
    private static final String PROP_FILE = "ethernetCompanies.properties";

    private static final Pattern RE_BASE16_LINE =
            Pattern.compile("\\s*([0-9a-fA-F]{6})\\s+\\(base 16\\)\\s+(.*)");

    private static final String LINE =
            "# ===========================================================";

    private static final String[] HEADER = {
            LINE,
            "# Ethernet Company OUIs (Organizationally Unique Identifiers)",
            LINE,
            "# NOTE:",
            "# This file is generated automatically via \"OuiTextParser\"",
            "#          *****   D O    N O T    E D I T   *****",
            LINE
    };
    private static final String GEN = "# Last generated: ";
    private static final String UNKNOWN = "UNKNOWN = Unknown";
    private static final String PRIVATE = "PRIVATE = Private";

    /** Main entry point.
     *
     * @param args command line arguments
     * @throws IOException if there is an issue reading the input file
     */
    public static void main(String[] args) throws IOException {
        String contents = StringUtils.getFileContents(OUI_TXT, CL);
        if (contents == null)
            throw new RuntimeException("input file not found");

        String[] lines = normalizeEOL(contents).split("\n");

        BufferedWriter bw = new BufferedWriter(new FileWriter(PROP_FILE));
        for (String s: HEADER)
            bw.write(s + EOL);
        bw.write(GEN + new Date() + EOL);
        bw.write(LINE + EOL + EOL + UNKNOWN + EOL + PRIVATE + EOL);

        int count = 0;
        for (String s: lines) {
            Matcher m = RE_BASE16_LINE.matcher(s);
            if (m.matches()) {
                String key = m.group(1).toLowerCase(Locale.getDefault());
                bw.write(key + " = " + m.group(2) + StringUtils.EOL);
                count++;
            }
        }
        bw.close();
        System.out.println(PROP_FILE + " written with " + count + " entries.");
    }
}
