/*
 * Copyright (c) 2015 Inocybe Technologies inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor: jfokkan@inocybe.com
 */

package org.opendaylight.openflowplugin.karaf.commands;

import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.MBeanInfo;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.ReflectionException;

import java.lang.management.ThreadInfo;
import java.lang.Integer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.util.LinkedList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Formatter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.karaf.shell.console.AbstractAction;


@Command(scope = "openflow", name = "status", description = "Openflow Status Command")
public class OpenflowStatus extends AbstractAction {

    private int DEFAULT_REFRESH_INTERVAL = 1000;
    private int DEFAULT_KEYBOARD_INTERVAL = 100;


    @Option(name = "-u", aliases = { "--updates" }, description = "Update interval in milliseconds", required = false, multiValued = false)
    private String updates;


    protected Object doExecute() throws Exception {
        if (updates != null) {
             DEFAULT_REFRESH_INTERVAL = Integer.parseInt(updates);
        }
        try {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            OpenflowStatus(server);
        } catch (IOException e) {
            //Ignore
        }
        return null;
    }

    private void OpenflowStatus(MBeanServer server) throws InterruptedException, IOException, MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException {

        boolean run = true;

        // Continously update stats to console.
        while (run) {
            clearScreen();
            System.out.println("\u001B[36m==========================================================================================\u001B[0m");
            run = waitOnKeyboard();
        }
    }


    private boolean waitOnKeyboard() throws InterruptedException {
        InputStreamReader reader = new InputStreamReader(session.getKeyboard());
        for (int i = 0; i < DEFAULT_REFRESH_INTERVAL / DEFAULT_KEYBOARD_INTERVAL; i++) {
            Thread.sleep(DEFAULT_KEYBOARD_INTERVAL);
            try {
                if (reader.ready()) {
                    int value = reader.read();
                    switch (value) {
                        case 'q':
                            return false;
                        // Add more cases here for more interactive status display
                    }
                }
            } catch (IOException e) {

            }
        }

        return true;
    }

    private void clearScreen() {
        System.out.print("\33[2J");
        System.out.flush();
        System.out.print("\33[1;1H");
        System.out.flush();
    }

}
