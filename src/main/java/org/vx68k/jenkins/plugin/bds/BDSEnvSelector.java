/*
 * BDSEnvSelector
 * Copyright (C) 2014 Kaz Nishimura
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.vx68k.jenkins.plugin.bds;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Lets users select RAD Studio version in their projects.
 *
 * @author Kaz Nishimura
 * @since 1.0
 */
public class BDSEnvSelector extends Builder {

    private static final String DISPLAY_NAME = "RAD Studio Plugin";

    @DataBoundConstructor
    public BDSEnvSelector() {
    }

    /**
     * Describes {@link BDSEnvSelector}.
     *
     * @author Kaz Nishimura
     */
    @Extension
    public static final class Descriptor extends BuildStepDescriptor<Builder> {

        public Descriptor() {
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
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
