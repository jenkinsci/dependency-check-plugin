/*
 * This file is part of DependencyCheck Jenkins plugin.
 *
 * DependencyCheck is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * DependencyCheck is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * DependencyCheck. If not, see http://www.gnu.org/licenses/.
 */
package org.jenkinsci.plugins.DependencyCheck.parser;

import hudson.plugins.analysis.core.AbstractAnnotationParser;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;
import org.apache.commons.digester.Digester;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

/**
 * A parser for DependencyCheck XML files.
 *
 * @author Steve Springett
 */
public class ReportParser extends AbstractAnnotationParser
{

    private static final long serialVersionUID = -1906443657161473919L;
    private static final BigDecimal PMD_PRIORITY_MAPPED_TO_HIGH_PRIORITY = new BigDecimal(7.0);
    private static final BigDecimal PMD_PRIORITY_MAPPED_TO_MEDIUM_PRIORITY = new BigDecimal(4.0);
    private static final BigDecimal PMD_PRIORITY_MAPPED_TO_LOW_PRIORITY = new BigDecimal(0.0);


    /**
     * Creates a new instance of {@link ReportParser}.
     */
    public ReportParser()
    {
        super(StringUtils.EMPTY);
    }

    /**
     * Creates a new instance of {@link ReportParser}.
     *
     * @param defaultEncoding the default encoding to be used when reading and parsing files
     */
    public ReportParser(final String defaultEncoding)
    {
        super(defaultEncoding);
    }

    @Override
    public Collection<FileAnnotation> parse(final InputStream file, final String moduleName) throws InvocationTargetException
    {
        try
        {
            // Parse DependencyCheck-Report.xml files compatible with DependencyCheck.xsd v.0.3
            Digester digester = new Digester();
            digester.setValidating(false);
            digester.setClassLoader(ReportParser.class.getClassLoader());

            digester.addObjectCreate("analysis", Analysis.class);

            String depXpath = "analysis/dependencies/dependency";
            digester.addObjectCreate(depXpath, Dependency.class);
            digester.addBeanPropertySetter(depXpath + "/fileName");
            digester.addBeanPropertySetter(depXpath + "/filePath");
            digester.addBeanPropertySetter(depXpath + "/md5");
            digester.addBeanPropertySetter(depXpath + "/sha1");
            digester.addBeanPropertySetter(depXpath + "/description");
            digester.addBeanPropertySetter(depXpath + "/license");

            String vulnXpath = "analysis/dependencies/dependency/vulnerabilities/vulnerability";
            digester.addObjectCreate(vulnXpath, Vulnerability.class);
            digester.addBeanPropertySetter(vulnXpath + "/name");
            digester.addBeanPropertySetter(vulnXpath + "/cvssScore");
            digester.addBeanPropertySetter(vulnXpath + "/severity");
            digester.addBeanPropertySetter(vulnXpath + "/cwe");
            digester.addBeanPropertySetter(vulnXpath + "/description");

            digester.addSetNext(vulnXpath, "addVulnerability");
            digester.addSetNext(depXpath, "addDependency");

            Analysis module = (Analysis) digester.parse(file);
            if (module == null)
            {
                throw new SAXException("Input stream is not a DependencyCheck report file.");
            }

            return convert(module, moduleName);

        }
        catch (IOException exception)
        {
            throw new InvocationTargetException(exception);
        }
        catch (SAXException exception)
        {
            throw new InvocationTargetException(exception);
        }
    }

    /**
     * Converts the internal structure to the annotations API.
     *
     * @param collection the internal maven module
     * @param moduleName name of the maven module
     * @return a maven module of the annotations API
     */
    private Collection<FileAnnotation> convert(final Analysis collection, final String moduleName)
    {
        ArrayList<FileAnnotation> annotations = new ArrayList<FileAnnotation>();

        for (Dependency dependency : collection.getDependencies())
        {
            for (Vulnerability warning : dependency.getVulnerabilities())
            {
                Priority priority;

                if (warning.getCvssScore().compareTo(PMD_PRIORITY_MAPPED_TO_HIGH_PRIORITY) > 0)
                {
                    priority = Priority.HIGH;
                } else if (warning.getCvssScore().compareTo(PMD_PRIORITY_MAPPED_TO_LOW_PRIORITY) < 0)
                {
                    priority = Priority.LOW;
                } else
                {
                    priority = Priority.NORMAL;
                }

                Warning bug = new Warning(priority, createMessage(warning), warning.getCvssScore().toPlainString(), warning.getName());
                bug.setModuleName(moduleName);
                bug.setFileName(dependency.getFileName());
                //bug.setColumnPosition(warning.getBegincolumn(), warning.getEndcolumn());

                try
                {
                    bug.setContextHashCode(createContextHashCode(dependency.getFileName(), 0));
                }
                catch (IOException exception)
                {
                    // ignore and continue
                }

                annotations.add(bug);
            }
        }
        return annotations;
    }

    private String createMessage(final Vulnerability warning)
    {
        String original = warning.getDescription();
        if (StringUtils.endsWith(original, "."))
        {
            return original;
        } else
        {
            return original + ".";
        }
    }
}