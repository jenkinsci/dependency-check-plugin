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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import org.jenkinsci.plugins.DependencyCheck.model.Dependency;
import org.jenkinsci.plugins.DependencyCheck.model.Finding;
import org.jenkinsci.plugins.DependencyCheck.model.Severity;
import org.jenkinsci.plugins.DependencyCheck.model.SeverityDistribution;
import org.jenkinsci.plugins.DependencyCheck.model.Vulnerability;
import org.junit.Test;

public class FindingsAggregatorTest {

    private Finding createFinding(Severity severity, int idx) {
        Dependency dependency = new Dependency();
        dependency.setFileName(severity.name() + String.valueOf(idx));
        Vulnerability vulnerability = new Vulnerability();
        vulnerability.setSeverity(severity.name());
        return new Finding(dependency, vulnerability);
    }

    private List<Finding> createFindings(int critical, int high, int medium, int low, int info, int unassigned) {
        List<Finding> findings = new ArrayList<Finding>();
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
    public void testAggregateFindingsOfSingleReport() throws Exception {

        FindingsAggregator findingsAggregator = new FindingsAggregator(1);

        findingsAggregator.addFindings(createFindings(1, 2, 3, 4, 5, 0));

        List<Finding> aggregatedfindings = findingsAggregator.getAggregatedFindings();
        assertNotNull(aggregatedfindings);
        assertEquals(15, aggregatedfindings.size());

        SeverityDistribution severityDistribution = findingsAggregator.getSeverityDistribution();
        assertNotNull(severityDistribution);
        assertEquals("Severity distribution critial is not as expected.", 1, severityDistribution.getCritical());
        assertEquals("Severity distribution high is not as expected.", 2, severityDistribution.getHigh());
        assertEquals("Severity distribution medium is not as expected.", 3, severityDistribution.getMedium());
        assertEquals("Severity distribution low is not as expected.", 4, severityDistribution.getLow());
        assertEquals("Severity distribution info is not as expected.", 0, severityDistribution.getInfo());
        assertEquals("Severity distribution unassigned is not as expected.", 5, severityDistribution.getUnassigned());
    }

    @Test
    public void testAggregateFindingsOfMultipleReports() throws Exception {

        FindingsAggregator findingsAggregator = new FindingsAggregator(1);

        findingsAggregator.addFindings(createFindings(1, 2, 3, 4, 5, 0));
        findingsAggregator.addFindings(createFindings(5, 0, 1, 9, 2, 1));
        findingsAggregator.addFindings(createFindings(0, 1, 0, 2, 1, 0));

        List<Finding> aggregatedfindings = findingsAggregator.getAggregatedFindings();
        assertNotNull(aggregatedfindings);
        assertEquals(25, aggregatedfindings.size());

        SeverityDistribution severityDistribution = findingsAggregator.getSeverityDistribution();
        assertNotNull(severityDistribution);
        assertEquals("Severity distribution critial is not as expected.", 5, severityDistribution.getCritical());
        assertEquals("Severity distribution high is not as expected.", 2, severityDistribution.getHigh());
        assertEquals("Severity distribution medium is not as expected.", 3, severityDistribution.getMedium());
        assertEquals("Severity distribution low is not as expected.", 9, severityDistribution.getLow());
        assertEquals("Severity distribution info is not as expected.", 0, severityDistribution.getInfo());
        assertEquals("Severity distribution unassigned is not as expected.", 6, severityDistribution.getUnassigned());
    }

    @Test
    public void test_count_of_aggregation_findings() throws Exception {
        FindingsAggregator findingsAggregator = new FindingsAggregator(1);

        findingsAggregator.addFindings(createFindings(1, 2, 3, 4, 5, 0));
        findingsAggregator.addFindings(createFindings(5, 0, 1, 9, 2, 1));
        findingsAggregator.addFindings(createFindings(0, 1, 0, 2, 1, 0));
        findingsAggregator.addFindings(createFindings(0, 0, 0, 0, 1, 0));

        List<Finding> aggredatedFindings = findingsAggregator.getAggregatedFindings();
        assertThat(aggredatedFindings) //
                .filteredOn(finding -> "CRITICAL1".equals(finding.getDependency().getFileName())) //
                .first() //
                .satisfies(finding -> assertThat(finding.getCount()).isEqualTo(2));
        assertThat(aggredatedFindings) //
                .filteredOn(finding -> "LOW1".equals(finding.getDependency().getFileName())) //
                .first() //
                .satisfies(finding -> assertThat(finding.getCount()).isEqualTo(3));
        assertThat(aggredatedFindings) //
                .filteredOn(finding -> "LOW2".equals(finding.getDependency().getFileName())) //
                .first() //
                .satisfies(finding -> assertThat(finding.getCount()).isEqualTo(3));
        assertThat(aggredatedFindings) //
                .filteredOn(finding -> "LOW3".equals(finding.getDependency().getFileName())) //
                .first() //
                .satisfies(finding -> assertThat(finding.getCount()).isEqualTo(2));
        assertThat(aggredatedFindings) //
                .filteredOn(finding -> "INFO1".equals(finding.getDependency().getFileName())) //
                .first() //
                .satisfies(finding -> assertThat(finding.getCount()).isEqualTo(4));
    }

}
