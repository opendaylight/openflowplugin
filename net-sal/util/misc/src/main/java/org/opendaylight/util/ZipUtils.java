/*
 * (c) Copyright 2009 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Set of utilities to produce ZIP files.
 * 
 * @author Thomas Vachuska
 * @author Fabiel Zuniga
 */
public final class ZipUtils {

    // no instantiation
    private ZipUtils() {
    }

    /**
     * Writes the given file to the given ZIP output.
     * 
     * @param file file to write
     * @param outputStream stream to write the file into
     * @throws NullPointerException if either file or outputStream is null
     * @throws IOException if an I/O error has occurred
     */
    public static void write(File file, ZipOutputStream outputStream) 
                                throws NullPointerException, IOException {
        
        if (file == null)
            throw new NullPointerException("file cannot be null");
        if (outputStream == null)
            throw new NullPointerException("outputStream cannot be null");
        write(file, "", outputStream);
    }

    private static void write(File file, String parentPath, 
                              ZipOutputStream outputStream) throws IOException {
        if (file.isFile()) {
            ZipEntry entry = new ZipEntry(parentPath + file.getName());
            entry.setSize(file.length());
            entry.setTime(file.lastModified());
            outputStream.putNextEntry(entry);

            byte buffer[] = new byte[8 * 1024];
            int length;
            InputStream inputStream = new FileInputStream(file);

            try {
                while ((length = inputStream.read(buffer, 0, buffer.length)) > 0)
                    outputStream.write(buffer, 0, length);
            } finally {
                inputStream.close();
            }

            outputStream.closeEntry();

        } else {
            String newParentPath = parentPath + file.getName() + File.separator;
            File[] files = file.listFiles();

            // Files are written before folders to properly generate the ZIP file.
            for (File childFile : files)
                if (childFile.isFile())
                    write(childFile, newParentPath, outputStream);

            for (File childFile : files)
                if (childFile.isDirectory())
                    write(childFile, newParentPath, outputStream);
        }
    }

}
