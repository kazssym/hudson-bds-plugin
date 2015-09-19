/*
 * BDSInstallation (deprecated)
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

package org.vx68k.jenkins.plugin.bds;

import java.io.IOException;
import java.util.List;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.EnvironmentSpecific;
import hudson.model.Hudson;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.slaves.NodeSpecific;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;
import hudson.tools.ToolProperty;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.vx68k.jenkins.plugin.bds.resources.Messages;

/**
 * Deprecated RAD Studio installation.
 * This class is retained for backward compatibility.
 * @author Kaz Nishimura
 * @since 1.0
 * @deprecated As of version 4.0, replaced by
 * {@link org.vx68k.hudson.plugin.bds.BDSInstallation}
 */
@Deprecated
public class BDSInstallation extends ToolInstallation
        implements NodeSpecific<BDSInstallation>,
        EnvironmentSpecific<BDSInstallation> {

    private static final long serialVersionUID = 2L;

    /**
     * Constructs this instance with property values.
     * @param name name of this installation
     * @param home home directory (the value of <code>BDS</code>)
     * @param properties list of tool properties
     */
    @DataBoundConstructor
    public BDSInstallation(
            String name, String home,
            List<? extends ToolProperty<?>> properties) {
        super(name, home, properties);
    }

    /**
     * Returns the array of the deprecated RAD Studio installations.
     * @return array of the deprecated RAD Studio installations
     * @since 4.0
     */
    public static BDSInstallation[] getInstallations() {
        return BDSInstallationDescriptor.getDescriptor().getInstallations();
    }

    /**
     * Converts this instance to
     * {@link org.vx68k.hudson.plugin.bds.BDSInstallation}.
     * @return converted {@link org.vx68k.hudson.plugin.bds.BDSInstallation}
     * instance
     * @since 4.0
     */
    public org.vx68k.hudson.plugin.bds.BDSInstallation convert() {
        return new org.vx68k.hudson.plugin.bds.BDSInstallation(getName(),
                getHome(), getProperties().toList());
    }

    /**
     * Returns a node-specific copy of this instance.
     * @param node node for which the return value is specialized.
     * @param listener a task listener
     * @return node-specific copy of this instance
     * @throws IOException if an I/O exception has occurred
     * @throws InterruptedException if interrupted
     */
    @Override
    public BDSInstallation forNode(Node node, TaskListener listener)
            throws IOException, InterruptedException {
        return new BDSInstallation(
                getName(), translateFor(node, listener),
                getProperties().toList());
    }

    /**
     * Returns an environment-specific copy of this instance.
     * @param environment environment for which the return value is
     * specialized.
     * @return environment-specific copy of this instance
     */
    @Override
    public BDSInstallation forEnvironment(EnvVars environment) {
        return new BDSInstallation(
                getName(), environment.expand(getHome()),
                getProperties().toList());
    }

    /**
     * Describes {@link BDSInstallation}.
     * @author Kaz Nishimura
     * @since 2.0
     */
    @Extension
    public static final class BDSInstallationDescriptor
            extends ToolDescriptor<BDSInstallation> {

        /**
         * Constructs this instance by loading the saved installations.
         */
        public BDSInstallationDescriptor() {
            // {@link ToolDescriptor#installations} can be <code>null</code>
            // when there is no configuration.
            setInstallations();
            load();
        }

        /**
         * Return the {@link BDSInstallationDescriptor} instance.
         * @return {@link BDSInstallationDescriptor} instance
         * @since 4.0
         */
        public static BDSInstallationDescriptor getDescriptor() {
            return Hudson.getInstance().getDescriptorByType(
                    BDSInstallationDescriptor.class);
        }

        /**
         * Saves the configuration of deprecated RAD Studio installations.
         * @param request Stapler request
         * @param json JSON object for the form fields
         * @return <code>true</code> if the operation succeeded;
         * <code>false</code> otherwise
         * @throws hudson.model.Descriptor.FormException if a form parsing
         * error has occurred
         */
        @Override
        public boolean configure(StaplerRequest request, JSONObject json)
                throws hudson.model.Descriptor.FormException {
            boolean ready = super.configure(request, json);
            if (ready) {
                save();
            }
            return ready;
        }

        /**
         * Returns the display name of a deprecated RAD Studio installation.
         * @return display name of a deprecated RAD Studio installations
         */
        @Override
        public String getDisplayName() {
            return Messages.getInstallationDisplayName();
        }
    }
}
