/*
 * This file is part of Dependency-Check Jenkins plugin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jenkinsci.plugins.DependencyCheck.tools;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
import hudson.model.Computer;
import hudson.model.EnvironmentSpecific;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.slaves.NodeSpecific;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;
import hudson.tools.ToolProperty;
import java.io.File;
import java.io.IOException;
import java.util.List;
import jenkins.security.MasterToSlaveCallable;
import org.apache.commons.lang3.ArrayUtils;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.DependencyCheck.DependencyCheckConstants;
import org.jenkinsci.plugins.DependencyCheck.DependencyCheckToolBuilder.DependencyCheckToolBuilderDescriptor;
import org.jenkinsci.plugins.DependencyCheck.Messages;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Defines a Dependency-Check CLI tool installation.
 *
 * @author Steve Springett (steve.springett@owasp.org)
 * @since 5.0.0
 */
public class DependencyCheckInstallation extends ToolInstallation
        implements EnvironmentSpecific<DependencyCheckInstallation>, NodeSpecific<DependencyCheckInstallation> {

    private static final long serialVersionUID = 6948241591210479899L;

    @SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED", justification = "calculate at runtime, its value depends on the OS where it run")
    private transient Platform platform;

    @DataBoundConstructor
    public DependencyCheckInstallation(@NonNull String name, @NonNull String home, List<? extends ToolProperty<?>> properties) {
        this(name, home, properties, null);
    }

    protected DependencyCheckInstallation(@NonNull String name, @Nullable String home, List<? extends ToolProperty<?>> properties, Platform platform) {
        super(Util.fixEmptyAndTrim(name), Util.fixEmptyAndTrim(home), properties);
        this.platform = platform;
    }

    @Override
    public DependencyCheckInstallation forEnvironment(EnvVars environment) {
        return new DependencyCheckInstallation(getName(), environment.expand(getHome()), getProperties().toList());
    }

    @Override
    public DependencyCheckInstallation forNode(@NonNull Node node, TaskListener log) throws IOException, InterruptedException {
        return new DependencyCheckInstallation(getName(), translateFor(node, log), getProperties().toList(), Platform.of(node));
    }

    @Override
    public void buildEnvVars(EnvVars env) {
        String home = getHome();
        if (home == null) {
            return;
        }
        env.put(DependencyCheckConstants.ENVVAR_DEPENDENCYCHECK_PATH, getBin());
    }

    /**
     * Calculate the dependency check bin folder based on current Node platform. We can't
     * use {@link Computer#currentComputer()} because it's always null in case of
     * pipeline.
     *
     * @return path of the bin folder for the installation tool in the current
     *         Node.
     */
    private String getBin() {
        Platform currentPlatform = null;
        try {
            currentPlatform = getPlatform();
        } catch (DetectionFailedException e) {
            throw new RuntimeException(e);  // NOSONAR
        }
        return getBin(currentPlatform, getHome());
    }

    private static String getBin(Platform currentPlatform, String home) {
        String bin = home;
        switch (currentPlatform) {
        case WINDOWS:
            bin += '\\' + "bin";
            break;
        case LINUX:
        default:
            bin += "/bin";
        }

        return bin;
    }

    private Platform getPlatform() throws DetectionFailedException {
        Platform currentPlatform = platform;

        // missed call method forNode
        if (currentPlatform == null) {
            Computer computer = Computer.currentComputer();
            if (computer != null) {
                Node node = computer.getNode();
                if (node == null) {
                    throw new DetectionFailedException(Messages.Builder_nodeOffline());
                }

                currentPlatform = Platform.of(node);
            } else {
                // pipeline or MasterToSlave use case
                currentPlatform = Platform.current();
            }

            platform = currentPlatform;
        }

        return currentPlatform;
    }

    public String getExecutable(@NonNull Launcher launcher) throws IOException, InterruptedException {
        // DO NOT REMOVE this callable otherwise paths constructed by File
        // and similar API will be based on the master node O.S.
        final VirtualChannel channel = launcher.getChannel();
        if (channel == null) {
            throw new IOException("Unable to get a channel for the launcher");
        }
        return channel.call(new FindExecutableCallable(getHome()));
    }

    @SuppressWarnings("serial")
    private static class FindExecutableCallable extends MasterToSlaveCallable<String, IOException> {
        private static final String CMD = "dependency-check";

        private final String home;

        FindExecutableCallable(String home) {
            this.home = home;
        }

        @Override
        public String call() throws IOException {
            Platform currentPlatform = Platform.current();
            // let java to resolve in a correct manner the path separator on a Jenkins slave node
            File exe = new File(getBin(currentPlatform, home), CMD + currentPlatform.cmdExtension);
            if (exe.exists()) {
                return exe.getPath();
            }
            return null;
        }

    }

    @Extension
    @Symbol("dependency-check")
    public static class DescriptorImpl extends ToolDescriptor<DependencyCheckInstallation> {

        public DescriptorImpl() {
            load();
            if (ArrayUtils.isEmpty(getInstallations())) {
                migrate();
            }
        }

        private void migrate() {
            DependencyCheckToolBuilderDescriptor oldDescriptor = new DependencyCheckToolBuilderDescriptor();
            DependencyCheckInstallation[] installations = oldDescriptor.loadInstalltions();
            if (!ArrayUtils.isEmpty(installations)) {
                setInstallations(installations);
                oldDescriptor.purge();
            }
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Installation_displayName();
        }

        @Override
        public void setInstallations(DependencyCheckInstallation... installations) {
            super.setInstallations(installations);
            /*
             * Invoked when the global configuration page is submitted. If
             * installation are modified programmatically than it's a developer
             * task perform the call to save method on this descriptor.
             */
            save();
        }
    }
}