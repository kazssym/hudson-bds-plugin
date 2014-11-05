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
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

/**
 * Lets users select RAD Studio installations in their projects.
 *
 * @author Kaz Nishimura
 * @since 1.0
 */
public class BDSInstallation extends ToolInstallation implements
        NodeSpecific<BDSInstallation>, EnvironmentSpecific<BDSInstallation> {

    private static final String DISPLAY_NAME = "RAD Studio";

    private String commonDir;

    @DataBoundConstructor
    public BDSInstallation(String name, String home, String commonDir,
                List<? extends ToolProperty<?>> properties) {
        super(name, home, properties);
        this.commonDir = commonDir;
    }

    public String getCommonDir() {
        return commonDir;
    }

    @Override
    public void buildEnvVars(EnvVars env) {
        super.buildEnvVars(env);
        env.put("BDS", getHome());
        env.put("BDSCOMMONDIR", getCommonDir());
    }

    @Override
    public BDSInstallation forNode(Node node, TaskListener log) throws
            IOException, InterruptedException {
        return new BDSInstallation(getName(), translateFor(node, log),
                getCommonDir(), getProperties().toList());
    }

    @Override
    public BDSInstallation forEnvironment(EnvVars env) {
        return new BDSInstallation(getName(), env.expand(getHome()),
                env.expand(getCommonDir()),
                getProperties().toList()); // TODO: Expand other variables.
    }

    /**
     * Describes {@link BDSInstallation}.
     *
     * @author Kaz Nishimura
     */
    @Extension
    public static class Descriptor extends ToolDescriptor<BDSInstallation> {

        public Descriptor() {
            load();
        }

        public FormValidation doCheckCommonDir(@QueryParameter File value) {
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

        @Override
        public void setInstallations(BDSInstallation... installations) {
            super.setInstallations(installations);
            save();
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
