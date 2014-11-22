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

package org.vx68k.jenkins.plugin.bds;

import java.io.File;
import java.io.IOException;
import java.util.List;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.EnvironmentSpecific;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.slaves.NodeSpecific;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;
import hudson.tools.ToolProperty;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Configures RAD Studio installations for {@link BDSBuilder}.
 *
 * @author Kaz Nishimura
 * @since 1.0
 */
public class BDSInstallation extends ToolInstallation
        implements NodeSpecific<BDSInstallation>,
        EnvironmentSpecific<BDSInstallation> {

    private static final long serialVersionUID = 2L;

    private static final String DISPLAY_NAME = "RAD Studio";

    private static final String NOT_INSTALLATION_DIRECTORY =
            "Not a RAD Studio installation directory."; // TODO: I18N.

    private static final String BIN_DIRECTORY_NAME = "bin";
    private static final String BATCH_FILE_NAME = "rsvars.bat";

    /**
     * Constructs this object with property values.
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
    public FilePath getHome(VirtualChannel channel) {
        return new FilePath(channel, getHome());
    }

    /**
     * Returns a {@link FilePath} object for the batch file that initializes
     * environment variables for a Command Prompt.
     *
     * @param channel a {@link VirtualChannel} interface for {@link FilePath}
     * @return {@link FilePath} object for the batch file
     * @since 3.0
     */
    public FilePath getBatchFile(VirtualChannel channel) {
        FilePath bin = new FilePath(getHome(channel), BIN_DIRECTORY_NAME);
        return new FilePath(bin, BATCH_FILE_NAME);
    }

    /**
     * Returns a {@link NodeSpecific} version of this object.
     *
     * @param node node for whitch the return value is specialized.
     * @param listener a {@link TaskListener} object
     * @return {@link NodeSpecific} copy of this object
     * @throws IOException
     * @throws InterruptedException
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
     * @since 2.0
     */
    @Extension
    public static class BDSInstallationDescriptor
            extends ToolDescriptor<BDSInstallation> {

        public BDSInstallationDescriptor() {
            load();
        }

        /**
         * @deprecated As of version 3.0, replaced by
         * {@link BDSInstallation#getBatchFile}.
         */
        @Deprecated
        public FilePath getBatchFile(FilePath home) {
            FilePath bin = new FilePath(home, BIN_DIRECTORY_NAME);
            return new FilePath(bin, BATCH_FILE_NAME);
        }

        /**
         * Finds a {@link BDSInstallation} object by its name.
         * If there is nothing found, it returns <code>null</code>.
         *
         * @param name name of a installation to find
         * @return a {@link BDSInstallation} object, or <code>null</code>
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

        @Override
        protected FormValidation checkHomeDirectory(File home) {
            File bin = new File(home, BIN_DIRECTORY_NAME);
            File batch = new File(bin, BATCH_FILE_NAME);
            if (!batch.isFile()) {
                return FormValidation.error(NOT_INSTALLATION_DIRECTORY);
            }
            return super.checkHomeDirectory(home);
        }

        /**
         * Returns the display name of this plugin.
         *
         * @return the display name
         */
        @Override
        public String getDisplayName() {
            return DISPLAY_NAME;
        }
    }
}
