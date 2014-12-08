/*
 * AbstractMsbuildBuilder
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

package org.vx68k.hudson.plugin;

import java.io.IOException;
import java.util.StringTokenizer;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Proc;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.tasks.Builder;
import hudson.util.ArgumentListBuilder;

/**
 * Abstract builder for MSBuild projects.
 *
 * @author Kaz Nishimura
 * @since 4.0
 */
public abstract class AbstractMsbuildBuilder extends Builder {

    /**
     * Name of the MSBuild executable.
     */
    protected static final String MSBUILD_FILE_NAME = "MSBuild.exe";

    private final String projectFile;
    private final String switches;

    /**
     * Constructs this object and Sets the immutable properties.
     *
     * @param projectFile name of a MSBuild project file
     * @param switches command-line switches
     */
    protected AbstractMsbuildBuilder(String projectFile, String switches) {
        this.projectFile = projectFile;
        this.switches = switches;
    }

    /**
     * Returns the name of the project file passed to the constructor.
     *
     * @return name of the project file
     */
    public String getProjectFile() {
        return projectFile;
    }

    /**
     * Returns the command-line switches passed to the constructor.
     *
     * @return command-line switches
     */
    public String getSwitches() {
        return switches;
    }

    /**
     * Builds environment variables for this object.  This method shall be
     * overridden in subclasses if necessary.
     *
     * @param build {@link AbstractBuild} object
     * @param launcher {@link Launcher} object
     * @param listener {@link BuildListener} object
     * @param environment environment variables to which new ones are added
     * @throws IOException if an I/O exception has occurred
     * @throws InterruptedException if interrupted
     */
    protected void buildEnvVars(AbstractBuild<?, ?> build, Launcher launcher,
            BuildListener listener, EnvVars environment)
            throws IOException, InterruptedException {
    }

    /**
     * Returns the file path to a MSBuild executable.
     *
     * @param channel {@link VirtualChannel} object for {@link FilePath}
     * @param environment environment variables
     * @return file path to a MSBuild executable, or <code>null</code> if it
     * cannot be determined
     */
    protected abstract FilePath getMsbuildPath(VirtualChannel channel,
            EnvVars environment);

    /**
     * Performs this build step.
     *
     * @param build {@link AbstractBuild} object
     * @param launcher {@link Launcher} object
     * @param listener {@link TaskListener} object
     * @return <code>true</code> if the current build can be continued.
     * @throws IOException if an I/O exception has occurred
     * @throws InterruptedException if interrupted
     */
    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
            BuildListener listener) throws IOException, InterruptedException {
        EnvVars environment = build.getEnvironment(listener);
        buildEnvVars(build, launcher, listener, environment);
        // Build variables overrides others.
        environment.putAll(build.getBuildVariables());

        FilePath msbuildPath = getMsbuildPath(launcher.getChannel(),
                environment);
        if (msbuildPath == null) {
            listener.fatalError(
                    "A MSBuild executable could not be determined."); // TODO: I18N.
            return false;
        }

        Launcher.ProcStarter msbuildStarter = launcher.launch();
        msbuildStarter.envs(environment);
        msbuildStarter.stdout(listener.getLogger());
        msbuildStarter.stderr(listener.getLogger());
        msbuildStarter.pwd(build.getWorkspace());

        ArgumentListBuilder args = new ArgumentListBuilder(
                msbuildPath.getRemote());
        StringTokenizer tokenizer = new StringTokenizer(getSwitches());
        while (tokenizer.hasMoreTokens()) {
            args.add(environment.expand(tokenizer.nextToken()));
        }
        if (!getProjectFile().isEmpty()) {
            args.add(environment.expand(getProjectFile()));
        }
        msbuildStarter.cmds(args.toList());

        Proc msbuildProc = msbuildStarter.start();
        // Any error messages must already be printed.
        return msbuildProc.join() == 0;
    }
}
