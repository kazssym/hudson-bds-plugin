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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Proc;
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
import org.vx68k.jenkins.plugin.bds.BDSInstallation.BDSInstallationDescriptor;
import org.vx68k.jenkins.plugin.bds.resources.Messages;

/**
 * Builds a RAD Studio project or project group.
 *
 * @author Kaz Nishimura
 * @since 2.0
 */
public class BDSBuilder extends Builder {

    private static final String DISPLAY_NAME = "Build a RAD Stduio project";

    private static final String MSBUILD_COMMAND_NAME = "MSBuild.exe";

    private static final Pattern SET_COMMAND_PATTERN =
            Pattern.compile("\\s*@?set\\s+([^=]+)=(.*)",
                    Pattern.CASE_INSENSITIVE);

    private final String installationName;
    private final String projectFile;

    @DataBoundConstructor
    public BDSBuilder(String installationName, String projectFile) {
        this.installationName = installationName;
        this.projectFile = projectFile;
    }

    public String getInstallationName() {
        return installationName;
    }

    public String getProjectFile() {
        return projectFile;
    }

    protected Map<String, String> extractVariables(InputStream stream,
            BuildListener listener) throws IOException {
        Map<String, String> variables =
                new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(stream));
        try {
            String line;
            line = reader.readLine();
            while (line != null) {
                Matcher setCommand = SET_COMMAND_PATTERN.matcher(line);
                if (setCommand.matches()) {
                    String key = setCommand.group(1);
                    String value = setCommand.group(2);
                    if (key.startsWith("BDS") ||
                            key.startsWith("Framework") ||
                            key.endsWith("_BOOST_ROOT")) {
                        variables.put(key, value);
                    }
                } else if (!line.isEmpty()) {
                    listener.getLogger()
                            .format("Not a set command: %s\n", line);
                }
                line = reader.readLine();
            }
        } finally {
            reader.close();
        }
        return variables;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
            BuildListener listener) throws InterruptedException, IOException {
        BDSBuilderDescriptor descriptor =
                (BDSBuilderDescriptor) getDescriptor();

        BDSInstallation bds = descriptor.getInstallationDescriptor()
                .getInstallation(getInstallationName());
        if (bds == null) {
            listener.error("Installation not found.");
            return false;
        }

        Node node = Computer.currentComputer().getNode();
        bds = bds.forNode(node, listener);

        EnvVars env = build.getEnvironment(listener);
        bds = bds.forEnvironment(env);

        if (bds.getHome().isEmpty()) {
            listener.error("Installation home is not specified.");
            return false;
        }

        Map<String, String> variables;
        FilePath home = new FilePath(launcher.getChannel(), bds.getHome());
        FilePath batch = descriptor.getInstallationDescriptor()
                .getBatchFile(home);
        if (batch.isRemote()) {
            String cmd = env.get("COMSPEC");
            if (cmd == null) {
                listener.error("COMSPEC is not set: "
                        + "this node is probably not Windows.");
                return false;
            }

            Launcher.ProcStarter remoteStarter = launcher.launch();
            remoteStarter.readStdout();
            remoteStarter.stdout(listener.getLogger());
            remoteStarter.cmds(cmd, "/c", "type", batch.getRemote());

            Proc remote = remoteStarter.start();
            variables = extractVariables(remote.getStdout(), listener);

            int status = remote.join();
            if (status != 0) {
                // Any error message must already be printed.
                return false;
            }
        } else {
            variables = extractVariables(batch.read(), listener);
        }

        // TODO: remove this test code.
        for (String key : variables.keySet()) {
            listener.getLogger().format("%s=%s\n", key, variables.get(key));
        }

        // TODO: launch MSBuild.

        return true;
    }

    /**
     * Describes {@link BDSBuildWrapper}.
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

        protected BDSInstallationDescriptor getInstallationDescriptor() {
            Jenkins app = Jenkins.getInstance();
            return app.getDescriptorByType(BDSInstallationDescriptor.class);
        }

        public ListBoxModel doFillInstallationNameItems() {
            ListBoxModel items = new ListBoxModel();

            for (BDSInstallation i :
                    getInstallationDescriptor().getInstallations()) {
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
            return DISPLAY_NAME;
        }
    }
}
