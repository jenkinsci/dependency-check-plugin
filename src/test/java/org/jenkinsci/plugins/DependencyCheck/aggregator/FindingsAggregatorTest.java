package org.jenkinsci.plugins.DependencyCheck.aggregator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

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
        for (int i = 0; i < critical; i++) {
            findings.add(createFinding(Severity.CRITICAL, i));
        }
        for (int i = 0; i < high; i++) {
            findings.add(createFinding(Severity.HIGH, i));
        }
        for (int i = 0; i < medium; i++) {
            findings.add(createFinding(Severity.MEDIUM, i));
        }
        for (int i = 0; i < low; i++) {
            findings.add(createFinding(Severity.LOW, i));
        }
        for (int i = 0; i < info; i++) {
            findings.add(createFinding(Severity.INFO, i));
        }
        for (int i = 0; i < unassigned; i++) {
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
    
}
