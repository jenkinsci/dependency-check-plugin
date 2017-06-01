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
import hudson.model.Run;
import hudson.plugins.analysis.core.BuildHistory;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.core.ResultAction;
import hudson.plugins.analysis.util.HtmlPrinter;
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
     */
    protected DependencyCheckResult(final Run<?, ?> build,
                                    final String defaultEncoding, final ParserResult result,
                                    final boolean usePreviousBuildAsReference,
                                    final boolean useStableBuildAsReference,
                                    final Class<? extends ResultAction<DependencyCheckResult>> actionType) {
        this(build, new BuildHistory(build, actionType, usePreviousBuildAsReference, useStableBuildAsReference), result, defaultEncoding, true);
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

    @Override
    protected void configure(final XStream xstream) {
        xstream.alias("warning", Warning.class);
    }

    @Override
    public String getSummary() {
        int warnings = getNumberOfWarnings();
        int modules = getNumberOfModules();

        HtmlPrinter summary = new HtmlPrinter();
        String message = createWarningsMessage(warnings);
        if (warnings > 0) {
            summary.append(summary.link(DependencyCheckDescriptor.RESULT_URL, message));
        } else {
            summary.append(message);
        }
        if (modules > 0) {
            summary.append(" ");
            summary.append(createAnalysesMessage(modules));
        } else {
            summary.append(".");
        }
        return summary.toString();
    }

    /**
     * Use the default Jenkins messages for this method
     */
    private static String createAnalysesMessage(final int modules) {
        if (modules == 1) {
            return hudson.plugins.analysis.Messages.ResultAction_OneFile();
        } else {
            return hudson.plugins.analysis.Messages.ResultAction_MultipleFiles(modules);
        }
    }

    private static String createWarningsMessage(final int warnings) {
        if (warnings == 1) {
            return Messages.ResultAction_OneWarning();
        }
        else {
            return Messages.ResultAction_MultipleWarnings(warnings);
        }
    }

    @Override
    protected String createDeltaMessage() {
        HtmlPrinter summary = new HtmlPrinter();
        if (getNumberOfNewWarnings() > 0) {
            summary.append(summary.item(
                    summary.link(DependencyCheckDescriptor.RESULT_URL + "/new", createNewWarningsLinkName(getNumberOfNewWarnings()))));
        }
        if (getNumberOfFixedWarnings() > 0) {
            summary.append(summary.item(
                    summary.link(DependencyCheckDescriptor.RESULT_URL + "/fixed", createFixedWarningsLinkName(getNumberOfFixedWarnings()))));
        }
        return summary.toString();
    }

    private static String createNewWarningsLinkName(final int newWarnings) {
        if (newWarnings == 1) {
            return Messages.ResultAction_OneNewWarning();
        }
        else {
            return Messages.ResultAction_MultipleNewWarnings(newWarnings);
        }
    }

    private static String createFixedWarningsLinkName(final int fixedWarnings) {
        if (fixedWarnings == 1) {
            return Messages.ResultAction_OneFixedWarning();
        }
        else {
            return Messages.ResultAction_MultipleFixedWarnings(fixedWarnings);
        }
    }

    @Override
    protected String getSerializationFileName() {
        return "dependencycheck-unaudited-warnings.xml";
    }

    public String getDisplayName() {
        return Messages.ProjectAction_Name();
    }

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
