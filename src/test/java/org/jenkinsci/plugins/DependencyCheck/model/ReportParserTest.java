package org.jenkinsci.plugins.DependencyCheck.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.junit.Test;

public class ReportParserTest {
    @Test(expected = InvocationTargetException.class)
    public void testRejectsExternalEntities() throws Exception
    {
        new ReportParser(1).parse(getClass().getResourceAsStream("dependency-check-report-external-entities.xml"));

        fail("Should have rejected input with external entities");
    }

    @Test
    public void testEqualFindings() throws Exception
    {
        final List<Finding> findings1 = new ReportParser(1).parse(getClass().getResourceAsStream("/org/jenkinsci/plugins/DependencyCheck/parser/dependency-check-report.xml"));
        final List<Finding> findings2 = new ReportParser(2).parse(getClass().getResourceAsStream("/org/jenkinsci/plugins/DependencyCheck/parser/dependency-check-report.xml"));

        assertEquals(findings1, findings2);
    }
}
