package org.jenkinsci.plugins.DependencyCheck.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.jenkinsci.plugins.DependencyCheck.model.Vulnerability.Source;
import org.junit.Test;

public class ReportParserTest {

    @Test(expected = InvocationTargetException.class)
    public void testRejectsExternalEntities() throws Exception {
        ReportParser.parse(getClass().getResourceAsStream("dependency-check-report-external-entities.xml"));
        fail("Should have rejected input with external entities");
    }

    @Test
    public void testNoVulnerabilities() throws Exception {
        List<Finding> findings = ReportParser
                .parse(getClass().getResourceAsStream("dependency-check-report-no-vulnerability.xml"));
        assertNotNull(findings);
        assertEquals(0, findings.size());
    }

    @Test
    public void testTenVulnerabilities() throws Exception {
        List<Finding> findings = ReportParser
                .parse(getClass().getResourceAsStream("dependency-check-report-ten-vulnerabilities.xml"));
        assertNotNull(findings);
        assertEquals(10, findings.size());
    }

    @Test
    public void testVulnerability() throws Exception {
        List<Finding> findings = ReportParser
                .parse(getClass().getResourceAsStream("dependency-check-report-one-vulnerability.xml"));
        assertNotNull(findings);
        Finding finding = findings.get(0);
        assertNotNull(finding);
        Vulnerability vulnerability = finding.getVulnerability();
        assertNotNull(vulnerability);
        assertEquals(Source.NVD, vulnerability.getSource());
        assertEquals("CVE-2019-10088", vulnerability.getName());
        assertEquals("HIGH", vulnerability.getSeverity());
        assertEquals("6.8", vulnerability.getCvssV2().getScore());
        assertEquals("8.8", vulnerability.getCvssV3().getBaseScore());
        assertEquals(Severity.HIGH, finding.getNormalizedSeverity());
    }

}
