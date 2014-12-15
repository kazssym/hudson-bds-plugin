/*
 * BDSUtilities
 * Copyright (C) 2014 Kaz Nishimura
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.vx68k.hudson.plugin.bds;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Proc;
import hudson.model.AbstractBuild;
import hudson.model.TaskListener;

/**
 * Utility methods for RAD Studio.
 *
 * @author Kaz Nishimura
 * @since 4.0
 */
public class BDSUtilities {

    /**
     * Pattern to match a <code>set</code> command.
     */
    private static final Pattern SET_COMMAND_PATTERN =
            Pattern.compile("\\s*@?set\\s+([^=]+)=(.*)",
                    Pattern.CASE_INSENSITIVE);

    /**
     * Read RAD Studio environment variables from an input stream.
     *
     * @param stream input stream
     * @return environment variables read from the input stream
     * @throws IOException if an I/O exception has occurred
     */
    public static Map<String, String> readVariables(InputStream stream)
            throws IOException {
        Map<String, String> variables = new TreeMap<String, String>(
                String.CASE_INSENSITIVE_ORDER);
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                stream, "ISO-8859-1")); // TOOD: Handle OEM character set.
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher setCommand = SET_COMMAND_PATTERN.matcher(line);
                if (setCommand.matches()) {
                    String key = setCommand.group(1);
                    String value = setCommand.group(2);
                    if (key.startsWith("BDS") || key.startsWith("CG_") ||
                            key.startsWith("Framework")) {
                        variables.put(key, value);
                    }
                }
            }
        } finally {
            reader.close();
        }
        // RAD Studio XE2 and earlier does not set 'BDSINCLUDE'.
        variables.putIfAbsent("BDSINCLUDE", variables.get("BDS") +
                "\\include");
        return variables;
    }

    /**
     * Returns an input stream for a file.  For a remote file, a
     * <code>type</code> command will be executed to get the file content.
     *
     * @param build {@link AbstractBuild} object
     * @param launcher {@link Launcher} object
     * @param listener {@link TaskListener} object
     * @param file {@link FilePath} object for a file
     * @return input stream from the file
     * @throws IOException
     * @throws InterruptedException
     */
    public static InputStream getInputStream(AbstractBuild<?, ?> build,
            Launcher launcher, TaskListener listener, FilePath file)
            throws IOException, InterruptedException {
        if (file.isRemote()) {
            EnvVars environment = build.getEnvironment(listener);

            String comspec = environment.get("COMSPEC");
            if (comspec == null) {
                listener.error("COMSPEC is not set: "
                        + "this node is probably not Windows."); // TODO: I18N.
                return null;
            }

            ByteArrayOutputStream stdout = new ByteArrayOutputStream();

            Launcher.ProcStarter shellStarter = launcher.launch();
            shellStarter.envs(environment);
            shellStarter.stdout(stdout);
            shellStarter.stderr(listener.getLogger());
            shellStarter.cmds(comspec, "/c", "type", file.getRemote());

            Proc shell = shellStarter.start();
            if (shell.join() != 0) {
                // Any error messages must already be printed.
                return null;
            }

            return new ByteArrayInputStream(stdout.toByteArray());
        }
        return file.read();
    }
}
