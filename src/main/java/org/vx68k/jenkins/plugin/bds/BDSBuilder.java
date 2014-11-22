/*
 * BDSBuilder
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
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Computer;
import hudson.model.Node;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;
import org.vx68k.jenkins.plugin.AbstractMSBuildBuilder;
import org.vx68k.jenkins.plugin.bds.BDSInstallation.BDSInstallationDescriptor;
import org.vx68k.jenkins.plugin.bds.resources.Messages;

/**
 * Builds a RAD Studio project or project group.
 *
 * @author Kaz Nishimura
 * @since 2.0
 */
public class BDSBuilder extends AbstractMSBuildBuilder {

    private final String installationName;

    /**
     * Constructs this object with properties..
     *
     * @param projectFile name of a MSBuild project file
     * @param switches MSBuild switches
     * @param installationName name of a RAD Studio installation
     */
    @DataBoundConstructor
    public BDSBuilder(String projectFile, String switches,
            String installationName) {
        super(projectFile, switches);
        this.installationName = installationName;
    }

    /**
     * Returns the name of the RAD Studio installation.
     *
     * @return name of the RAD Studio installation
     */
    public String getInstallationName() {
        return installationName;
    }

    /**
     * Performs a RAD Studio build.
     *
     * @param build an {@link AbstractBuild} object
     * @param launcher a {@link Launcher} object
     * @param listener a {@link BuildListener} object
     * @return true if this object did not detect a failure.
     * @throws InterruptedException
     * @throws IOException
     */
    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
            BuildListener listener) throws InterruptedException, IOException {
        BDSBuilderDescriptor descriptor =
                (BDSBuilderDescriptor) getDescriptor();

        BDSInstallation installation =
                descriptor.getInstallation(getInstallationName());
        if (installation == null) {
            listener.fatalError("Installation not found."); // TODO: I18N.
            return false;
        }

        Node node = Computer.currentComputer().getNode();
        installation = installation.forNode(node, listener);

        EnvVars environment = build.getEnvironment(listener);
        installation = installation.forEnvironment(environment);

        if (installation.getHome().isEmpty()) {
            listener.error("Home is not specified."); // TODO: I38N.
            return false;
        }

        Map<String, String> variables =
                installation.readVariables(build, launcher, listener);
        if (variables == null) {
            // Any error messages must already be printed.
            return false;
        }
        environment.putAll(variables);

        // RAD Stduio sets FrameworkDir with FrameworkVersion appended.
        FilePath framworkHome = new FilePath(launcher.getChannel(),
                environment.get("FrameworkDir"));
        return build(build, launcher, listener, framworkHome, environment);
    }

    /**
     * Describes {@link BDSBuilder}.
     *
     * @author Kaz Nishimura
     * @since 2.0
     */
    @Extension
    public static class BDSBuilderDescriptor extends
            BuildStepDescriptor<Builder> {

        public BDSBuilderDescriptor() {
            super(BDSBuilder.class);
        }

        /**
         * Returns a RAD Studio installation identified by a name.
         *
         * @param name name of a RAD Studio installation
         * @return RAD Studio installation, or <code>null</code> if not found
         * @since 3.0
         */
        public BDSInstallation getInstallation(String name) {
            for (BDSInstallation i : getInstallations()) {
                if (i.getName().equals(name)) {
                    return i;
                }
            }
            return null;
        }

        /**
         * Returns an array of RAD Studio installations.
         * This method uses {@link BDSInstallationDescriptor#getInstallations}
         * to get the installations.
         *
         * @return array of RAD Studio installations
         * @since 3.0
         */
        protected BDSInstallation[] getInstallations() {
            Jenkins application = Jenkins.getInstance();
            return application.getDescriptorByType(
                    BDSInstallationDescriptor.class).getInstallations();
        }

        public ListBoxModel doFillInstallationNameItems() {
            ListBoxModel items = new ListBoxModel();

            for (BDSInstallation i : getInstallations()) {
                items.add(i.getName(), i.getName());
            }
            return items;
        }

        @Override
        public boolean isApplicable(
                Class<? extends AbstractProject> projectType) {
            return true;
        }

        /**
         * Returns the display name of this object.
         *
         * @return the display name
         */
        @Override
        public String getDisplayName() {
            return Messages.getBuilderDisplayName();
        }
    }
}
