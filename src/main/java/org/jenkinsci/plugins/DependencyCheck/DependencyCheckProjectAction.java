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

import hudson.model.Job;
import hudson.plugins.analysis.core.AbstractProjectAction;
import hudson.plugins.analysis.core.ResultAction;

/**
 * Entry point to visualize the DependencyCheck trend graph in the project
 * screen. Drawing of the graph is delegated to the associated {@link ResultAction}.
 *
 * @author Steve Springett (steve.springett@owasp.org), based on PmdProjectAction by Ulli Hafner
 */
public class DependencyCheckProjectAction extends AbstractProjectAction<ResultAction<DependencyCheckResult>> {

    /**
     * Instantiates a new {@link DependencyCheckProjectAction}.
     *
     * @param job the job that owns this action
     */
    public DependencyCheckProjectAction(final Job<?, ?> job) {
        this(job, DependencyCheckResultAction.class);
    }

    /**
     * Instantiates a new {@link DependencyCheckProjectAction}.
     *
     * @param job the job that owns this action
     * @param type    the result action type
     */
    public DependencyCheckProjectAction(final Job<?, ?> job,
                                        final Class<? extends ResultAction<DependencyCheckResult>> type) {
        super(job, type, Messages._ProjectAction_Name(), Messages._Trend_Name(),
                DependencyCheckDescriptor.PLUGIN_ID, DependencyCheckDescriptor.ICON_URL, DependencyCheckDescriptor.RESULT_URL);
    }
}
