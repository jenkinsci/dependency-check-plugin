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
package org.jenkinsci.plugins.DependencyCheck;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.CopyOnWrite;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Cause;
import hudson.model.Computer;
import hudson.model.Node;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.triggers.SCMTrigger;
import hudson.util.ArgumentListBuilder;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.DependencyCheck.tools.DependencyCheckInstallation;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Stream;

import static hudson.Util.fixEmptyAndTrim;
import static hudson.Util.replaceMacro;
import static hudson.util.QuotedStringTokenizer.tokenize;

public class DependencyCheckToolBuilder extends Builder implements SimpleBuildStep, Serializable {

    private String odcInstallation;
    private String additionalArguments;
    private boolean skipOnScmChange;
    private boolean skipOnUpstreamChange;

    @DataBoundConstructor
    public DependencyCheckToolBuilder() {
    }

    @SuppressWarnings("unused")
    public String getOdcInstallation() {
        return odcInstallation;
    }

    @DataBoundSetter
    public void setOdcInstallation(String odcInstallation) {
        this.odcInstallation = odcInstallation;
    }

    @SuppressWarnings("unused")
    public String getAdditionalArguments() {
        return additionalArguments;
    }

    @DataBoundSetter
    public void setAdditionalArguments(String additionalArguments) {
        this.additionalArguments = additionalArguments;
    }

    public boolean isSkipOnScmChange() {
        return skipOnScmChange;
    }

    @DataBoundSetter
    public void setSkipOnScmChange(boolean skipOnScmChange) {
        this.skipOnScmChange = skipOnScmChange;
    }

    public boolean isSkipOnUpstreamChange() {
        return skipOnUpstreamChange;
    }

    @DataBoundSetter
    public void setSkipOnUpstreamChange(boolean skipOnUpstreamChange) {
        this.skipOnUpstreamChange = skipOnUpstreamChange;
    }

    /**
     * This method is called whenever the build step is executed.
     */
    @Override
    public void perform(@Nonnull final Run<?, ?> build,
                        @Nonnull final FilePath workspace,
                        @Nonnull final Launcher launcher,
                        @Nonnull final TaskListener listener) throws InterruptedException, IOException {

        final ConsoleLogger logger = new ConsoleLogger(listener);
        // Determine if the build should be skipped or not
        if (isSkip(build, listener, logger)) {
            build.setResult(Result.SUCCESS);
            return;
        }

        final EnvVars env = build.getEnvironment(listener);

        DependencyCheckInstallation installation = findInstallation();
        final String odcScript;
        if (installation == null) {
            logger.log("A Dependency-Check installation was not specified. Please configure the build and specify a Dependency-Check instance to use.");
            build.setResult(Result.FAILURE);
            return;
        }

        // Install Dependency-Check if necessary
        final Computer computer = workspace.toComputer();
        final Node node = computer != null ? computer.getNode() : null;
        if (node == null) {
            logger.log("Not running on a build node.");
            build.setResult(Result.FAILURE);
            return;
        }

        installation = installation.forNode(node, listener);
        installation = installation.forEnvironment(env);
        odcScript = installation.getExecutable(launcher);

        if (odcScript == null) {
            logger.log("Can't retrieve the Dependency-Check wrapper script.");
            build.setResult(Result.FAILURE);
            return;
        }
        ArgumentListBuilder cliArguments = buildArgumentList(odcScript, build, workspace, env);
        int exitCode = launcher.launch()
                .cmds(cliArguments)
                .envs(env)
                .stdout(logger)
                .quiet(true)
                .pwd(workspace)
                .join();
        final boolean success = (exitCode == 0);
        build.setResult(success ? Result.SUCCESS : Result.FAILURE);
    }

    private ArgumentListBuilder buildArgumentList(@Nonnull final String odcScript, @Nonnull final Run<?, ?> build,
                                                  @Nonnull final FilePath workspace, @Nonnull final EnvVars env) {
        final ArgumentListBuilder cliArguments = new ArgumentListBuilder(odcScript);
        if (!additionalArguments.contains("--project")) {
            cliArguments.add("--project",  build.getFullDisplayName());
        }
        if (!additionalArguments.contains("--scan") && !additionalArguments.contains("-s ")) {
            cliArguments.add("--scan", workspace.getRemote());
        }
        if (!additionalArguments.contains("--format") && !additionalArguments.contains("-f ")) {
            cliArguments.add("--format", "XML");
        }
        if (fixEmptyAndTrim(additionalArguments) != null) {
            for (String addArg : tokenize(additionalArguments)) {
                if (fixEmptyAndTrim(addArg) != null) {
                    cliArguments.add(replaceMacro(addArg, env));
                }
            }
        }
        return cliArguments;
    }

    private DependencyCheckInstallation findInstallation() {
        return Stream.of(((DependencyCheckToolBuilderDescriptor) getDescriptor()).getInstallations())
                .filter(installation -> installation.getName().equals(odcInstallation))
                .findFirst().orElse(null);
    }

    /**
     * Determine if the build should be skipped or not
     */
    private boolean isSkip(final Run<?, ?> build, final TaskListener listener, ConsoleLogger logger) {
        boolean skip = false;
        // Determine if the OWASP_DC_SKIP environment variable is set to true
        try {
            skip = Boolean.parseBoolean(build.getEnvironment(listener).get("OWASP_DC_SKIP"));
        } catch (Exception e) { /* throw it away */ }

        // Why was this build triggered? Get the causes and find out.
        @SuppressWarnings("unchecked")
        final List<Cause> causes = build.getCauses();
        for (final Cause cause: causes) {
            // Skip if the build is configured to skip on SCM change and the cause of the build was an SCM trigger
            if (skipOnScmChange && cause instanceof SCMTrigger.SCMTriggerCause) {
                skip = true;
            }
            // Skip if the build is configured to skip on Upstream change and the cause of the build was an Upstream trigger
            if (skipOnUpstreamChange && cause instanceof Cause.UpstreamCause) {
                skip = true;
            }
        }
        // Log a message if being skipped
        if (skip) {
            logger.log("Skipping Dependency-Check analysis");
        }

        return skip;
    }

    @Extension
    @Symbol("dependencycheck")
    public static class DependencyCheckToolBuilderDescriptor extends BuildStepDescriptor<Builder> {

        @CopyOnWrite
        private volatile DependencyCheckInstallation[] installations = new DependencyCheckInstallation[0];

        public DependencyCheckToolBuilderDescriptor() {
            load();
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Invoke Dependency-Check";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @SuppressFBWarnings("EI_EXPOSE_REP")
        public DependencyCheckInstallation[] getInstallations() {
            return installations;
        }

        public void setInstallations(DependencyCheckInstallation... installations) {
            this.installations = installations;
            save();
        }

        @SuppressWarnings("unused")
        public boolean hasInstallationsAvailable() {
            return installations.length > 0;
        }
    }
}
