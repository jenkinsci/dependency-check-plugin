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

import org.apache.commons.digester3.Digester;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * A parser for DependencyCheck XML files.
 *
 * @author Steve Springett (steve.springett@owasp.org)
 */
public class ReportParser {

    private SeverityDistribution severityDistribution;

    public ReportParser(int buildNumber) {
        this.severityDistribution = new SeverityDistribution(buildNumber);
    }

    public List<Finding> parse(final InputStream file) throws InvocationTargetException {
        try {
            // Parse dependency-check-report.xml files compatible with dependency-check.2.0.xsd
            final Digester digester = new Digester();
            digester.setValidating(false);
            digester.setClassLoader(ReportParser.class.getClassLoader());

            digester.addObjectCreate("analysis", Analysis.class);

            final String depXpath = "analysis/dependencies/dependency";
            digester.addObjectCreate(depXpath, Dependency.class);
            digester.addBeanPropertySetter(depXpath + "/fileName");
            digester.addBeanPropertySetter(depXpath + "/filePath");
            digester.addBeanPropertySetter(depXpath + "/md5");
            digester.addBeanPropertySetter(depXpath + "/sha1");
            digester.addBeanPropertySetter(depXpath + "/sha256");
            digester.addBeanPropertySetter(depXpath + "/description");
            digester.addBeanPropertySetter(depXpath + "/license");

            final String vulnXpath = "analysis/dependencies/dependency/vulnerabilities/vulnerability";
            digester.addFactoryCreate(vulnXpath, new VulnerabilityCreationFactory());
            digester.addBeanPropertySetter(vulnXpath + "/name");
            digester.addBeanPropertySetter(vulnXpath + "/description");

            final String cvssV2Xpath = "analysis/dependencies/dependency/vulnerabilities/vulnerability/cvssV2";
            digester.addObjectCreate(cvssV2Xpath, CvssV2.class);
            digester.addBeanPropertySetter(cvssV2Xpath + "/score");
            digester.addBeanPropertySetter(cvssV2Xpath + "/accessVector");
            digester.addBeanPropertySetter(cvssV2Xpath + "/accessComplexity");
            digester.addBeanPropertySetter(cvssV2Xpath + "/authenticationr");
            digester.addBeanPropertySetter(cvssV2Xpath + "/confidentialImpact");
            digester.addBeanPropertySetter(cvssV2Xpath + "/integrityImpact");
            digester.addBeanPropertySetter(cvssV2Xpath + "/availabilityImpact");
            digester.addBeanPropertySetter(cvssV2Xpath + "/severity");

            final String cvssV3Xpath = "analysis/dependencies/dependency/vulnerabilities/vulnerability/cvssV3";
            digester.addObjectCreate(cvssV3Xpath, CvssV3.class);
            digester.addBeanPropertySetter(cvssV3Xpath + "/baseScore");
            digester.addBeanPropertySetter(cvssV3Xpath + "/attackVector");
            digester.addBeanPropertySetter(cvssV3Xpath + "/attackComplexity");
            digester.addBeanPropertySetter(cvssV3Xpath + "/privilegesRequired");
            digester.addBeanPropertySetter(cvssV3Xpath + "/userInteraction");
            digester.addBeanPropertySetter(cvssV3Xpath + "/scope");
            digester.addBeanPropertySetter(cvssV3Xpath + "/confidentialityImpact");
            digester.addBeanPropertySetter(cvssV3Xpath + "/integrityImpact");
            digester.addBeanPropertySetter(cvssV3Xpath + "/availabilityImpact");
            digester.addBeanPropertySetter(cvssV3Xpath + "/baseSeverity");

            final String refXpath = "analysis/dependencies/dependency/vulnexrabilities/vulnerability/references/reference";
            digester.addObjectCreate(refXpath, Reference.class);
            digester.addBeanPropertySetter(refXpath + "/source");
            digester.addBeanPropertySetter(refXpath + "/url");
            digester.addBeanPropertySetter(refXpath + "/name");

            digester.addSetNext(refXpath, "addReference");
            digester.addSetNext(vulnXpath, "addVulnerability");
            digester.addSetNext(depXpath, "addDependency");

            final Analysis analysis = digester.parse(file);
            if (analysis == null) {
                throw new SAXException("Input stream is not a Dependency-Check report file.");
            }
            return convert(analysis);
        } catch (IOException | SAXException e) {
            throw new InvocationTargetException(e);
        }
    }

    /**
     * Converts the dependency-check structure to findings.
     *
     * @param collection the internal maven module
     * @return a List of Finding objects
     */
    private List<Finding> convert(final Analysis collection) {
        final ArrayList<Finding> findings = new ArrayList<>();

        for (Dependency dependency : collection.getDependencies()) {
            for (Vulnerability vulnerability : dependency.getVulnerabilities()) {
                final Finding finding = new Finding(dependency, vulnerability);
                severityDistribution.add(getSeverity(vulnerability));
                findings.add(finding);
            }
        }
        return findings;
    }

    private Severity getSeverity(Vulnerability vulnerability) {
        if (vulnerability.getSeverity() == null) {
            return Severity.UNASSIGNED;
        }
        switch (vulnerability.getSeverity().toUpperCase()) {
            case "CRITICAL":
                return Severity.CRITICAL;
            case "HIGH":
                return Severity.HIGH;
            case "MEDIUM":
                return Severity.MEDIUM;
            case "MODERATE":
                return Severity.MEDIUM;
            case "LOW":
                return Severity.LOW;
            default:
                return Severity.UNASSIGNED;
        }
    }

    public SeverityDistribution getSeverityDistribution() {
        return severityDistribution;
    }
}
