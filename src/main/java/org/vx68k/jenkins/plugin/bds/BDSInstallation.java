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
 * Lets users select RAD Studio installations in their projects.
 *
 * @author Kaz Nishimura
 * @since 1.0
 */
public class BDSInstallation extends ToolInstallation implements
        NodeSpecific<BDSInstallation>, EnvironmentSpecific<BDSInstallation> {

    private static final long serialVersionUID = 1L;

    private static final String DISPLAY_NAME = "RAD Studio";

    private final String commonDir;
    private final String include;
    private final String boostRoot;
    private final String boostRoot64;

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

    public String getCommonDir() {
        return commonDir;
    }

    public String getInclude() {
        return include;
    }

    public String getBoostRoot() {
        return boostRoot;
    }

    public String getBoostRoot64() {
        return boostRoot64;
    }

    @Override
    public void buildEnvVars(EnvVars env) {
        super.buildEnvVars(env);
        env.put("BDS", getHome());
        env.put("BDSCOMMONDIR", getCommonDir());
        if (getInclude().isEmpty()) {
            env.put("BDSINCLUDE", env.expand("${BDS}\\include"));
        } else {
            env.put("BDSINCLUDE", getInclude());
        }
    }

    @Override
    public BDSInstallation forNode(Node node, TaskListener log) throws
            IOException, InterruptedException {
        return new BDSInstallation(getName(), translateFor(node, log),
                getCommonDir(), getInclude(), getBoostRoot(),
                getBoostRoot64(), getProperties().toList());
    }

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
     */
    @Extension
    public static class DescriptorImpl extends
            ToolDescriptor<BDSInstallation> {

        public DescriptorImpl() {
            load();
        }

        protected FormValidation checkDirectory(File value) {
            Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);

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
