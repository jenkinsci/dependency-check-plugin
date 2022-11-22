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
package org.jenkinsci.plugins.DependencyCheck.charts;

import java.util.List;

import org.jenkinsci.plugins.DependencyCheck.model.Finding;
import org.jenkinsci.plugins.DependencyCheck.model.SeverityDistribution;

/**
 * DTO class to store vulnerability scan of a single run.
 * 
 * @author Nikolas Falco
 */
public class DependencyCheckBuildResult {
    private List<Finding> findings;
    private SeverityDistribution severityDistribution;

    public DependencyCheckBuildResult(List<Finding> findings, SeverityDistribution severityDistribution) {
        this.findings = findings;
        this.severityDistribution = severityDistribution;
    }

    public List<Finding> getFindings() {
        return findings;
    }

    public void setFindings(List<Finding> findings) {
        this.findings = findings;
    }

    public SeverityDistribution getSeverityDistribution() {
        return severityDistribution;
    }

    public void setSeverityDistribution(SeverityDistribution severityDistribution) {
        this.severityDistribution = severityDistribution;
    }

}
