/*
 * BDSBuilderDescriptor for RAD Studio Plugin for Jenkins
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
import org.vx68k.jenkins.plugin.bds.BDSInstallation.BDSInstallationDescriptor;
import org.vx68k.jenkins.plugin.bds.resources.Messages;

/**
 * Describes {@link BDSBuilder}.
 *
 * @author Kaz Nishimura
 * @since 4.0
 */
@Extension
public class BDSBuilderDescriptor extends BuildStepDescriptor<Builder> {

    public BDSBuilderDescriptor() {
        super(BDSBuilder.class);
    }

    public BDSBuilderDescriptor(Class<? extends Builder> clazz) {
        super(clazz);
    }

    /**
     * Returns a RAD Studio installation identified by a name.
     *
     * @param name name of a RAD Studio installation
     * @return RAD Studio installation, or <code>null</code> if not found
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
    public boolean isApplicable(Class<? extends AbstractProject> type) {
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
