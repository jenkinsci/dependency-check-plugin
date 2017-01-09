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

import hudson.model.Action;
import hudson.model.Run;
import hudson.plugins.analysis.core.AbstractResultAction;
import hudson.plugins.analysis.core.HealthDescriptor;
import hudson.plugins.analysis.core.PluginDescriptor;

import java.util.Collection;

/**
 * Controls the live cycle of the Dependency-Check results. This action
 * persists the results of the Dependency-Check analysis of a build and displays the
 * results on the build page. The actual visualization of the results
 * is defined in the matching <code>summary.jelly</code> file.
 * <p>
 * Moreover, this class renders the DependencyCheck result trend.
 * </p>
 *
 * @author Steve Springett (steve.springett@owasp.org), based on PmdResultAction by Ulli Hafner
 */
public class DependencyCheckResultAction extends AbstractResultAction<DependencyCheckResult> {

    /**
     * Creates a new instance of <code>DependencyCheckResultAction</code>.
     *
     * @param owner            the associated build of this action
     * @param healthDescriptor health descriptor to use
     * @param result           the result in this build
     */
    public DependencyCheckResultAction(final Run<?, ?> owner, final HealthDescriptor healthDescriptor, final DependencyCheckResult result) {
        super(owner, new DependencyCheckHealthDescriptor(healthDescriptor), result);
    }

    @Override
    public Collection<? extends Action> getProjectActions() {
        return asSet(new DependencyCheckProjectAction(getJob()));
    }

    public String getDisplayName() {
        return Messages.ProjectAction_Name();
    }

    @Override
    protected PluginDescriptor getDescriptor() {
        return new DependencyCheckDescriptor();
    }
}
