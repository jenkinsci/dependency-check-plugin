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

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.lang3.ArrayUtils;
import org.jenkinsci.plugins.DependencyCheck.aggregator.FindingsAggregator;
import org.jenkinsci.plugins.DependencyCheck.model.Finding;
import org.jenkinsci.plugins.DependencyCheck.model.ReportParser;
import org.jenkinsci.plugins.DependencyCheck.model.ReportParserException;
import org.jenkinsci.plugins.DependencyCheck.model.RiskGate;
import org.jenkinsci.plugins.DependencyCheck.model.SeverityDistribution;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ported from the Dependency-Track Jenkins plugin. Not related to the original
 * DependencyCheckPublisher included with v1.0.0 - v4.0.2.
 *
 * @author Steve Springett (steve.springett@owasp.org)
 * @since 5.0.0
 */
public class DependencyCheckPublisher extends AbstractThresholdPublisher implements SimpleBuildStep {

    private static final long serialVersionUID = -3849031519263613214L;
    private static final Logger LOGGER = LoggerFactory.getLogger(DependencyCheckPublisher.class);
    private static final String DEFAULT_PATTERN = "**/dependency-check-report.xml";

    private String pattern;
    private boolean stopBuild = false;
    private boolean ignoreNoResults = false;

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
        this.pattern = Util.fixEmptyAndTrim(pattern);
    }

    @DataBoundSetter
    public void setStopBuild(boolean stopBuild) {
        this.stopBuild = stopBuild;
    }

    public boolean isStopBuild() {
        return stopBuild;
    }

    public boolean isIgnoreNoResults() {
        return ignoreNoResults;
    }

    @DataBoundSetter
    public void setIgnoreNoResults(boolean ignoreNoResults) {
        this.ignoreNoResults = ignoreNoResults;
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
    public void perform(@NonNull final Run<?, ?> build,
                        @NonNull final FilePath filePath,
                        @NonNull final EnvVars env,
                        @NonNull final Launcher launcher,
                        @NonNull final TaskListener listener) throws InterruptedException, IOException {
        Result result = process(build, filePath, launcher, listener);
        if (result.isWorseThan(Result.SUCCESS)) {
            listener.getLogger().println(Messages.Publisher_Threshold_Exceed());
            build.setResult(result); // only set the result if the evaluation fails the threshold
        }
        if (Result.FAILURE == result && stopBuild) {
            throw new AbortException(Messages.Publisher_Threshold_Exceed());
        }
    }

    @Restricted(NoExternalUse.class)
    public Result process(@NonNull final Run<?, ?> build,
                        @NonNull final FilePath filePath,
                        @NonNull final Launcher launcher,
                        @NonNull final TaskListener listener) throws InterruptedException, IOException {
        PrintStream logger = listener.getLogger();
        logger.println(Messages.Publisher_CollectingArtifact());

        if (pattern == null) {
            pattern = DEFAULT_PATTERN;
        }

        Result result = Result.SUCCESS;
        final FilePath[] odcReportFiles = filePath.list(pattern);
        if (ArrayUtils.isEmpty(odcReportFiles)) {
            logger.println(Messages.Publisher_NoArtifactsFound());
            if (ignoreNoResults) {
                return result;
            }
            return Result.UNSTABLE;
        }

        final FindingsAggregator findingsAggregator = new FindingsAggregator(build.getNumber());
        for (FilePath odcReportFile : odcReportFiles) {
            try {
                logger.println(Messages.Publisher_ParsingFile() + " " + odcReportFile.getRemote());

                List<Finding> findings = ReportParser.parse(odcReportFile.read());
                findingsAggregator.addFindings(findings);
            } catch (InvocationTargetException | ReportParserException e) {
                String errorMessage = Messages.Publisher_NotParsable(odcReportFile.getRemote());
                listener.error(errorMessage);
                LOGGER.error(errorMessage, e);
            }
        }

        final SeverityDistribution currentDistribution = findingsAggregator.getSeverityDistribution();
        final List<Finding> findings = findingsAggregator.getAggregatedFindings();
        final ResultAction projectAction = new ResultAction(build, findings, currentDistribution);
        build.addAction(projectAction);

        // Get previous results and evaluate to thresholds
        final RiskGate riskGate = new RiskGate(getThresholds());
        final SeverityDistribution previousDistribution = getPreviousSeverityDistribution(build, currentDistribution);
        final Result reportResult = riskGate.evaluate(previousDistribution, currentDistribution);
        if (reportResult.isWorseThan(result)) {
            result = reportResult;
        }

        return result;
    }

    private SeverityDistribution getPreviousSeverityDistribution(Run<?, ?> build, SeverityDistribution defaultDistribution) {
        final Run<?, ?> previousBuild = build.getPreviousBuild();
        if (previousBuild != null) {
            final ResultAction previousResults = previousBuild.getAction(ResultAction.class);
            if (previousResults != null) {
                return previousResults.getSeverityDistribution();
            }
        }
        return defaultDistribution;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Extension
    // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        /**
         * Default constructor. Obtains the Descriptor used in DependencyCheckBuilder as this contains
         * the global Dependency-Check Jenkins plugin configuration.
         */
        public DescriptorImpl() {
            super(DependencyCheckPublisher.class);
            load();
        }

        @Override
        public boolean isApplicable(@SuppressWarnings("rawtypes") Class<? extends AbstractProject> aClass) {
            return true; // as specified in jenkins.tasks.SimpleBuildStep
        }

        /**
         * This name is used on the build configuration screen.
         */
        @Override
        public String getDisplayName() {
            return Messages.Publisher_Name();
        }

    }

}
