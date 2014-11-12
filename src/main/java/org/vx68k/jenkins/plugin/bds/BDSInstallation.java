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
import hudson.model.EnvironmentSpecific;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.slaves.NodeSpecific;
import hudson.tools.Messages;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;
import hudson.tools.ToolProperty;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Manages RAD Studio installations for {@link BDSBuildWrapper}.
 *
 * @author Kaz Nishimura
 * @since 1.0
 */
public class BDSInstallation extends ToolInstallation implements
        NodeSpecific<BDSInstallation>, EnvironmentSpecific<BDSInstallation> {

    private static final long serialVersionUID = 1L;

    private static final String DISPLAY_NAME = "RAD Studio";

    private static final String NOT_INSTALLATION_DIRECTORY =
            "Not a RAD Studio installation directory.";

    private static final String INITIALIZATION_FILE_NAME =
            "bin" + File.separator + "rsbars.bat";

    private final String commonDir;
    private final String include;
    private final String boostRoot;
    private final String boostRoot64;

    /**
     * Constructs this object with properties.
     *
     * @param name installation name
     * @param home home directory (value of <code>BDS</code>)
     * @param commonDir value of <code>BDSCOMMONDIR</code>
     * @param include value of <code>BDSINCLUDE</code>
     * @param boostRoot value of <code>CG_BOOST_ROOT</code>
     * @param boostRoot64 value of <code>CG_64_BOOST_ROOT</code>
     * @param properties properties for {@link ToolInstallation}
     */
    @DataBoundConstructor
    public BDSInstallation(String name, String home, String commonDir,
            String include, String boostRoot, String boostRoot64,
            List<? extends ToolProperty<?>> properties) {
        super(name, home, properties);
        this.commonDir = commonDir;
        this.include = include;
        this.boostRoot = boostRoot;
        this.boostRoot64 = boostRoot64;
    }

    /**
     * Returns the value of <code>BDSCOMMONDIR</code>
     *
     * @return value of <code>BDSCOMMONDIR</code>
     */
    public String getCommonDir() {
        return commonDir;
    }

    /**
     * Returns the value of <code>BDSINCLUDE</code>
     *
     * @return value of <code>BDSINCLUDE</code>
     */
    public String getInclude() {
        return include;
    }

    /**
     * Returns the value of <code>CG_BOOST_ROOT</code>
     *
     * @return value of <code>CG_BOOST_ROOT</code>
     */
    public String getBoostRoot() {
        return boostRoot;
    }

    /**
     * Returns the value of <code>CG_64_BOOST_ROOT</code>
     *
     * @return value of <code>CG_64_BOOST_ROOT</code>
     */
    public String getBoostRoot64() {
        return boostRoot64;
    }

    /**
     * Returns a {@link NodeSpecific} version of this object.
     *
     * @param node a {@link Node} object
     * @param log a {@link TaskListener} object
     * @return {@link NodeSpecific} version of this object
     * @throws IOException
     * @throws InterruptedException
     */
    @Override
    public BDSInstallation forNode(Node node, TaskListener log) throws
            IOException, InterruptedException {
        return new BDSInstallation(getName(), translateFor(node, log),
                getCommonDir(), getInclude(), getBoostRoot(),
                getBoostRoot64(), getProperties().toList());
    }

    /**
     * Returns an {@link EnvironmentSpecific} version of this object.
     *
     * @param env an {@link EnvVar} object
     * @return {@link EnvironmentSpecific} version of this object
     */
    @Override
    public BDSInstallation forEnvironment(EnvVars env) {
        return new BDSInstallation(getName(), env.expand(getHome()),
                env.expand(getCommonDir()), env.expand(getInclude()),
                env.expand(getBoostRoot()), env.expand(getBoostRoot64()),
                getProperties().toList());
    }

    /**
     * Describes {@link BDSInstallation}.
     *
     * @author Kaz Nishimura
     * @since 1.0
     */
    @Extension
    public static class DescriptorImpl extends
            ToolDescriptor<BDSInstallation> {

        public DescriptorImpl() {
            load();
        }

        protected FormValidation checkDirectory(File value) {
            Jenkins jenkins = Jenkins.getInstance();
            assert jenkins != null;
            jenkins.checkPermission(Jenkins.ADMINISTER);

            if (value.getPath().isEmpty()) {
                return FormValidation.ok();
            }
            if (value.isDirectory()) {
                return FormValidation.ok();
            }
            return FormValidation.warning(
                    Messages.ToolDescriptor_NotADirectory(value));
        }

        public FormValidation doCheckCommonDir(@QueryParameter File value) {
            return checkDirectory(value);
        }

        public FormValidation doCheckInclude(@QueryParameter File value) {
            return checkDirectory(value);
        }

        public FormValidation doCheckBoostRoot(@QueryParameter File value) {
            return checkDirectory(value);
        }

        public FormValidation doCheckBoostRoot64(@QueryParameter File value) {
            return checkDirectory(value);
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws
                FormException {
            boolean ready = super.configure(req, json);
            if (ready) {
                save();
            }
            return ready;
        }

        @Override
        protected FormValidation checkHomeDirectory(File home) {
            File rsvars = new File(home, INITIALIZATION_FILE_NAME);
            if (!rsvars.isFile()) {
                return FormValidation.error(NOT_INSTALLATION_DIRECTORY);
            }
            return FormValidation.ok();
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
