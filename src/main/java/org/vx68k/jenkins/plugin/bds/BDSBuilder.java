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
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.XStream2;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.vx68k.jenkins.plugin.bds.resources.Messages;

/**
 * Builds a RAD Studio project or project group. This class is retained for
 * backward compatibility.
 *
 * @author Kaz Nishimura
 * @since 2.0
 * @deprecated As of version 4.0, replaced by {@link
 * org.vx68k.hudson.plugin.bds.BDSBuilder}.
 */
@Deprecated
public class BDSBuilder extends org.vx68k.hudson.plugin.bds.BDSBuilder {

    /**
     * Constructs this object with properties.
     *
     * @param projectFile name of a MSBuild project file
     * @param switches MSBuild switches
     * @param installationName name of a RAD Studio installation
     */
    public BDSBuilder(String projectFile, String switches,
            String installationName) {
        super(projectFile, switches, installationName);
    }

    /**
     * Returns a {@link org.vx68k.hudson.plugin.bds.BDSBuilder} object
     * that has the same properties as this object.
     *
     * @return new {@link org.vx68k.hudson.plugin.bds.BDSBuilder} object
     */
    protected org.vx68k.hudson.plugin.bds.BDSBuilder translate() {
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
        public void marshal(Object object, HierarchicalStreamWriter writer,
                MarshallingContext context) {
            if (object instanceof BDSBuilder) {
                object = ((BDSBuilder) object).translate();
            }
            super.marshal(object, writer, context);
        }

        @Override
        public Object unmarshal(HierarchicalStreamReader reader,
                UnmarshallingContext context) {
            Object object = super.unmarshal(reader, context);
            if (object instanceof BDSBuilder) {
                object = ((BDSBuilder) object).translate();
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
     * Describes {@link BDSBuilder}. This class is retained for backward
     * compatibility.
     *
     * @author Kaz Nishimura
     * @since 4.0
     */
    @Extension
    public static final class Descriptor
            extends BuildStepDescriptor<Builder> {

        /**
         * Does nothing but constructs this object.
         */
        public Descriptor() {
        }

        /**
         * Returns <code>false</code> to make {@link BDSBuilder} hidden
         * from users.
         *
         * @param type {@link Class} object for projects.
         * @return <code>false</code>
         */
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> type) {
            return false;
        }

        /**
         * Returns the display name for {@link BDSBuilder}.
         *
         * @return display name for {@link BDSBuilder}
         */
        @Override
        public String getDisplayName() {
            return Messages.getBuilderDisplayName();
        }
    }
}
