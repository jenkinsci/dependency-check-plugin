package org.jenkinsci.plugins.DependencyCheck.model;

import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

public class ReportParserTest {
    @Test(expected = InvocationTargetException.class)
    public void testRejectsExternalEntities() throws Exception
    {
        new ReportParser(1).parse(getClass().getResourceAsStream("dependency-check-report-external-entities.xml"));

        fail("Should have rejected input with external entities");
    }
}
