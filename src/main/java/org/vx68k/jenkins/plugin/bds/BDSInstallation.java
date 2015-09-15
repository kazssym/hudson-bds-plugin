/*
 * BDSInstallation for backward compatibility
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

package org.vx68k.jenkins.plugin.bds;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
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
import org.vx68k.hudson.plugin.bds.BDSUtilities;
import org.vx68k.hudson.plugin.bds.resources.Messages;

/**
 * Deprecated RAD Studio installation.  This class is retained for backward
 * compatibility.
 *
 * @author Kaz Nishimura
 * @since 1.0
 * @deprecated As of version 4.0, replaced by {@link
 * org.vx68k.hudson.plugin.bds.BDSInstallation}
 */
@Deprecated
public class BDSInstallation extends ToolInstallation
        implements NodeSpecific<BDSInstallation>,
        EnvironmentSpecific<BDSInstallation> {

    private static final long serialVersionUID = 2L;

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
     * Returns a {@link FilePath} object for the home directory.
     *
     * @param channel a {@link VirtualChannel} interface for {@link FilePath}
     * @return {@link FilePath} object for the home directory
     * @since 3.0
     */
    protected FilePath getHome(VirtualChannel channel) {
        return new FilePath(channel, getHome());
    }

    /**
     * Returns a {@link FilePath} object for the batch file which initializes
     * a RAD Studio Command Prompt.
     *
     * @param channel a {@link VirtualChannel} interface for {@link FilePath}
     * @return {@link FilePath} object for the batch file
     * @since 3.0
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
     * @param build an {@link AbstractBuild} object
     * @param launcher a {@link Launcher} object
     * @param listener a {@link BuildListener} object
     * @return map of the environment variables
     * @throws InterruptedException
     * @throws IOException
     * @since 3.0
     */
    public Map<String, String> readVariables(AbstractBuild<?, ?> build,
            Launcher launcher, BuildListener listener)
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
     * Read the RAD Studio environment variables from an input stream.
     *
     * @param stream input stream from the batch file for initializing a RAD
     * Studio Command Prompt
     * @return map of the environment variables
     * @throws IOException
     * @since 3.0
     */
    protected Map<String, String> readVariables(InputStream stream)
            throws IOException {
        return BDSUtilities.readVariables(stream);
    }

    /**
     * Converts this object to {@link
     * org.vx68k.hudson.plugin.bds.BDSInstallation}.
     *
     * @return {@link org.vx68k.hudson.plugin.bds.BDSInstallation} object
     * @since 4.0
     */
    public org.vx68k.hudson.plugin.bds.BDSInstallation convert() {
        return new org.vx68k.hudson.plugin.bds.BDSInstallation(getName(),
                getHome(), getProperties().toList());
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
     * Describes deprecated {@link BDSInstallation}.
     *
     * @author Kaz Nishimura
     * @since 2.0
     */
    @Extension
    public static final class BDSInstallationDescriptor
            extends ToolDescriptor<BDSInstallation> {

        /**
         * Constructs this object and loads configured installations.  In
         * addition, if {@link
         * org.vx68k.hudson.plugin.bds.BDSInstallation.Descriptor} has no
         * installations, migrate all the installations of this object.
         */
        public BDSInstallationDescriptor() {
            // {@link ToolDescriptor#installations} can be <code>null</code>
            // when there is no configuration
            setInstallations(new BDSInstallation[0]);
            load();

            Hudson application = Hudson.getInstance();
            org.vx68k.hudson.plugin.bds.BDSInstallation.Descriptor descriptor
                    = application.getDescriptorByType(
                            org.vx68k.hudson.plugin.bds.BDSInstallation.Descriptor.class);
            if (descriptor.getInstallations().length == 0) {
                    BDSInstallation[] installations = getInstallations();
                org.vx68k.hudson.plugin.bds.BDSInstallation[] newInstallations
                        = new org.vx68k.hudson.plugin.bds.BDSInstallation[installations.length];
                for (int i = 0; i != installations.length; ++i) {
                    newInstallations[i] = installations[i].convert();
                }
                descriptor.setInstallations(newInstallations);
            }
        }

        /**
         * Returns a deprecated RAD Studio installation that has a specified
         * name.
         *
         * @param name name of a deprecated RAD Studio installation
         * @return deprecated RAD Studio installation, or <code>null</code> if
         * not found
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
        public boolean configure(StaplerRequest request, JSONObject json)
                throws FormException {
            boolean ready = super.configure(request, json);
            if (ready) {
                save();
            }
            return ready;
        }

        /**
         * Returns the display name for deprecated RAD Studio installations.
         *
         * @return display name for deprecated RAD Studio installations
         */
        @Override
        public String getDisplayName() {
            return "RAD Studio \u2013 " + Messages.getDeprecated();
        }
    }
}
