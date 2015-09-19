/*
 * BDSInstallation
 * Copyright (C) 2014-2015 Nishimura Software Studio
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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
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
import org.vx68k.hudson.plugin.bds.resources.Messages;

/**
 * RAD Studio installation.
 * @author Kaz Nishimura
 * @since 4.0
 */
public class BDSInstallation extends ToolInstallation
        implements NodeSpecific<BDSInstallation>,
        EnvironmentSpecific<BDSInstallation> {

    private static final long serialVersionUID = 4L;

    private static final String BIN_DIRECTORY_NAME = "bin";
    private static final String BATCH_FILE_NAME = "rsvars.bat";

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
     * Returns the array of the RAD Studio installations.
     * @return array of the RAD Studio installations
     */
    public static BDSInstallation[] getInstallations() {
        return Descriptor.getDescriptor().getInstallations();
    }

    /**
     * Returns the RAD Studio installation identified by a name.
     * @param name name of the RAD Studio installation
     * @return RAD Studio installation, or <code>null</code> if no installation
     * was found
     */
    public static BDSInstallation getInstallation(String name) {
        for (BDSInstallation i : getInstallations()) {
            if (i.getName() != null && i.getName().equals(name)) {
                return i;
            }
        }
        return null;
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
     *
     * @param build {@link AbstractBuild} object
     * @param launcher {@link Launcher} object
     * @param listener {@link TaskListener} object
     * @return environment variables read from the batch file
     * @throws IOException if an I/O exception has occurred
     * @throws InterruptedException if interrupted
     */
    public Map<String, String> readVariables(AbstractBuild<?, ?> build,
            Launcher launcher, TaskListener listener)
            throws IOException, InterruptedException {
        if (getHome().isEmpty()) {
            listener.error(Messages.getHomeIsEmptyMessage());
            return null;
        }

        InputStream batchStream = BDSUtilities.getInputStream(build,
                launcher, listener, getBatchFile(launcher.getChannel()));
        if (batchStream == null) {
            // Any error messages must already be printed.
            return null;
        }

        return BDSUtilities.readVariables(batchStream);
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
     * @author Kaz Nishimura
     */
    @Extension
    public static final class Descriptor
            extends ToolDescriptor<BDSInstallation> {

        /**
         * Constructs this object by loading the saved installations.
         */
        public Descriptor() {
            // {@link ToolDescriptor#installations} can be <code>null</code>
            // when there is no configuration.
            setInstallations();
            load();
        }

        /**
         * Return the {@link Descriptor} instance.
         * @return {@link Descriptor} instance
         */
        public static Descriptor getDescriptor() {
            return Hudson.getInstance().getDescriptorByType(Descriptor.class);
        }

        @Override
        public BDSInstallation[] getInstallations() {
            BDSInstallation[] installations;
            synchronized (this) {
                installations = super.getInstallations();
                if (installations.length == 0) {
                    org.vx68k.jenkins.plugin.bds.BDSInstallation[] olds =
                            org.vx68k.jenkins.plugin.bds.BDSInstallation
                                    .getInstallations();
                    installations = new BDSInstallation[olds.length];
                    for (int i = 0; i != installations.length; i += 1) {
                        installations[i] = olds[i].convert();
                    }

                    // Keeps the migrated installations without saving.
                    setInstallations(installations);
                }
            }
            return installations;
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
         * Returns the display name for RAD Studio installations.
         *
         * @return display name for RAD Studio installations
         */
        @Override
        public String getDisplayName() {
            return "RAD Studio";
        }
    }
}
