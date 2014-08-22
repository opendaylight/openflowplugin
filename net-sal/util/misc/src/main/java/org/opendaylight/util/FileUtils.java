/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import static org.opendaylight.util.StringUtils.UTF8;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Miscellaneous utilities for manipulating files.
 *
 * @author Thomas Vachuska
 */
public final class FileUtils {
    
    /**
     * Utility to replace a given ERE in the specified file with a replacement
     * string.
     * 
     * @param file file to search and replace patterns within
     * @param ere extended regular expression to be replaced
     * @param replacement replacement string
     * @throws IOException if issues encountered while reading original or
     *         writing replacement
     */
    public static void replaceERE(String file, String ere, String replacement) 
                            throws IOException {
        InputStreamReader ir = null;
        BufferedReader br = null;
        PrintWriter pw = null;
        boolean ok = false;
        try {
            ir = new InputStreamReader(new FileInputStream(file), UTF8);
            br = new BufferedReader(ir);
            pw = new PrintWriter(file + "~~", UTF8);
            String line;
            while ((line = br.readLine()) != null) {
                String s = line.replaceAll(ere, replacement);
                pw.println(s);
            }
            ok = true;
        } finally {
            // Close the writer first, if needed; the other close methods may
            // toss exception, thus resulting in leaked file handles.
            if (pw != null)
                pw.close();
            
            // Close the outter buffered reader first if appropriate; else just
            // close the raw file reader.
            if (br != null)
                br.close();
            else if (ir != null)
                ir.close();
        }
        
        if (ok)
            replaceFile(file + "~~", file);
    }
    
    /**
     * Safely moves one file over another.
     * 
     * @param file source file
     * @param toFile target file to which the source file should be moved
     * @return true if the replacement succeeded; false otherwise
     */
    public static boolean replaceFile(String file, String toFile) {
        File src = new File(file);
        File tgt = new File(toFile);
        boolean ok = true;
        
        if (src.exists() && tgt.exists())
            ok = tgt.delete();
        return ok && src.renameTo(tgt);
    }
    
    
    /**
     * Consume the content of the specified text file and return it as a string.
     * 
     * @param file file path of the file to be read
     * @return string containing file content
     * @throws IOException if issues encountered while reading file
     */
    public static String read(String file) throws IOException {
        return ProcessUtils.slurp(new BufferedReader(new InputStreamReader(new FileInputStream(file), UTF8)));
    }

    /**
     * Write the given data into the specified text file.
     * 
     * @param file file path of the file to be written
     * @param data to be written
     * @throws IOException if issues encountered while reading file
     */
    public static void write(String file, String data) throws IOException {
        FileOutputStream fos = new FileOutputStream(file, false);
        try {
            fos.write(data.getBytes(UTF8));
        } finally {
            fos.close();
        }
    }
    
    /**
     * Write the given input stream of data to the specified file
     * 
     * @param fileStream the input stream containing the data to be written
     * @param file The file to be written to
     * @throws IOException if issues encountered while writing the file
     */
    public static void write(InputStream fileStream, File file) throws IOException{
        OutputStream out = new FileOutputStream(file, false);
        try {
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = fileStream.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } finally {
            out.close();
        }
    }

    
    /**
     * List directory contents for a resource folder. Not recursive. This is
     * basically a brute-force implementation. Works for regular files and
     * also JARs.
     * 
     * Based on code authored by Greg Briggs.
     * 
     * @param clazz Any java class that lives in the same place as the
     *        resources you want.
     * @param path Should end with "/", but not start with one.
     * @return Just the name of each member item, not the full paths.
     * @throws URISyntaxException if the given path forms bad URL
     * @throws IOException if issues arise reading the requested resource
     * @throws IllegalArgumentException if path starts with a slash
     */
    public static String[] getResourceListing(Class<?> clazz, String path)
                                  throws URISyntaxException, IOException {
        if (path.startsWith("/"))
            throw new IllegalArgumentException("path starts with slash: " + path);

        String pathNorm = FilePathUtils.addSeparator(path, '/');
        
        URL dirURL = clazz.getClassLoader().getResource(pathNorm);
        if (dirURL != null && dirURL.getProtocol().equals("file"))
            // A file path; easy enough
            return new File(dirURL.toURI()).list();

        if (dirURL == null) {
            // In case of a jar file, we can't actually find a directory. Have
            // to assume the same jar as the reference class 'clazz' parameter.
            String me = clazz.getName().replace(".", "/") + ".class";
            dirURL = clazz.getClassLoader().getResource(me);
        }

        if (dirURL.getProtocol().equals("jar")) {
            // Strip out the jar path and use it to construct a jar file
            String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!"));
            JarFile jar = new JarFile(URLDecoder.decode(jarPath, UTF8));

            try {
                // Get all entries in jar
                Enumeration<JarEntry> entries = jar.entries();
                
                // Use a set to avoid duplicates in case it is a subdirectory
                Set<String> result = new HashSet<String>(); 
                while (entries.hasMoreElements()) {
                    String name = entries.nextElement().getName();
                    
                    // Filter according to the path
                    if (name.startsWith(pathNorm)) {
                        String entry = name.substring(pathNorm.length());
                        int checkSubdir = entry.indexOf("/");
                        if (checkSubdir >= 0)
                            // if it is a subdirectory, we just return the directory name
                            entry = entry.substring(0, checkSubdir);
                        result.add(entry);
                    }
                }
                return result.toArray(new String[result.size()]);
                
            } finally {
                jar.close();
            }
        }

        throw new UnsupportedOperationException("Cannot list files for URL "  + dirURL);
    }

}
