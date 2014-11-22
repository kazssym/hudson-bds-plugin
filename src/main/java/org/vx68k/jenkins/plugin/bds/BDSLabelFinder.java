/*
 * BDSLabelFinder for RAD Studio Plugin
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

import java.util.Collection;
import java.util.HashSet;
import hudson.Extension;
import hudson.model.LabelFinder;
import hudson.model.Node;
import hudson.model.labels.LabelAtom;
import hudson.slaves.NodeProperty;
import hudson.tools.ToolLocationNodeProperty;
import jenkins.model.Jenkins;
import org.vx68k.jenkins.plugin.bds.BDSInstallation.BDSInstallationDescriptor;

/**
 * Adds the names of RAD Studio installations as dynamic labels to a node.
 *
 * @author Kaz Nishimura
 * @since 4.0
 */
@Extension
public class BDSLabelFinder extends LabelFinder {

    public BDSLabelFinder() {
    }

    @Override
    public Collection<LabelAtom> findLabels(Node node) {
        Jenkins application = Jenkins.getInstance();
        BDSInstallationDescriptor descriptor
                = application.getDescriptorByType(
                        BDSInstallationDescriptor.class);
        ToolLocationNodeProperty tools =
                node.getNodeProperties().get(ToolLocationNodeProperty.class);

        HashSet<LabelAtom> labels = new HashSet();
        for (BDSInstallation i : descriptor.getInstallations()) {
            String home = null;
            if (tools != null) {
                home = tools.getHome(i);
            }
            if (home == null) {
                home = i.getHome();
            }
            if (!home.isEmpty()) {
                labels.add(new LabelAtom(i.getName()));
            }
        }
        return labels;
    }
}
