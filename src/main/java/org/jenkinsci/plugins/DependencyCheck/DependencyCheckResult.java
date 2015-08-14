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

import com.thoughtworks.xstream.XStream;
import hudson.model.AbstractBuild;
import hudson.model.Run;
import hudson.plugins.analysis.core.BuildHistory;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.core.ResultAction;
import hudson.plugins.analysis.util.model.AnnotationContainer;
import org.jenkinsci.plugins.DependencyCheck.parser.Warning;

/**
 * Represents the results of the DependencyCheck analysis. One instance of
 * this class is persisted for each build via an XML file.
 *
 * @author Steve Springett (steve.springett@owasp.org), based on PmdResult by Ulli Hafner
 */
public class DependencyCheckResult extends BuildResult {

    private static final long serialVersionUID = 7033295368738599221L;

    /**
     * Creates a new instance of {@link DependencyCheckResult}.
     *
     * @param build
     *            the current build as owner of this action
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param result
     *            the parsed result with all annotations
     * @param usePreviousBuildAsReference
     *            determines whether to use the previous build as the reference
     *            build
     * @param useStableBuildAsReference
     *            determines whether only stable builds should be used as
     *            reference builds or not
     *
     * @deprecated use {@link #DependencyCheckResult(Run, String, ParserResult, boolean, boolean)}
     */
    @Deprecated
    public DependencyCheckResult(final AbstractBuild<?, ?> build, final String defaultEncoding, final ParserResult result,
                                 final boolean usePreviousBuildAsReference, final boolean useStableBuildAsReference) {
        this((Run<?, ?>) build, defaultEncoding, result, usePreviousBuildAsReference, useStableBuildAsReference, DependencyCheckResultAction.class);
    }

    /**
     * Creates a new instance of {@link DependencyCheckResult}.
     *
     * @param build
     *            the current build as owner of this action
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param result
     *            the parsed result with all annotations
     * @param usePreviousBuildAsReference
     *            determines whether to use the previous build as the reference
     *            build
     * @param useStableBuildAsReference
     *            determines whether only stable builds should be used as
     *            reference builds or not
     */
    public DependencyCheckResult(final Run<?, ?> build, final String defaultEncoding, final ParserResult result,
                                 final boolean usePreviousBuildAsReference, final boolean useStableBuildAsReference) {
        this(build, defaultEncoding, result, usePreviousBuildAsReference, useStableBuildAsReference, DependencyCheckResultAction.class);
    }

    /**
     * Creates a new instance of {@link DependencyCheckResult}.
     *
     * @param build the current build as owner of this action
     * @param defaultEncoding the default encoding to be used when reading and parsing files
     * @param result the parsed result with all annotations
     * @param usePreviousBuildAsReference the value of usePreviousBuildAsReference
     * @param useStableBuildAsReference determines whether only stable builds should be used as reference builds or not
     * @param actionType the type of the result action
     *
     * @deprecated use {@link #DependencyCheckResult(Run, BuildHistory, ParserResult, String, boolean)}
     */
    @Deprecated
    protected DependencyCheckResult(final AbstractBuild<?, ?> build, final String defaultEncoding, final ParserResult result,
                                    final boolean usePreviousBuildAsReference, final boolean useStableBuildAsReference,
                                    final Class<? extends ResultAction<DependencyCheckResult>> actionType) {
        this((Run<?, ?>) build, new BuildHistory(build, actionType, usePreviousBuildAsReference, useStableBuildAsReference), result, defaultEncoding, true);
    }

    /**
     * Creates a new instance of {@link DependencyCheckResult}.
     *
     * @param build the current build as owner of this action
     * @param defaultEncoding the default encoding to be used when reading and parsing files
     * @param result the parsed result with all annotations
     * @param usePreviousBuildAsReference the value of usePreviousBuildAsReference
     * @param useStableBuildAsReference determines whether only stable builds should be used as reference builds or not
     * @param actionType the type of the result action
     */
    protected DependencyCheckResult(final Run<?, ?> build, final String defaultEncoding, final ParserResult result,
                                    final boolean usePreviousBuildAsReference, final boolean useStableBuildAsReference,
                                    final Class<? extends ResultAction<DependencyCheckResult>> actionType) {
        this(build, new BuildHistory(build, actionType, usePreviousBuildAsReference, useStableBuildAsReference), result, defaultEncoding, true);
    }

    /**
     * @deprecated see {@link #DependencyCheckResult(Run, BuildHistory, ParserResult, String, boolean)}
     */
    @Deprecated
    DependencyCheckResult(final AbstractBuild<?, ?> build, final BuildHistory history, final ParserResult result,
                          final String defaultEncoding, final boolean canSerialize) {

        this((Run<?, ?>) build, history, result, defaultEncoding, canSerialize);
    }

    DependencyCheckResult(final Run<?, ?> build, final BuildHistory history, final ParserResult result,
                          final String defaultEncoding, final boolean canSerialize) {
        super(build, history, result, defaultEncoding);

        if (canSerialize) {
            serializeAnnotations(result.getAnnotations());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHeader() {
        return Messages.ResultAction_Header();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configure(final XStream xstream) {
        xstream.alias("warning", Warning.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSummary() {
        return "Dependency-Check: " + createDefaultSummary(DependencyCheckDescriptor.RESULT_URL, getNumberOfAnnotations(), getNumberOfModules());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String createDeltaMessage() {
        return createDefaultDeltaMessage(DependencyCheckDescriptor.RESULT_URL, getNumberOfNewWarnings(), getNumberOfFixedWarnings());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getSerializationFileName() {
        return "dependencycheck-unaudited-warnings.xml";
    }

    /**
     * {@inheritDoc}
     */
    public String getDisplayName() {
        return Messages.ProjectAction_Name();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<? extends ResultAction<? extends BuildResult>> getResultActionType() {
        return DependencyCheckResultAction.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void attachLabelProvider(final AnnotationContainer container) {
        container.setLabelProvider(new CustomAnnotationsLabelProvider(container.getPackageCategoryTitle()));
    }
}
