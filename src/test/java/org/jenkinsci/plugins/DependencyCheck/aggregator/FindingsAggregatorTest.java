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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import org.jenkinsci.plugins.DependencyCheck.model.Dependency;
import org.jenkinsci.plugins.DependencyCheck.model.Finding;
import org.jenkinsci.plugins.DependencyCheck.model.Severity;
import org.jenkinsci.plugins.DependencyCheck.model.SeverityDistribution;
import org.jenkinsci.plugins.DependencyCheck.model.Vulnerability;

import org.junit.jupiter.api.Test;

class FindingsAggregatorTest {

    private static Finding createFinding(Severity severity, int idx) {
        Dependency dependency = new Dependency();
        dependency.setFileName(severity.name() + idx);
        Vulnerability vulnerability = new Vulnerability();
        vulnerability.setSeverity(severity.name());
        return new Finding(dependency, vulnerability);
    }

    private static List<Finding> createFindings(int critical, int high, int medium, int low, int info, int unassigned) {
        List<Finding> findings = new ArrayList<>();
        for (int i = 1; i <= critical; i++) {
            findings.add(createFinding(Severity.CRITICAL, i));
        }
        for (int i = 1; i <= high; i++) {
            findings.add(createFinding(Severity.HIGH, i));
        }
        for (int i = 1; i <= medium; i++) {
            findings.add(createFinding(Severity.MEDIUM, i));
        }
        for (int i = 1; i <= low; i++) {
            findings.add(createFinding(Severity.LOW, i));
        }
        for (int i = 1; i <= info; i++) {
            findings.add(createFinding(Severity.INFO, i));
        }
        for (int i = 1; i <= unassigned; i++) {
            findings.add(createFinding(Severity.UNASSIGNED, i));
        }
        return findings;
    }

    @Test
    void testAggregateFindingsOfSingleReport() {
        FindingsAggregator findingsAggregator = new FindingsAggregator(1);

        findingsAggregator.addFindings(createFindings(1, 2, 3, 4, 5, 0));

        List<Finding> aggregatedFindings = findingsAggregator.getAggregatedFindings();
        assertNotNull(aggregatedFindings);
        assertEquals(15, aggregatedFindings.size());

        SeverityDistribution severityDistribution = findingsAggregator.getSeverityDistribution();
        assertNotNull(severityDistribution);
        assertEquals(1, severityDistribution.getCritical(), "Severity distribution critial is not as expected.");
        assertEquals(2, severityDistribution.getHigh(), "Severity distribution high is not as expected.");
        assertEquals(3, severityDistribution.getMedium(), "Severity distribution medium is not as expected.");
        assertEquals(4, severityDistribution.getLow(), "Severity distribution low is not as expected.");
        assertEquals(0, severityDistribution.getInfo(), "Severity distribution info is not as expected.");
        assertEquals(5, severityDistribution.getUnassigned(), "Severity distribution unassigned is not as expected.");
    }

    @Test
    void testAggregateFindingsOfMultipleReports() {
        FindingsAggregator findingsAggregator = new FindingsAggregator(1);

        findingsAggregator.addFindings(createFindings(1, 2, 3, 4, 5, 0));
        findingsAggregator.addFindings(createFindings(5, 0, 1, 9, 2, 1));
        findingsAggregator.addFindings(createFindings(0, 1, 0, 2, 1, 0));

        List<Finding> aggregatedFindings = findingsAggregator.getAggregatedFindings();
        assertNotNull(aggregatedFindings);
        assertEquals(25, aggregatedFindings.size());

        SeverityDistribution severityDistribution = findingsAggregator.getSeverityDistribution();
        assertNotNull(severityDistribution);
        assertEquals(5, severityDistribution.getCritical(), "Severity distribution critial is not as expected.");
        assertEquals(2, severityDistribution.getHigh(), "Severity distribution high is not as expected.");
        assertEquals(3, severityDistribution.getMedium(), "Severity distribution medium is not as expected.");
        assertEquals(9, severityDistribution.getLow(), "Severity distribution low is not as expected.");
        assertEquals(0, severityDistribution.getInfo(), "Severity distribution info is not as expected.");
        assertEquals(6, severityDistribution.getUnassigned(), "Severity distribution unassigned is not as expected.");
    }

    @Test
    void test_count_of_aggregation_findings() {
        FindingsAggregator findingsAggregator = new FindingsAggregator(1);

        findingsAggregator.addFindings(createFindings(1, 2, 3, 4, 5, 0));
        findingsAggregator.addFindings(createFindings(5, 0, 1, 9, 2, 1));
        findingsAggregator.addFindings(createFindings(0, 1, 0, 2, 1, 0));
        findingsAggregator.addFindings(createFindings(0, 0, 0, 0, 1, 0));

        List<Finding> aggregatedFindings = findingsAggregator.getAggregatedFindings();
        assertThat(aggregatedFindings) //
                .filteredOn(finding -> "CRITICAL1".equals(finding.getDependency().getFileName())) //
                .first() //
                .satisfies(finding -> assertThat(finding.getCount()).isEqualTo(2));
        assertThat(aggregatedFindings) //
                .filteredOn(finding -> "LOW1".equals(finding.getDependency().getFileName())) //
                .first() //
                .satisfies(finding -> assertThat(finding.getCount()).isEqualTo(3));
        assertThat(aggregatedFindings) //
                .filteredOn(finding -> "LOW2".equals(finding.getDependency().getFileName())) //
                .first() //
                .satisfies(finding -> assertThat(finding.getCount()).isEqualTo(3));
        assertThat(aggregatedFindings) //
                .filteredOn(finding -> "LOW3".equals(finding.getDependency().getFileName())) //
                .first() //
                .satisfies(finding -> assertThat(finding.getCount()).isEqualTo(2));
        assertThat(aggregatedFindings) //
                .filteredOn(finding -> "INFO1".equals(finding.getDependency().getFileName())) //
                .first() //
                .satisfies(finding -> assertThat(finding.getCount()).isEqualTo(4));
    }

}
