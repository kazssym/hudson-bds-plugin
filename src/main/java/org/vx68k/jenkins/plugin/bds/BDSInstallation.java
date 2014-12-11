/*
 * BDSInstallation for backward compatibility
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

import java.util.List;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;
import hudson.tools.ToolProperty;
import hudson.model.Hudson;
import org.vx68k.hudson.plugin.bds.resources.Messages;

/**
 * Deprecated RAD Studio installation.  This class is retained for backward
 * compatibility.
 *
 * @author Kaz Nishimura
 * @since 1.0
 * @deprecated As of version 4.0, replaced by {@link
 * org.vx68k.hudson.plugin.bds.BDSInstallation}
 */
public class BDSInstallation
        extends org.vx68k.hudson.plugin.bds.BDSInstallation {

    private static final long serialVersionUID = 3L;

    /**
     * Constructs this object with immutable properties.
     *
     * @param name name of this installation
     * @param home home directory (the value of <code>BDS</code>)
     * @param properties properties for {@link ToolInstallation}
     */
    @DataBoundConstructor
    public BDSInstallation(String name, String home,
            List<? extends ToolProperty<?>> properties) {
        super(name, home, properties);
    }

    /**
     * Converts this object to {@link
     * org.vx68k.hudson.plugin.bds.BDSInstallation}.
     *
     * @return {@link org.vx68k.hudson.plugin.bds.BDSInstallation} object
     */
    public org.vx68k.hudson.plugin.bds.BDSInstallation convert() {
        return new org.vx68k.hudson.plugin.bds.BDSInstallation(getName(),
                getHome(), getProperties().toList());
    }

    /**
     * Describes deprecated {@link BDSInstallation}.
     *
     * @author Kaz Nishimura
     * @since 2.0
     */
    @Extension
    public static class BDSInstallationDescriptor
            extends ToolDescriptor<BDSInstallation> {

        public BDSInstallationDescriptor() {
            Hudson application = Hudson.getInstance();
            org.vx68k.hudson.plugin.bds.BDSInstallation.Descriptor descriptor
                    = application.getDescriptorByType(
                            org.vx68k.hudson.plugin.bds.BDSInstallation.Descriptor.class);
            // {@link ToolDescriptor#installations} can be <code>null</code>
            // when there is no configuration
            setInstallations(new BDSInstallation[0]);
            load();
            if (descriptor.getInstallations().length == 0) {
                    BDSInstallation[] installations = getInstallations();
                org.vx68k.hudson.plugin.bds.BDSInstallation[] newInstallations
                        = new org.vx68k.hudson.plugin.bds.BDSInstallation[installations.length];
                for (int i = 0; i != installations.length; ++i) {
                    newInstallations[i] = installations[i].convert();
                }
                descriptor.setInstallations(newInstallations);
            }
        }

        /**
         * Returns the display name for {@link BDSInstallation}.
         *
         * @return display name for {@link BDSInstallation}
         */
        @Override
        public String getDisplayName() {
            return "RAD Studio \u2013 " + Messages.getDeprecated();
        }
    }
}
