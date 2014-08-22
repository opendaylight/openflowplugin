/*
 * (c) Copyright 2009 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import static org.opendaylight.util.StringUtils.UTF8;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Set of utilities for launching a process and parsing it's output.
 *
 * @author Thomas Vachuska
 */
public final class ProcessUtils {

    // no instantiation
    private ProcessUtils() { }

    /**
     * Open the system default editor on the specified file.
     * 
     * @param file path of the file which should be open 
     * @return buffered reader for consuming the process output; null if the
     *         process failed to launch correctly
     */
    public static BufferedReader open(String file) {
        return execute("rundll32 url.dll,FileProtocolHandler \"" + file + "\"");
    }
    
    /**
     * Launch a process and return a buffered reader ready to consume its
     * output.
     * 
     * @param command command to be executed
     * @param cwd path to the current working directory in which to launch the
     *        subprocess
     * @return buffered reader for consuming the process output; null if the
     *         process failed to launch correctly
     */
    public static BufferedReader execute(String command, String cwd) {
        try {
            Process p = Runtime.getRuntime().exec(command, null, new File(cwd));
            return new BufferedReader(new InputStreamReader(p.getInputStream(), UTF8));
        } catch (IOException e) {
        }
        return null;
    }
    
    /**
     * Launch a process and return a buffered reader ready to consume its
     * output.
     * 
     * @param command array of command arguments to be executed
     * @param cwd path to the current working directory in which to launch the
     *        subprocess
     * @return buffered reader for consuming the process output; null if the
     *         process failed to launch correctly
     */
    public static BufferedReader execute(String command[], String cwd) {
        try {
            Process p = Runtime.getRuntime().exec(command, null, new File(cwd));
            return new BufferedReader(new InputStreamReader(p.getInputStream(), UTF8));
        } catch (IOException e) {
        }
        return null;
    }


    /**
     * Launch a process and return a buffered reader ready to consume its
     * output.
     * 
     * @param command command to be executed
     * @return buffered reader for consuming the process output; null if the
     *         process failed to launch correctly
     */
    public static BufferedReader execute(String command) {
        return execute(command, System.getProperty("user.dir"));
    }

    /**
     * Run the specified command and return it's output as a string.
     * 
     * @param command command to be executed
     * @return string containing command standard output; null if the process
     *         failed to launch correctly
     */
    public static String exec(String command) {
        return exec(command, System.getProperty("user.dir"));
    }

    /**
     * Run the specified command and return it's output as a string.
     * 
     * @param command array of command arguments to be executed
     * @return string containing command standard output; null if the process
     *         failed to launch correctly
     */
    public static String exec(String command[]) {
        return exec(command, System.getProperty("user.dir"));
    }

    /**
     * Run the specified command and return it's output as a string.
     * 
     * @param command array of command arguments to be executed
     * @param cwd path to the current working directory in which to launch the
     *        subprocess
     * @return string containing command standard output; null if the process
     *         failed to launch correctly
     */
    public static String exec(String command[], String cwd) {
        BufferedReader br = execute(command, cwd);
        return slurp(br);
    }
    
    /**
     * Run the specified command and return it's output as a string.
     * 
     * @param command command to be executed
     * @param cwd path to the current working directory in which to launch the
     *        subprocess
     * @return string containing command standard output; null if the process
     *         failed to launch correctly
     */
    public static String exec(String command, String cwd) {
        BufferedReader br = execute(command, cwd);
        return slurp(br);
    }
    
    /**
     * Consume the output of a command from the given buffered reader.
     * 
     * @param br buffered reader containing output of a command
     * @return string containing command standard output; null if the process
     *         failed to launch correctly
     */
    public static String slurp(BufferedReader br) {
        if (br == null)
            return null;

        StringBuilder output = new StringBuilder();
        String line;
        try {
            while ((line = br.readLine()) != null)
                output.append(line).append(StringUtils.EOL);
            return new String(output);
        } catch (IOException e) {
            return null;
        } finally {
            try {
                br.close();
            } catch (IOException ioe) {
            }
        }
    }

}
