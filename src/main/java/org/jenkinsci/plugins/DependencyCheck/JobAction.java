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
import hudson.model.Job;
import hudson.model.Run;
import net.sf.json.JSONArray;
import org.jenkinsci.plugins.DependencyCheck.model.SeverityDistribution;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import java.util.ArrayList;
import java.util.List;

/**
 * Ported from the Dependency-Track Jenkins plugin.
 *
 * @author Steve Springett (steve.springett@owasp.org)
 * @since 5.0.0
 */
public class JobAction implements Action {

    private Job<?, ?> project;

    public JobAction(final Job<?, ?> project) {
        this.project = project;
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return "odcTrend";
    }

    public Job<?, ?> getProject() {
        return this.project;
    }

    /**
     * Returns whether the trend chart is visible or not.
     *
     * @return {@code true} if the trend is visible, false otherwise
     */
    public boolean isTrendVisible() {
        final List<? extends Run<?, ?>> builds = project.getBuilds();
        int count = 0;
        for (Run<?, ?> currentBuild : builds) {
            final ResultAction action = currentBuild.getAction(ResultAction.class);
            if (action != null) {
                return true;
            }
            count++;
            if (count == 10) { // Only chart the last 10 builds (max)
                break;
            }
        }
        return false;
    }

    /**
     * Returns the UI model for an ECharts line chart that shows the issues stacked by severity.
     *
     * @return the UI model as JSON
     */
    @JavaScriptMethod
    public JSONArray getSeverityDistributionTrend() {
        final List<SeverityDistribution> severityDistributions = new ArrayList<>();
        final List<? extends Run<?, ?>> builds = project.getBuilds();
        int count = 0;
        for (Run<?, ?> currentBuild : builds) {
            final ResultAction action = currentBuild.getAction(ResultAction.class);
            if (action != null && action.getSeverityDistribution() != null) {
                severityDistributions.add(action.getSeverityDistribution());
            }
            count++;
            if (count == 10) { // Only chart the last 10 builds (max)
                break;
            }
        }
        return JSONArray.fromObject(severityDistributions);
    }
}
