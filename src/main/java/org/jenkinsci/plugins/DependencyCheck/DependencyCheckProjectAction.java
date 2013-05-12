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

import hudson.model.AbstractProject;
import hudson.plugins.analysis.core.AbstractProjectAction;
import hudson.plugins.analysis.core.ResultAction;

/**
 * Entry point to visualize the DependencyCheck trend graph in the project
 * screen. Drawing of the graph is delegated to the associated {@link ResultAction}.
 *
 * @author Steve Springett, based on PmdProjectAction by Ulli Hafner
 */
public class DependencyCheckProjectAction extends AbstractProjectAction<ResultAction<DependencyCheckResult>> {

    /**
     * Instantiates a new {@link DependencyCheckProjectAction}.
     *
     * @param project the project that owns this action
     */
    public DependencyCheckProjectAction(final AbstractProject<?, ?> project) {
        this(project, DependencyCheckResultAction.class);
    }

    /**
     * Instantiates a new {@link DependencyCheckProjectAction}.
     *
     * @param project the project that owns this action
     * @param type    the result action type
     */
    public DependencyCheckProjectAction(final AbstractProject<?, ?> project,
                                        final Class<? extends ResultAction<DependencyCheckResult>> type) {
        super(project, type, Messages._ProjectAction_Name(), Messages._Trend_Name(),
                DependencyCheckDescriptor.PLUGIN_ID, DependencyCheckDescriptor.ICON_URL, DependencyCheckDescriptor.RESULT_URL);
    }
}