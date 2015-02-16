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

import hudson.Launcher;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.core.FilesParser;
import hudson.plugins.analysis.core.HealthAwarePublisher;
import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.util.PluginLogger;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.DependencyCheck.parser.ReportParser;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;

/**
 * Publishes the results of the Dependency-Check analysis  (freestyle project type).
 *
 * @author Steve Springett (steve.springett@owasp.org), based on PmdPublisher by Ulli Hafner
 */
public class DependencyCheckPublisher extends HealthAwarePublisher {

    private static final long serialVersionUID = 7990130928383567597L;

    // Default Dependency-Check report filename pattern.
    private static final String DEFAULT_PATTERN = "**/dependency-check-report.xml";

    // Ant file-set pattern of files to work with.
    private final String pattern;

    /**
     * Creates a new instance of <code>DependencyCheckPublisher</code>.
     *
     * @param healthy
     *            Report health as 100% when the number of warnings is less than
     *            this value
     * @param unHealthy
     *            Report health as 0% when the number of warnings is greater
     *            than this value
     * @param thresholdLimit
     *            determines which warning priorities should be considered when
     *            evaluating the build stability and health
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param useDeltaValues
     *            determines whether the absolute annotations delta or the
     *            actual annotations set difference should be used to evaluate
     *            the build stability
     * @param unstableTotalAll
     *            annotation threshold
     * @param unstableTotalHigh
     *            annotation threshold
     * @param unstableTotalNormal
     *            annotation threshold
     * @param unstableTotalLow
     *            annotation threshold
     * @param unstableNewAll
     *            annotation threshold
     * @param unstableNewHigh
     *            annotation threshold
     * @param unstableNewNormal
     *            annotation threshold
     * @param unstableNewLow
     *            annotation threshold
     * @param failedTotalAll
     *            annotation threshold
     * @param failedTotalHigh
     *            annotation threshold
     * @param failedTotalNormal
     *            annotation threshold
     * @param failedTotalLow
     *            annotation threshold
     * @param failedNewAll
     *            annotation threshold
     * @param failedNewHigh
     *            annotation threshold
     * @param failedNewNormal
     *            annotation threshold
     * @param failedNewLow
     *            annotation threshold
     * @param canRunOnFailed
     *            determines whether the plug-in can run for failed builds, too
     * @param usePreviousBuildAsReference
     *            determines whether to always use the previous build as the reference build
     * @param useStableBuildAsReference
     *            determines whether only stable builds should be used as reference builds or not
     * @param canComputeNew
     *            determines whether new warnings should be computed (with
     *            respect to baseline)
     * @param shouldDetectModules
     *            determines whether module names should be derived from Maven POM or Ant build files
     * @param pattern
     *            Ant file-set pattern to scan for PMD files
     */
    // CHECKSTYLE:OFF
    @SuppressWarnings("PMD.ExcessiveParameterList")
    @DataBoundConstructor
    public DependencyCheckPublisher(final String healthy, final String unHealthy, final String thresholdLimit,
                                    final String defaultEncoding, final boolean useDeltaValues,
                                    final String unstableTotalAll, final String unstableTotalHigh, final String unstableTotalNormal, final String unstableTotalLow,
                                    final String unstableNewAll, final String unstableNewHigh, final String unstableNewNormal, final String unstableNewLow,
                                    final String failedTotalAll, final String failedTotalHigh, final String failedTotalNormal, final String failedTotalLow,
                                    final String failedNewAll, final String failedNewHigh, final String failedNewNormal, final String failedNewLow,
                                    final boolean canRunOnFailed, final boolean usePreviousBuildAsReference, final boolean useStableBuildAsReference,
                                    final boolean shouldDetectModules, final boolean canComputeNew, final String pattern) {
        super(healthy, unHealthy, thresholdLimit, defaultEncoding, useDeltaValues,
                unstableTotalAll, unstableTotalHigh, unstableTotalNormal, unstableTotalLow,
                unstableNewAll, unstableNewHigh, unstableNewNormal, unstableNewLow,
                failedTotalAll, failedTotalHigh, failedTotalNormal, failedTotalLow,
                failedNewAll, failedNewHigh, failedNewNormal, failedNewLow,
                canRunOnFailed, usePreviousBuildAsReference, useStableBuildAsReference,
                shouldDetectModules, canComputeNew, false, DependencyCheckPlugin.PLUGIN_NAME);
        this.pattern = pattern;
    }
    // CHECKSTYLE:ON

    /**
     * Returns the Ant file-set pattern of files to work with.
     *
     * @return Ant file-set pattern of files to work with
     */
    public String getPattern() {
        return pattern;
    }

    @Override
    public Action getProjectAction(final AbstractProject<?, ?> project) {
        return new DependencyCheckProjectAction(project);
    }

    @Override
    public BuildResult perform(final AbstractBuild<?, ?> build, final PluginLogger logger) throws InterruptedException, IOException {
        logger.log("Collecting Dependency-Check analysis files...");
        final FilesParser dcCollector = new FilesParser(DependencyCheckPlugin.PLUGIN_NAME, StringUtils.defaultIfEmpty(getPattern(), DEFAULT_PATTERN),
                new ReportParser(getDefaultEncoding()), shouldDetectModules(), isMavenBuild(build));
        final ParserResult project = build.getWorkspace().act(dcCollector);
        logger.logLines(project.getLogMessages());

        final DependencyCheckResult result = new DependencyCheckResult(build, getDefaultEncoding(), project, usePreviousBuildAsReference(), useOnlyStableBuildsAsReference());
        build.getActions().add(new DependencyCheckResultAction(build, this, result));

        return result;
    }

    @Override
    public DependencyCheckDescriptor getDescriptor() {
        return (DependencyCheckDescriptor) super.getDescriptor();
    }

    /**
     * {@inheritDoc}
     */
    public MatrixAggregator createAggregator(final MatrixBuild build, final Launcher launcher,
                                             final BuildListener listener) {
        return new DependencyCheckAnnotationsAggregator(build, launcher, listener, this, getDefaultEncoding(), usePreviousBuildAsReference(), useOnlyStableBuildsAsReference());
    }
}
