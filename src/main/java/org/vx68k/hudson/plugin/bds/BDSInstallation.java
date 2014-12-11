/*
 * BDSInstallation
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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Proc;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.EnvironmentSpecific;
import hudson.model.Hudson;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.slaves.NodeSpecific;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;
import hudson.tools.ToolProperty;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.vx68k.jenkins.plugin.bds.BDSInstallation.BDSInstallationDescriptor;

/**
 * RAD Studio installation.
 *
 * @author Kaz Nishimura
 * @since 4.0
 */
public class BDSInstallation extends ToolInstallation
        implements NodeSpecific<BDSInstallation>,
        EnvironmentSpecific<BDSInstallation> {

    private static final long serialVersionUID = 1L;

    private static final String BIN_DIRECTORY_NAME = "bin";
    private static final String BATCH_FILE_NAME = "rsvars.bat";

    /**
     * Pattern to match a <code>set</code> command.
     */
    private static final Pattern SET_COMMAND_PATTERN =
            Pattern.compile("\\s*@?set\\s+([^=]+)=(.*)",
                    Pattern.CASE_INSENSITIVE);

    /**
     * Constructs this object with immutable properties.
     *
     * @param name name of this installation
     * @param home home directory (the value of <code>BDS</code>)
     * @param properties properties for {@link ToolInstallation}
     */
    @DataBoundConstructor
    public BDSInstallation(String name, String home,
            List<? extends ToolProperty<?>> properties) {
        super(name, home, properties);
    }

    /**
     * Returns a {@link FilePath} object for the home directory.
     *
     * @param channel {@link VirtualChannel} object for {@link FilePath}
     * @return {@link FilePath} object for the home directory
     */
    protected FilePath getHome(VirtualChannel channel) {
        return new FilePath(channel, getHome());
    }

    /**
     * Returns a {@link FilePath} object for the batch file which initializes
     * a RAD Studio Command Prompt.
     *
     * @param channel {@link VirtualChannel} object for {@link FilePath}
     * @return {@link FilePath} object for the batch file
     */
    protected FilePath getBatchFile(VirtualChannel channel) {
        FilePath bin = new FilePath(getHome(channel), BIN_DIRECTORY_NAME);
        return new FilePath(bin, BATCH_FILE_NAME);
    }

    /**
     * Reads the RAD Studio environment variables from the batch file which
     * initializes a RAD Studio Command Prompt.
     * For a remote node, a <code>type</code> command will be used to read the
     * file content.
     *
     * @param build {@link AbstractBuild} object
     * @param launcher {@link Launcher} object
     * @param listener {@link BuildListener} object
     * @return environment variables read from the batch file
     * @throws IOException if an I/O exception has occurred
     * @throws InterruptedException if interrupted
     */
    public Map<String, String> readVariables(AbstractBuild<?, ?> build,
            Launcher launcher, BuildListener listener)
            throws IOException, InterruptedException {
        InputStream batch;
        FilePath batchFile = getBatchFile(launcher.getChannel());
        if (batchFile.isRemote()) {
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
            shellStarter.cmds(comspec, "/c", "type", batchFile.getRemote());

            Proc shell = shellStarter.start();
            int status = shell.join();
            if (status != 0) {
                // Any error messages must already be printed.
                return null;
            }

            batch = new ByteArrayInputStream(stdout.toByteArray());
        } else {
            batch = batchFile.read();
        }
        return readVariables(batch);
    }

    /**
     * Read the RAD Studio environment variables from an input stream.
     *
     * @param stream input stream
     * @return environment variables read from the input stream
     * @throws IOException if an I/O exception has occurred
     */
    protected Map<String, String> readVariables(InputStream stream)
            throws IOException {
        Map<String, String> variables =
                new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(stream));
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
        return variables;
    }

    /**
     * Returns a {@link NodeSpecific} version of this object.
     *
     * @param node node for which the return value is specialized.
     * @param listener a {@link TaskListener} object
     * @return {@link NodeSpecific} copy of this object
     * @throws IOException if an I/O exception has occurred
     * @throws InterruptedException if interrupted
     */
    @Override
    public BDSInstallation forNode(Node node, TaskListener listener)
            throws IOException, InterruptedException {
        return new BDSInstallation(getName(), translateFor(node, listener),
                getProperties().toList());
    }

    /**
     * Returns an {@link EnvironmentSpecific} version of this object.
     *
     * @param environment environment for which the return value is
     * specialized.
     * @return {@link EnvironmentSpecific} copy of this object
     */
    @Override
    public BDSInstallation forEnvironment(EnvVars environment) {
        return new BDSInstallation(getName(), environment.expand(getHome()),
                getProperties().toList());
    }

    /**
     * Describes {@link BDSInstallation}.
     *
     * @author Kaz Nishimura
     */
    @Extension
    public static final class Descriptor
            extends ToolDescriptor<BDSInstallation> {

        /**
         * Constructs this object and loads configured installations.
         */
        public Descriptor() {
            // {@link ToolDescriptor#installations} can be <code>null</code>
            // when there is no configuration
            setInstallations(new BDSInstallation[0]);
            load();
        }

        /**
         * Finds a {@link BDSInstallation} object by its name.
         * If there is nothing found, it returns <code>null</code>.
         *
         * @param name name of a RAD Studio installation
         * @return {@link BDSInstallation} object, or <code>null</code> if not
         * found
         */
        public BDSInstallation getInstallation(String name) {
            for (BDSInstallation i : getInstallations()) {
                if (i.getName().equals(name)) {
                    return i;
                }
            }
            return null;
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json)
                throws FormException {
            boolean ready = super.configure(req, json);
            if (ready) {
                save();
            }
            return ready;
        }

//        @Override
//        protected FormValidation checkHomeDirectory(File home) {
//            File bin = new File(home, BIN_DIRECTORY_NAME);
//            File batch = new File(bin, BATCH_FILE_NAME);
//            if (!batch.isFile()) {
//                return FormValidation.error(NOT_INSTALLATION_DIRECTORY);
//            }
//            return super.checkHomeDirectory(home);
//        }

        /**
         * Returns the display name for {@link BDSInstallation}.
         *
         * @return display name for {@link BDSInstallation}
         */
        @Override
        public String getDisplayName() {
            return "RAD Studio";
        }
    }
}
