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

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.EnvironmentSpecific;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.slaves.NodeSpecific;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;
import hudson.tools.ToolInstaller;
import hudson.tools.ToolProperty;
import jenkins.model.Jenkins;
import jenkins.security.MasterToSlaveCallable;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.DependencyCheck.DependencyCheckToolBuilder;
import org.kohsuke.stapler.DataBoundConstructor;

public class DependencyCheckInstallation extends ToolInstallation implements EnvironmentSpecific<DependencyCheckInstallation>, NodeSpecific<DependencyCheckInstallation>, Serializable {

    private static final long serialVersionUID = 1L;

    @DataBoundConstructor
    public DependencyCheckInstallation(@Nonnull String name, @Nonnull String home, List<? extends ToolProperty<?>> properties) {
        super(name, home, properties);
    }

    @Override
    public DependencyCheckInstallation forEnvironment(EnvVars environment) {
        return new DependencyCheckInstallation(getName(), environment.expand(getHome()), getProperties().toList());
    }

    @Override
    public DependencyCheckInstallation forNode(@Nonnull Node node, TaskListener log) throws IOException, InterruptedException {
        return new DependencyCheckInstallation(getName(), translateFor(node, log), getProperties().toList());
    }

    public String getExecutable(@Nonnull Launcher launcher) throws IOException, InterruptedException {
        VirtualChannel channel = launcher.getChannel();
        return channel == null ? null : channel.call(new MasterToSlaveCallable<String, IOException>() {
            @Override
            public String call() throws IOException {
                return "dependency-check/bin/dependency-check.sh";
            }
        });
    }

    @Extension
    @Symbol("dependency-check")
    public static class DescriptorImpl extends ToolDescriptor<DependencyCheckInstallation>{

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Dependency-Check";
        }

        @Override
        public List<? extends ToolInstaller> getDefaultInstallers() {
            return Collections.singletonList(new DependencyCheckInstaller(null));
        }

        @Override
        public DependencyCheckInstallation[] getInstallations() {
            Jenkins instance = Jenkins.getInstance();
            return instance.getDescriptorByType(DependencyCheckToolBuilder.DependencyCheckToolBuilderDescriptor.class).getInstallations();
        }

        @Override
        public void setInstallations(DependencyCheckInstallation... installations) {
            Jenkins instance = Jenkins.getInstance();
            instance.getDescriptorByType(DependencyCheckToolBuilder.DependencyCheckToolBuilderDescriptor.class).setInstallations(installations);
        }
    }
}