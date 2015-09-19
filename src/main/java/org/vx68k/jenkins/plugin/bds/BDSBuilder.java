/*
 * BDSBuilder (deprecated)
 * Copyright (C) 2014-2025 Nishimura Software Studio
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
import hudson.util.XStream2;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import org.kohsuke.stapler.DataBoundConstructor;
import org.vx68k.jenkins.plugin.bds.resources.Messages;

/**
 * Deprecated RAD Studio builder.
 * This class is retained for backward compatibility.
 * @author Kaz Nishimura
 * @since 2.0
 * @deprecated As of version 4.0, replaced by
 * {@link org.vx68k.hudson.plugin.bds.BDSBuilder}.
 */
@Deprecated
public class BDSBuilder extends Builder {

    private final String projectFile;
    private final String switches;
    private final String installationName;

    /**
     * Constructs this instance with property values.
     * @param projectFile name of the MSBuild project file
     * @param switches command-line options for MSBuild
     * @param installationName name of the RAD Studio installation to use
     */
    @DataBoundConstructor
    public BDSBuilder(
            String projectFile, String switches, String installationName) {
        this.projectFile = projectFile;
        this.switches = switches;
        this.installationName = installationName;
    }

    /**
     * Returns the name of the MSBuild project file.
     * @return name of the MSBuild project file
     * @since 4.0
     */
    public String getProjectFile() {
        return projectFile;
    }

    /**
     * Returns the command-line options for MSBuild.
     * @return command-line options for MSBuild
     * @since 4.0
     */
    public String getSwitches() {
        return switches;
    }

    /**
     * Returns the RAD Studio installation to use.
     * @return RAD Studio installation to use
     */
    public String getInstallationName() {
        return installationName;
    }

    /**
     * Converts this instance to
     * {@link org.vx68k.hudson.plugin.bds.BDSBuilder}.
     * @return {@link org.vx68k.hudson.plugin.bds.BDSBuilder} instance
     * @since 4.0
     */
    protected org.vx68k.hudson.plugin.bds.BDSBuilder convert() {
        return new org.vx68k.hudson.plugin.bds.BDSBuilder(
                projectFile, switches, installationName);
    }

    /**
     * Converter from {@link BDSBuilder} configurations to new ones.
     * @author Kaz Nishimura
     * @since 4.0
     */
    public static final class ConverterImpl
            extends XStream2.PassthruConverter<BDSBuilder> {

        public ConverterImpl(XStream2 xstream) {
            super(xstream);
        }

        @Override
        public Object unmarshal(
                HierarchicalStreamReader reader,
                UnmarshallingContext context) {
            Object object = super.unmarshal(reader, context);
            if (object instanceof BDSBuilder) {
                object = ((BDSBuilder) object).convert();
            }
            return object;
        }

        /**
         * Does nothing.
         * @param object {@link BDSBuilder} instance
         * @param context {@link UnmarshallingContext} instance
         */
        @Override
        protected void callback(
                BDSBuilder object, UnmarshallingContext context) {
        }
    }

    /**
     * Describes {@link BDSBuilder}.
     * This class is retained for backward compatibility.
     * @author Kaz Nishimura
     * @since 2.0
     */
    @Extension
    public static final class BDSBuilderDescriptor
            extends BuildStepDescriptor<Builder> {

        /**
         * Returns <code>false</code> to make the deprecated RAD Studio builder
         * invisible.
         * @param type project type
         * @return <code>false</code>
         */
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> type) {
            return false;
        }

        /**
         * Returns the display name for the deprecated RAD Studio builder.
         * @return display name
         */
        @Override
        public String getDisplayName() {
            return Messages.getBuilderDisplayName();
        }
    }
}
