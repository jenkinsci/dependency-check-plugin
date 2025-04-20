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

import com.cloudbees.plugins.credentials.CredentialsMatcher;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernameListBoxModel;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.XmlFile;
import hudson.model.AbstractProject;
import hudson.model.Cause;
import hudson.model.Computer;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Node;
import hudson.model.Queue;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.queue.Tasks;
import hudson.security.ACL;
import hudson.security.AccessControlled;
import hudson.security.Permission;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.tools.InstallSourceProperty;
import hudson.triggers.SCMTrigger;
import hudson.util.ArgumentListBuilder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.DependencyCheck.tools.DependencyCheckInstallation;
import org.jenkinsci.plugins.DependencyCheck.tools.DependencyCheckInstaller;
import org.jenkinsci.plugins.DependencyCheck.tools.Version;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;
import org.springframework.security.core.Authentication;

import static hudson.Util.fixEmptyAndTrim;
import static hudson.Util.replaceMacro;
import static hudson.util.QuotedStringTokenizer.tokenize;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

/**
 * Performs an analysis using the specified Dependency-Check CLI tool installation.
 *
 * @author Steve Springett (steve.springett@owasp.org)
 * @since 5.0.0
 */
public class DependencyCheckToolBuilder extends Builder implements SimpleBuildStep, Serializable {

    @Serial
    private static final long serialVersionUID = 4267818809512542424L;

    private final String odcInstallation;
    private String additionalArguments;
    private String nvdCredentialsId;
    private boolean skipOnScmChange;
    private boolean skipOnUpstreamChange;
    private boolean stopBuild = false;
    private boolean debug = false;

    @DataBoundConstructor
    public DependencyCheckToolBuilder(final String odcInstallation) {
        this.odcInstallation = fixEmptyAndTrim(odcInstallation);
    }

    public String getOdcInstallation() {
        return odcInstallation;
    }

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

    @DataBoundSetter
    public void setStopBuild(boolean stopBuild) {
        this.stopBuild = stopBuild;
    }

    public boolean isStopBuild() {
        return stopBuild;
    }

    /**
     * This method is called whenever the build step is executed.
     */
    @Override
    public void perform(@NonNull final Run<?, ?> build,
                        @NonNull final FilePath workspace,
                        @NonNull final EnvVars env,
                        @NonNull final Launcher launcher,
                        @NonNull final TaskListener listener) throws InterruptedException, IOException {

        // Determine if the build should be skipped or not
        if (isSkip(build, listener)) {
            return;
        }

        // get specific installation for the node
        DependencyCheckInstallation ni = getDependencyCheck();
        if (ni == null) {
            if (odcInstallation != null) {
                throw new AbortException(Messages.Builder_noInstallationFound(odcInstallation));
            } else {
                // should we delegate to the node installation in the system PATH ??
                throw new AbortException(Messages.Builder_InstallationNotSpecified());
            }
        }

        final Computer computer = workspace.toComputer();
        final Node node = computer != null ? computer.getNode() : null;
        if (node == null) {
            throw new AbortException(Messages.Builder_nodeOffline());
        }

        ni = ni.forNode(node, listener);
        ni = ni.forEnvironment(env);

        final String odcScript = ni.getExecutable(launcher);
        if (odcScript == null) {
            throw new AbortException(Messages.Builder_noExecutableFound(ni.getHome()));
        }

        ArgumentListBuilder cliArguments = buildArgumentList(odcScript, build, workspace, env);
        int exitCode = launcher.launch()
                .cmds(cliArguments)
                .envs(env)
                .stdout(listener.getLogger())
                .quiet(!isDebug())
                .pwd(workspace)
                .join();
        final boolean success = isSuccess(exitCode);
        if (!success) {
            build.setResult(Result.FAILURE);
            if (stopBuild) {
                throw new AbortException(Messages.Publisher_FailBuild());
            } else {
                listener.error("Mark build as failed because of exit code " + exitCode);
            }
        }
    }

    private boolean isSuccess(int exitCode) {
        InstallSourceProperty installSourceProperty = getDependencyCheck().getProperties().get(InstallSourceProperty.class);
        if (installSourceProperty != null) {
            DependencyCheckInstaller ui = installSourceProperty.installers.get(DependencyCheckInstaller.class);
            if (ui != null) {
                Version v = Version.parseVersion(ui.id);
                if (v.getMajor() < 8) {
                    return exitCode == 0;
                }
            }
        }
        // fallback handle exitcode like version 8 or greater
        return exitCode == 0 || exitCode == 14 || exitCode == 15;
    }

    private DependencyCheckInstallation getDependencyCheck() {
        return DependencyCheckUtil.getDependencyCheck(odcInstallation);
    }

