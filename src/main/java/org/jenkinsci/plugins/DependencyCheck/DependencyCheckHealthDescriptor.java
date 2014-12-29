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

import hudson.plugins.analysis.core.AbstractHealthDescriptor;
import hudson.plugins.analysis.core.HealthDescriptor;
import hudson.plugins.analysis.util.model.AnnotationProvider;
import org.jvnet.localizer.Localizable;

/**
 * A health descriptor for DependencyCheck build results.
 *
 * @author Steve Springett (steve.springett@owasp.org), based on PmdHealthDescriptor by Ulli Hafner
 */
public class DependencyCheckHealthDescriptor extends AbstractHealthDescriptor {

    private static final long serialVersionUID = 5213014036329554062L;

    /**
     * Creates a new instance of {@link DependencyCheckHealthDescriptor} based on the
     * values of the specified descriptor.
     *
     * @param healthDescriptor the descriptor to copy the values from
     */
    public DependencyCheckHealthDescriptor(final HealthDescriptor healthDescriptor) {
        super(healthDescriptor);
    }

    @Override
    protected Localizable createDescription(final AnnotationProvider result) {
        if (result.getNumberOfAnnotations() == 0) {
            return Messages._ResultAction_HealthReportNoItem();
        } else if (result.getNumberOfAnnotations() == 1) {
            return Messages._ResultAction_HealthReportSingleItem();
        } else {
            return Messages._ResultAction_HealthReportMultipleItem(result.getNumberOfAnnotations());
        }
    }
}
