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

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.DependencyCheck.model.Finding;
import org.jenkinsci.plugins.DependencyCheck.model.ReportParser;
import org.jenkinsci.plugins.DependencyCheck.model.SeverityDistribution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Ported from the Dependency-Track Jenkins plugin
 */
@SuppressWarnings("unused")
public class DependencyCheckPublisher extends ThresholdCapablePublisher implements SimpleBuildStep, Serializable {

    private static final long serialVersionUID = 921545548328565547L;
    private static final String DEFAULT_PATTERN = "dependency-check-report.xml";
    private String pattern;

    @DataBoundConstructor
    public DependencyCheckPublisher() {
    }

    /**
     * Returns the Ant file-set pattern of files to work with.
     *
     * @return Ant file-set pattern of files to work with
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * Sets the Ant file-set pattern of files to work with.
     *
     * @param pattern the pattern of files
     */
    @DataBoundSetter
    public void setPattern(final String pattern) {
        this.pattern = pattern;
    }

    /**
     * This method is called whenever the build step is executed.
     *
     * @param build    A Run object
     * @param filePath A FilePath object
     * @param launcher A Launcher object
     * @param listener A BuildListener object
     */
    @Override
    public void perform(@Nonnull final Run<?, ?> build,
                        @Nonnull final FilePath filePath,
                        @Nonnull final Launcher launcher,
                        @Nonnull final TaskListener listener) throws InterruptedException, IOException {

        final ConsoleLogger logger = new ConsoleLogger(listener);
        logger.log("Collecting Dependency-Check artifact");

        final ReportParser parser = new ReportParser(build.getNumber());
        for (FilePath odcReportFile: filePath.list(DEFAULT_PATTERN)) {
            try {
                final List<Finding> findings = parser.parse(odcReportFile.read());
                final SeverityDistribution severityDistribution = parser.getSeverityDistribution();
                final ResultAction projectAction = new ResultAction(findings, severityDistribution);
                build.addAction(projectAction);
            } catch (InvocationTargetException e) {
                logger.log("Unable to parse " + odcReportFile.getRemote());
            }
        }

        /*
        logger.log(Messages.Builder_Findings_Processing());
        final FindingParser parser = new FindingParser(build.getNumber(), jsonResponseBody).parse();
        final ArrayList<Finding> findings = parser.getFindings();
        final SeverityDistribution severityDistribution = parser.getSeverityDistribution();
        final ResultAction projectAction = new ResultAction(findings, severityDistribution);
        build.addAction(projectAction);

        // Get previous results and evaluate to thresholds
        final Run previousBuild = build.getPreviousBuild();
        if (previousBuild != null) {
            final ResultAction previousResults = previousBuild.getAction(ResultAction.class);
            if (previousResults != null) {
                final RiskGate riskGate = new RiskGate(getThresholds());
                final Result result = riskGate.evaluate(
                        previousResults.getSeverityDistribution(),
                        previousResults.getFindings(),
                        severityDistribution,
                        findings);
                if (Result.SUCCESS != result) {
                    logger.log(Messages.Builder_Threshold_Exceed());
                    build.setResult(result); // only set the result if the evaluation fails the threshold
                }
            }
        }
        */
    }

    @Override
    public Action getProjectAction(AbstractProject<?, ?> project) {
        return new JobAction(project);
    }

    /**
     * A Descriptor Implementation.
     */
    @Override
    public DependencyCheckPublisher.DescriptorImpl getDescriptor() {
        return (DependencyCheckPublisher.DescriptorImpl) super.getDescriptor();
    }

    /**
     * Descriptor for {@link DependencyCheckPublisher}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     * <p/>
     * <p/>
     * See <tt>src/main/resources/org/jenkinsci/plugins/DependencyCheck/DependencyCheckBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension
    @Symbol("dependencyCheckPublisher")
    // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> implements Serializable {

        private static final long serialVersionUID = -1452897801137670635L;

        /**
         * Default constructor. Obtains the Descriptor used in DependencyCheckBuilder as this contains
         * the global Dependency-Check Jenkins plugin configuration.
         */
        public DescriptorImpl() {
            super(DependencyCheckPublisher.class);
            load();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types
            return true;
        }

        /**
         * This name is used on the build configuration screen.
         */
        @Override
        public String getDisplayName() {
            return Messages.Publisher_Name();
        }

    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }
}