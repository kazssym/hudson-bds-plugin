/*
 * BDSBuilder for backward compatibility
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
import hudson.model.Hudson;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ListBoxModel;
import hudson.util.XStream2;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import org.vx68k.hudson.plugin.bds.resources.Messages;
import org.vx68k.jenkins.plugin.bds.BDSInstallation.BDSInstallationDescriptor;

/**
 * Deprecated RAD Studio project or project group builder.  This class is
 * retained for backward compatibility.
 *
 * @author Kaz Nishimura
 * @since 2.0
 * @deprecated As of version 4.0, replaced by {@link
 * org.vx68k.hudson.plugin.bds.BDSBuilder}.
 */
@Deprecated
public class BDSBuilder extends org.vx68k.hudson.plugin.bds.BDSBuilder {

    /**
     * Constructs this object with immutable properties.
     *
     * @param projectFile name of a MSBuild project file
     * @param switches command-line switches
     * @param installationName name of a RAD Studio installation
     */
    public BDSBuilder(String projectFile, String switches,
            String installationName) {
        super(projectFile, switches, installationName);
    }

    /**
     * Converts this object to {@link
     * org.vx68k.hudson.plugin.bds.BDSBuilder}.
     *
     * @return {@link org.vx68k.hudson.plugin.bds.BDSBuilder} object
     * @since 4.0
     */
    protected org.vx68k.hudson.plugin.bds.BDSBuilder convert() {
        return new org.vx68k.hudson.plugin.bds.BDSBuilder(getProjectFile(),
                getSwitches(), getInstallationName());
    }

    /**
     * Converts XML elements for {@link BDSBuilder}.
     *
     * @author Kaz Nishimura
     * @since 4.0
     */
    public static final class ConverterImpl
            extends XStream2.PassthruConverter<BDSBuilder> {

        public ConverterImpl(XStream2 xstream) {
            super(xstream);
        }

        @Override
        public Object unmarshal(HierarchicalStreamReader reader,
                UnmarshallingContext context) {
            Object object = super.unmarshal(reader, context);
            if (object instanceof BDSBuilder) {
                object = ((BDSBuilder) object).convert();
            }
            return object;
        }

        /**
         * Does nothing.
         *
         * @param object a {@link BDSBuilder} object.
         * @param context a {@link UnmarshallingContext} object.
         */
        @Override
        protected void callback(BDSBuilder object,
                UnmarshallingContext context) {
        }
    }

    /**
     * Describes deprecated {@link BDSBuilder}. This class is retained for
     * backward compatibility.
     *
     * @author Kaz Nishimura
     * @since 2.0
     */
    @Extension
    public static final class BDSBuilderDescriptor
            extends BuildStepDescriptor<Builder> {

        /**
         * Returns a deprecated {@link BDSBuilder} object that has a specified name.
         *
         * @param name name of a deprecated {@link BDSBuilder} object
         * @return deprecated {@link BDSBuilder}, or <code>null</code> if not
         * found
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
         * Returns all deprecated {@link BDSBuilder}s.
         * This method uses {@link BDSInstallationDescriptor#getInstallations}
         * to get the installations.
         *
         * @return all deprecated {@link BDSBuilder}s
         * @since 3.0
         */
        protected BDSInstallation[] getInstallations() {
            Hudson application = Hudson.getInstance();
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

        /**
         * Returns <code>false</code> to make deprecated {@link BDSBuilder}
         * hidden from users.
         *
         * @param type {@link Class} object of projects.
         * @return <code>false</code>
         */
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> type) {
            return false;
        }

        /**
         * Returns the display name for deprecated {@link BDSBuilder}.
         *
         * @return display name for deprecated {@link BDSBuilder}
         */
        @Override
        public String getDisplayName() {
            return Messages.getBuilderDisplayName();
        }
    }
}
