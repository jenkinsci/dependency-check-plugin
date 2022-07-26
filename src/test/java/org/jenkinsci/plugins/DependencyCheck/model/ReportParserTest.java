package org.jenkinsci.plugins.DependencyCheck.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.junit.Test;

public class ReportParserTest {

	@Test(expected = InvocationTargetException.class)
	public void testRejectsExternalEntities() throws Exception {
		new ReportParser().parse(getClass().getResourceAsStream("dependency-check-report-external-entities.xml"));
		fail("Should have rejected input with external entities");
	}

	@Test
	public void testParseProject1() throws Exception {
		ReportParser reportParser = new ReportParser();
		reportParser.parse(getClass().getResourceAsStream("dependency-check-report_project1.xml"));

		List<Finding> findings = reportParser.getFindings();
		SeverityDistribution severityDistribution = reportParser.getSeverityDistribution();

		assertNotNull(findings);
		assertEquals(4, findings.size());

		assertNotNull(severityDistribution);
		assertEquals("Severity distribution critial is not as ecpected.", 0, severityDistribution.getCritical());
		assertEquals("Severity distribution high is not as ecpected.", 2, severityDistribution.getHigh());
		assertEquals("Severity distribution medium is not as ecpected.", 0, severityDistribution.getMedium());
		assertEquals("Severity distribution low is not as ecpected.", 2, severityDistribution.getLow());
		assertEquals("Severity distribution info is not as ecpected.", 0, severityDistribution.getInfo());
		assertEquals("Severity distribution unassigned is not as ecpected.", 0, severityDistribution.getUnassigned());
	}
	
	@Test
	public void testParseProject2() throws Exception {
		ReportParser reportParser = new ReportParser();
		reportParser.parse(getClass().getResourceAsStream("dependency-check-report_project2.xml"));

		List<Finding> findings = reportParser.getFindings();
		SeverityDistribution severityDistribution = reportParser.getSeverityDistribution();

		assertNotNull(findings);
		assertEquals(10, findings.size());

		assertNotNull(severityDistribution);
		assertEquals("Severity distribution critial is not as ecpected.", 0, severityDistribution.getCritical());
		assertEquals("Severity distribution high is not as ecpected.", 2, severityDistribution.getHigh());
		assertEquals("Severity distribution medium is not as ecpected.", 7, severityDistribution.getMedium());
		assertEquals("Severity distribution low is not as ecpected.", 1, severityDistribution.getLow());
		assertEquals("Severity distribution info is not as ecpected.", 0, severityDistribution.getInfo());
		assertEquals("Severity distribution unassigned is not as ecpected.", 0, severityDistribution.getUnassigned());
	}
	
	@Test
	public void testParseProject1AndProject2() throws Exception {
		ReportParser reportParser = new ReportParser();
		reportParser.parse(getClass().getResourceAsStream("dependency-check-report_project1.xml"));
		reportParser.parse(getClass().getResourceAsStream("dependency-check-report_project2.xml"));

		List<Finding> findings = reportParser.getFindings();
		SeverityDistribution severityDistribution = reportParser.getSeverityDistribution();

		assertNotNull(findings);
		assertEquals(14, findings.size());

		assertNotNull(severityDistribution);
		assertEquals("Severity distribution critial is not as ecpected.", 0, severityDistribution.getCritical());
		assertEquals("Severity distribution high is not as ecpected.", 4, severityDistribution.getHigh());
		assertEquals("Severity distribution medium is not as ecpected.", 7, severityDistribution.getMedium());
		assertEquals("Severity distribution low is not as ecpected.", 3, severityDistribution.getLow());
		assertEquals("Severity distribution info is not as ecpected.", 0, severityDistribution.getInfo());
		assertEquals("Severity distribution unassigned is not as ecpected.", 0, severityDistribution.getUnassigned());
	}

}
