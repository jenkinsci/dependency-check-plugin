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
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixRun;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.plugins.analysis.core.AnnotationsAggregator;
import hudson.plugins.analysis.core.HealthDescriptor;
import hudson.plugins.analysis.core.ParserResult;

/**
 * Aggregates {@link DependencyCheckResultAction}s of {@link MatrixRun}s into
 * {@link MatrixBuild}.
 *
 * @author Steve Springett (steve.springett@owasp.org), based on PmdAnnotationsAggregator by Ulli Hafner
 */
public class DependencyCheckAnnotationsAggregator extends AnnotationsAggregator {

    /**
     * Creates a new instance of {@link DependencyCheckAnnotationsAggregator}.
     *
     * @param build                     the matrix build
     * @param launcher                  the launcher
     * @param listener                  the build listener
     * @param healthDescriptor          health descriptor
     * @param defaultEncoding           the default encoding to be used when reading and parsing files
     * @param useStableBuildAsReference determines whether only stable builds should be used as
     *                                  reference builds or not
     */
    public DependencyCheckAnnotationsAggregator(final MatrixBuild build, final Launcher launcher,
                                                final BuildListener listener, final HealthDescriptor healthDescriptor, final String defaultEncoding,
                                                final boolean useStableBuildAsReference) {
        super(build, launcher, listener, healthDescriptor, defaultEncoding, useStableBuildAsReference);
    }

    @Override
    protected Action createAction(final HealthDescriptor healthDescriptor, final String defaultEncoding, final ParserResult aggregatedResult) {
        return new DependencyCheckResultAction(build, healthDescriptor,
                new DependencyCheckResult(build, defaultEncoding, aggregatedResult, useOnlyStableBuildsAsReference()));
    }

    @Override
    protected boolean hasResult(final MatrixRun run) {
        return getAction(run) != null;
    }

    @Override
    protected DependencyCheckResult getResult(final MatrixRun run) {
        return getAction(run).getResult();
    }

    private DependencyCheckResultAction getAction(final MatrixRun run) {
        return run.getAction(DependencyCheckResultAction.class);
    }
}