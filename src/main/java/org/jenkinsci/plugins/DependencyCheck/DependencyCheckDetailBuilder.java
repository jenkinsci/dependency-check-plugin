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

import hudson.model.Run;
import hudson.plugins.analysis.util.model.AnnotationContainer;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.views.DetailFactory;
import hudson.plugins.analysis.views.FixedWarningsDetail;
import hudson.plugins.analysis.views.TabDetail;
import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * A detail builder for DependencyCheck annotations capable of showing details of linked annotations.
 *
 * @author Steve Springett (steve.springett@owasp.org)
 */
public class DependencyCheckDetailBuilder extends DetailFactory {

    @Override
    protected TabDetail createTabDetail(@Nonnull final Run<?, ?> owner, final Collection<FileAnnotation> annotations,
                                        final String url, final String defaultEncoding) {
        return new DependencyCheckTabDetail(owner, this, annotations, url, defaultEncoding);
    }

    @Override
    protected void attachLabelProvider(final AnnotationContainer container) {
        container.setLabelProvider(new CustomAnnotationsLabelProvider(container.getPackageCategoryTitle()));
    }

    @Override
    protected FixedWarningsDetail createFixedWarningsDetail(@Nonnull Run<?, ?> owner, Collection<FileAnnotation> fixedAnnotations,
                                                            String defaultEncoding, String displayName) {
        return new FixedVulnerabilityDetail(owner, fixedAnnotations, defaultEncoding, displayName);
    }
}
