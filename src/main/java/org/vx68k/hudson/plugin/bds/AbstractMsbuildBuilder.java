/*
 * AbstractMsbuildBuilder
 * Copyright (C) 2014-2015 Nishimura Software Studio
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

package org.vx68k.hudson.plugin.bds;

import java.io.IOException;
import java.util.StringTokenizer;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Proc;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
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
    private final String options;

    /**
     * Constructs this object and Sets the immutable properties.
     *
     * @param projectFile name of a MSBuild project file
     * @param options command-line options
     */
    protected AbstractMsbuildBuilder(String projectFile, String options) {
        this.projectFile = projectFile;
        this.options = options;
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
     * Returns the command-line options passed to the constructor.
     *
     * @return command-line options
     */
    public String getOptions() {
        return options;
    }

    /**
     * Returns the file path to a MSBuild executable.
     *
     * @param channel {@link VirtualChannel} object for {@link FilePath}
     * @param env environment variables
     * @return file path to a MSBuild executable, or <code>null</code> if it
     * could not be determined
     */
    protected abstract FilePath getMsbuildPath(
            VirtualChannel channel, EnvVars env);

    /**
     * Builds environment variables for this object.  This method shall be
     * overridden in subclasses if necessary.
     *
     * @param build current build
     * @param launcher {@link Launcher} object
     * @param listener {@link BuildListener} object
     * @param environment environment variables to which new ones are added
     * @throws IOException if an I/O exception has occurred
     * @throws InterruptedException if interrupted
     */
    protected void buildEnvVars(
            AbstractBuild<?, ?> build, Launcher launcher,
            BuildListener listener, EnvVars environment)
            throws IOException, InterruptedException {
    }

    /**
     * Performs the build step.
     *
     * @param build current build
     * @param launcher {@link Launcher} object
     * @param listener {@link BuildListener} object
     * @return <code>true</code> if the current build can be continued, or
     * <code>false</code> otherwise
     * @throws IOException if an I/O exception has occurred
     * @throws InterruptedException if this thread has been interrupted
     */
    @Override
    public boolean perform(
            AbstractBuild<?, ?> build, Launcher launcher,
            BuildListener listener) throws IOException, InterruptedException {
        EnvVars env = build.getEnvironment(listener);
        buildEnvVars(build, launcher, listener, env);
        // Build variables overrides others.
        env.putAll(build.getBuildVariables());

        FilePath msbuildPath = getMsbuildPath(launcher.getChannel(), env);
        if (msbuildPath == null) {
            listener.fatalError(
                    "A MSBuild executable could not be determined."); // TODO: I18N.
            return false;
        }

        Launcher.ProcStarter msbuildStarter = launcher.launch();
        msbuildStarter.envs(env);
        msbuildStarter.pwd(build.getWorkspace());
        msbuildStarter.stdout(listener.getLogger());
        msbuildStarter.stderr(listener.getLogger());

        ArgumentListBuilder args = new ArgumentListBuilder(
                msbuildPath.getRemote());
        StringTokenizer optionsTokenizer = new StringTokenizer(options);
        while (optionsTokenizer.hasMoreTokens()) {
            String option = env.expand(optionsTokenizer.nextToken());
            // TODO: Check every option starts with '/'.
            args.add(option);
        }
        if (!projectFile.isEmpty()) {
            // TODO: Check the project file exists.
            args.add(env.expand(projectFile));
        }
        msbuildStarter.cmds(args.toList());

        Proc msbuildProc = msbuildStarter.start();
        // Any error messages must already be printed.
        return msbuildProc.join() == 0;
    }
}
