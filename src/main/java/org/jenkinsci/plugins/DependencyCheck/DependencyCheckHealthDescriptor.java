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
        if (result.getNumberOfAnnotations() == 0)
            return Messages._ResultAction_HealthReportNoItem();

        else if (result.getNumberOfAnnotations() == 1)
            return Messages._ResultAction_HealthReportSingleItem();

        else
            return Messages._ResultAction_HealthReportMultipleItem(result.getNumberOfAnnotations());
    }
}