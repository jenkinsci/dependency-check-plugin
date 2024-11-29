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
package org.jenkinsci.plugins.DependencyCheck.model;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.jenkinsci.plugins.DependencyCheck.model.Vulnerability.Source;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;

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

    @Test
    public void testProjectReferences() throws Exception {
        List<Finding> findings = ReportParser
                .parse(getClass().getResourceAsStream("dependency-check-report-one-vulnerability.xml"));
        assertNotNull(findings);
        Finding finding = findings.get(0);
        assertNotNull(finding);
        List<String> projectReferences = finding.getDependency().getProjectReferences();
        assertNotNull(projectReferences);
        assertEquals(singletonList("example:compile"), projectReferences);
    }

    @Issue("JENKINS-73382")
    @Test
    public void parse_report_v10() throws Exception {
        List<Finding> findings = ReportParser.parse(getClass().getResourceAsStream("dependency-check-report-v10.xml"));
        assertThat(findings).isEmpty();
    }

}