    protected ArgumentListBuilder buildArgumentList(@NonNull final String odcScript,
                                                    @NonNull final Run<?, ?> build,
                                                    @NonNull final FilePath workspace,
                                                    @NonNull final EnvVars env) throws AbortException {
        final ArgumentListBuilder cliArguments = new ArgumentListBuilder(odcScript);
        if (!StringUtils.contains(additionalArguments, "--project")) {
            cliArguments.add("--project",  build.getFullDisplayName());
        }
        if (!StringUtils.containsAny(additionalArguments, "--scan", "-s ")) {
            cliArguments.add("--scan", workspace.getRemote());
        }
        if (!StringUtils.containsAny(additionalArguments, "--format", "-f ")) {
            cliArguments.add("--format", "XML");
        }
        if (fixEmptyAndTrim(additionalArguments) != null) {
            for (String addArg : tokenize(additionalArguments)) {
                if (fixEmptyAndTrim(addArg) != null) {
                    cliArguments.add(replaceMacro(addArg, env));
                }
            }
        }
        if (nvdCredentialsId != null) {
            StringCredentials c = CredentialsProvider.findCredentialById(nvdCredentialsId, StringCredentials.class, build);
            if (c == null) {
                throw new AbortException(Messages.Builder_DescriptorImpl_invalidCredentialsId());
            }
            cliArguments.add("--nvdApiKey").addMasked(c.getSecret());
        }
        return cliArguments;
    }

    /**
     * Determine if the build should be skipped or not
     */
    private boolean isSkip(final Run<?, ?> build, final TaskListener listener) {
        boolean skip = false;
        // Determine if the OWASP_DC_SKIP environment variable is set to true
        try {
            skip = Boolean.parseBoolean(build.getEnvironment(listener).get("OWASP_DC_SKIP"));
        } catch (Exception e) { /* throw it away */ }

        // Why was this build triggered? Get the causes and find out.
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
            listener.getLogger().println(Messages.Builder_Skip());
        }

        return skip;
    }

    public String getNvdCredentialsId() {
        return nvdCredentialsId;
    }

    @DataBoundSetter
    public void setNvdCredentialsId(String nvdCredentialsId) {
        this.nvdCredentialsId = Util.fixEmpty(nvdCredentialsId);
    }

    public boolean isDebug() {
        return debug;
    }

    @DataBoundSetter
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    @Extension
    @Symbol({"dependencyCheck", "dependencycheck"})
    public static class DependencyCheckToolBuilderDescriptor extends BuildStepDescriptor<Builder> {

        private DependencyCheckInstallation[] installations = new DependencyCheckInstallation[0];

        public DependencyCheckInstallation[] loadInstalltions() {
            load();
            return installations;
        }

        public void purge() {
            XmlFile globalConfig = getConfigFile();
            FileUtils.deleteQuietly(globalConfig.getFile());
        }

        @POST
        public FormValidation doCheckdoNvdCredentialsId(@CheckForNull @AncestorInPath Item projectOrFolder,
                                                        @QueryParameter String nvdCredentialsId) {
            if ((projectOrFolder == null && !Jenkins.get().hasPermission(Jenkins.ADMINISTER)) ||
                    (projectOrFolder != null && !projectOrFolder.hasPermission(Item.EXTENDED_READ) && !projectOrFolder.hasPermission(CredentialsProvider.USE_ITEM))) {
                return FormValidation.ok();
            }
            if (StringUtils.isBlank(nvdCredentialsId)) {
                return FormValidation.warning(Messages.Builder_DescriptorImpl_emptyCredentialsId());
            }

            Authentication authentication = getAuthentication(projectOrFolder);
            CredentialsMatcher matcher = CredentialsMatchers.withId(nvdCredentialsId);
            if (CredentialsProvider.listCredentialsInItem(StringCredentials.class, projectOrFolder, authentication, null, matcher).isEmpty()) {
                return FormValidation.error(Messages.Builder_DescriptorImpl_invalidCredentialsId());
            }
            return FormValidation.ok();
        }

        @POST
        public ListBoxModel doFillNvdCredentialsIdItems(final @CheckForNull @AncestorInPath ItemGroup<?> context,
                                                        final @CheckForNull @AncestorInPath Item projectOrFolder,
                                                        @QueryParameter String nvdCredentialsId) {
            Permission permToCheck = projectOrFolder == null ? Jenkins.ADMINISTER : Item.CONFIGURE;
            AccessControlled contextToCheck = projectOrFolder == null ? Jenkins.get() : projectOrFolder;

            // If we're on the global page and we don't have administer
            // permission or if we're in a project or folder
            // and we don't have configure permission there
            if (!contextToCheck.hasPermission(permToCheck)) {
                return new StandardUsernameListBoxModel().includeCurrentValue(trimToEmpty(nvdCredentialsId));
            }

            Authentication authentication = getAuthentication(projectOrFolder);
            CredentialsMatcher matcher = CredentialsMatchers.instanceOf(StringCredentials.class);
            Class<StringCredentials> type = StringCredentials.class;
            ItemGroup<?> credentialsContext = context == null ? Jenkins.get() : context;

            return new StandardListBoxModel() //
                    .includeMatchingAs(authentication, credentialsContext, type, Collections.emptyList(), matcher) //
                    .includeEmptyValue();
        }

        @NonNull
        protected Authentication getAuthentication(AccessControlled item) {
            return item instanceof Queue.Task ? Tasks.getAuthenticationOf2((Queue.Task) item) : ACL.SYSTEM2;
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Builder_Name();
        }

        public DependencyCheckInstallation[] getInstallations() {
            return DependencyCheckUtil.getInstallations();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

    }
}
