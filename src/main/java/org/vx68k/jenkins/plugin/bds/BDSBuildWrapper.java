/*
 * BDSBuildWrapper
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
import java.util.Map;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Computer;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import hudson.util.ListBoxModel;
import hudson.util.StreamTaskListener;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;
import org.vx68k.jenkins.plugin.bds.BDSInstallation.BDSInstallationDescriptor;
import org.vx68k.jenkins.plugin.bds.resources.Messages;

/**
 * Allows users to set RAD Studio variables in their projects.
 *
 * @author Kaz Nishimura
 * @since 1.0
 */
public class BDSBuildWrapper extends BuildWrapper {

    private static final String DISPLAY_NAME =
            Messages.BDSBuildWrapper_DISPLAY_NAME();

    private final String installationName;

    @DataBoundConstructor
    public BDSBuildWrapper(String installationName) {
        this.installationName = installationName;
    }

    public String getInstallationName() {
        return installationName;
    }

    @Override
    public void makeBuildVariables(AbstractBuild build,
            Map<String, String> variables) {
        super.makeBuildVariables(build, variables);

        BDSBuildWrapperDescriptor descriptor =
                (BDSBuildWrapperDescriptor) getDescriptor();

        BDSInstallation installation =
                descriptor.getInstallationByName(getInstallationName());
        if (installation == null) {
            throw new IllegalStateException("Installation not found");
        }

        try {
            Node node = Computer.currentComputer().getNode();
            TaskListener listener = StreamTaskListener.fromStderr();
            installation = installation.forNode(node, listener);

            EnvVars env = build.getEnvironment(listener);
            installation = installation.forEnvironment(env);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        variables.put("BDS", installation.getHome());
        variables.put("BDSCOMMONDIR", installation.getCommonDir());
        if (installation.getInclude().isEmpty()) {
            variables.put("BDSINCLUDE", "${BDS}\\include");
        } else {
            variables.put("BDSINCLUDE", installation.getInclude());
        }
        if (installation.getBoostRoot().isEmpty()) {
            variables.put("CG_BOOST_ROOT", "${BDS}\\include\\boost_1_39");
        } else {
            variables.put("CG_BOOST_ROOT", installation.getBoostRoot());
        }
        if (installation.getBoostRoot64().isEmpty()) {
            variables.put("CG_64_BOOST_ROOT", "${BDS}\\include\\boost_1_50");
        } else {
            variables.put("CG_64_BOOST_ROOT", installation.getBoostRoot64());
        }
    }

    @Override
    public Environment setUp(AbstractBuild build, Launcher launcher,
            BuildListener listener) throws IOException, InterruptedException {
        return new Environment() {
            @Override
            public boolean tearDown(AbstractBuild build,
                    BuildListener listener) throws IOException,
                    InterruptedException {
                return true;
            }
        };
    }

    /**
     * Describes {@link BDSBuildWrapper}.
     *
     * @author Kaz Nishimura
     * @since 2.0
     */
    @Extension
    public static class BDSBuildWrapperDescriptor extends
            BuildWrapperDescriptor {

        public BDSBuildWrapperDescriptor() {
            super(BDSBuildWrapper.class);
        }

        public BDSInstallation getInstallationByName(String name) {
            for (BDSInstallation i : getInstallations()) {
                if (i.getName().equals(name)) {
                    return i;
                }
            }
            return null;
        }

        public BDSInstallation[] getInstallations() {
            Jenkins app = Jenkins.getInstance();
            return app.getDescriptorByType(BDSInstallationDescriptor.class)
                    .getInstallations();
        }

        public ListBoxModel doFillInstallationNameItems() {
            ListBoxModel items = new ListBoxModel();

            for (BDSInstallation i : getInstallations()) {
                items.add(i.getName(), i.getName());
            }
            return items;
        }

        @Override
        public boolean isApplicable(AbstractProject<?, ?> project) {
            return true;
        }

        /**
         * Returns the display name of this object.
         *
         * @return the display name
         */
        @Override
        public String getDisplayName() {
            return DISPLAY_NAME;
        }
    }
}
