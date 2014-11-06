/*
 * BDSSelector
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

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Lets users select a RAD Studio installation in their project.
 *
 * @author Kaz Nishimura
 * @since 1.0
 */
public class BDSSelector extends Builder {

    private static final String DISPLAY_NAME =
            "Set RAD Studio environment variables";

    private final String installationName;

    @DataBoundConstructor
    public BDSSelector(String installationName) {
        this.installationName = installationName;
    }

    public String getInstallationName() {
        return installationName;
    }

    /**
     * Describes {@link BDSSelector}.
     *
     * @author Kaz Nishimura
     */
    @Extension
    public static final class Descriptor extends
            BuildStepDescriptor<Builder> {

        public Descriptor() {
            super(BDSSelector.class);
        }

        public ListBoxModel doFillInstallationNameItems() {
            ListBoxModel items = new ListBoxModel();
            Jenkins jenkins = Jenkins.getInstance();
            assert jenkins != null;

            BDSInstallation.Descriptor descriptor;
            descriptor = jenkins.getDescriptorByType(
                    BDSInstallation.Descriptor.class);
            for (BDSInstallation i : descriptor.getInstallations()) {
                items.add(i.getName(), i.getName());
            }
            return items;
        }

        @Override
        public boolean isApplicable(
                Class<? extends AbstractProject> jobType) {
            return true;
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
