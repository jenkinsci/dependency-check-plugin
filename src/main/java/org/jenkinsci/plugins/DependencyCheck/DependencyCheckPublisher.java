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

import hudson.FilePath;
import hudson.Launcher;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.model.BuildListener;
import hudson.model.Run;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.core.FilesParser;
import hudson.plugins.analysis.core.HealthAwarePublisher;
import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.util.PluginLogger;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.DependencyCheck.parser.ReportParser;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

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
    private String pattern;

    /**
     * Constructor used from methods like {@link StaplerRequest#bindJSON(Class, JSONObject)} (Class, JSONObject)} and
     * {@link StaplerRequest#bindParameters(Class, String)}.
     */
    @DataBoundConstructor
    public DependencyCheckPublisher() {
        super(DependencyCheckPlugin.PLUGIN_NAME);
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

    @Override
    public BuildResult perform(final Run<?, ?> build, final FilePath workspace, final PluginLogger logger) throws InterruptedException, IOException {
        logger.log("Collecting Dependency-Check analysis files...");

        FilesParser parser = new FilesParser(DependencyCheckPlugin.PLUGIN_NAME, StringUtils.defaultIfEmpty(getPattern(), DEFAULT_PATTERN),
                new ReportParser(getDefaultEncoding()), shouldDetectModules(), isMavenBuild(build));

        ParserResult project = workspace.act(parser);
        logger.logLines(project.getLogMessages());

        DependencyCheckResult result = new DependencyCheckResult(build, getDefaultEncoding(), project, usePreviousBuildAsReference(), useOnlyStableBuildsAsReference());
        build.addAction(new DependencyCheckResultAction(build, this, result));

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
