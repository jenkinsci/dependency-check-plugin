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

import hudson.model.AbstractBuild;
import hudson.plugins.analysis.core.AbstractResultAction;
import hudson.plugins.analysis.core.HealthDescriptor;
import hudson.plugins.analysis.core.PluginDescriptor;

/**
 * Controls the live cycle of the DependencyCheck results. This action
 * persists the results of the PMD analysis of a build and displays the
 * results on the build page. The actual visualization of the results
 * is defined in the matching <code>summary.jelly</code> file.
 * <p>
 * Moreover, this class renders the DependencyCheck result trend.
 * </p>
 *
 * @author Steve Springett, based on PmdResultAction by Ulli Hafner
 */
public class DependencyCheckResultAction extends AbstractResultAction<DependencyCheckResult> {

    /**
     * Creates a new instance of <code>DependencyCheckResultAction</code>.
     *
     * @param owner            the associated build of this action
     * @param healthDescriptor health descriptor to use
     * @param result           the result in this build
     */
    public DependencyCheckResultAction(final AbstractBuild<?, ?> owner, final HealthDescriptor healthDescriptor, final DependencyCheckResult result) {
        super(owner, new DependencyCheckHealthDescriptor(healthDescriptor), result);
    }

    /**
     * {@inheritDoc}
     */
    public String getDisplayName() {
        return Messages.ProjectAction_Name();
    }

    @Override
    protected PluginDescriptor getDescriptor() {
        return new DependencyCheckDescriptor();
    }
}