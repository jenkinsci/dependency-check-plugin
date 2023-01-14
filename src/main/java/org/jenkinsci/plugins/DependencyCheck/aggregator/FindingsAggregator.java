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
package org.jenkinsci.plugins.DependencyCheck.aggregator;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.jenkinsci.plugins.DependencyCheck.model.Finding;
import org.jenkinsci.plugins.DependencyCheck.model.SeverityDistribution;

/**
 * Java class for a simple aggregation of findings
 *
 * @author Martin Müller
 */
public class FindingsAggregator {

    private final SeverityDistribution severityDistribution;
    private final SortedSet<Finding> aggregatedFindings;
    
    public FindingsAggregator(int buildNumber) {
        severityDistribution = new SeverityDistribution(buildNumber);
        aggregatedFindings = new TreeSet<>();
    }

    public void addFindings(List<Finding> findings) {
        for (Finding finding : findings) {
            if (aggregatedFindings.add(finding)) {
            	severityDistribution.add(finding.getNormalizedSeverity());
            }
        }
    }

    public SeverityDistribution getSeverityDistribution() {
        return severityDistribution;
    }

    public List<Finding> getAggregatedFindings() {
        return aggregatedFindings.stream().collect(Collectors.toList());
    }

}
