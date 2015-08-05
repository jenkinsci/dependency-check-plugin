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
package org.jenkinsci.plugins.DependencyCheck.dashboard;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.plugins.analysis.core.AbstractProjectAction;
import hudson.plugins.analysis.dashboard.AbstractWarningsTablePortlet;
import hudson.plugins.view.dashboard.DashboardPortlet;

import org.jenkinsci.plugins.DependencyCheck.DependencyCheckProjectAction;
import org.jenkinsci.plugins.DependencyCheck.Messages;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * A portlet that shows a table with the number of warnings in a job.
 *
 * @author Steve Springett (steve.springett@owasp.org), based on WarningsTablePortlet by Ulli Hafner
 */
public class WarningsTablePortlet extends AbstractWarningsTablePortlet {
    /**
     * Creates a new instance of {@link WarningsTablePortlet}.
     *
     * @param name
     *            the name of the portlet
     * @param canHideZeroWarningsProjects
     *            determines if zero warnings projects should be hidden in the
     *            table
     */
    @DataBoundConstructor
    public WarningsTablePortlet(final String name, final boolean canHideZeroWarningsProjects) {
        super(name, canHideZeroWarningsProjects);
    }

    @Override
    protected Class<? extends AbstractProjectAction<?>> getAction() {
        return DependencyCheckProjectAction.class;
    }

    /**
     * Extension point registration.
     *
     * @author Ulli Hafner
     */
    @Extension(optional = true)
    public static class WarningsPerJobDescriptor extends Descriptor<DashboardPortlet> {
        @Override
        public String getDisplayName() {
            return Messages.Portlet_WarningsTable();
        }
    }
}
