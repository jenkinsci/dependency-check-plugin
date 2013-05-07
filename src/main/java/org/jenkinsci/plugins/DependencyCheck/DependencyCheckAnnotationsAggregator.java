/*
 * This file is part of DependencyCheck Jenkins plugin.
 *
 * DependencyCheck is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * DependencyCheck is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * DependencyCheck. If not, see http://www.gnu.org/licenses/.
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
 * @author Steve Springett, based on PmdAnnotationsAggregator by Ulli Hafner
 */
public class DependencyCheckAnnotationsAggregator extends AnnotationsAggregator {
    /**
     * Creates a new instance of {@link DependencyCheckAnnotationsAggregator}.
     *
     * @param build
     *            the matrix build
     * @param launcher
     *            the launcher
     * @param listener
     *            the build listener
     * @param healthDescriptor
     *            health descriptor
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param useStableBuildAsReference
     *            determines whether only stable builds should be used as
     *            reference builds or not
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

