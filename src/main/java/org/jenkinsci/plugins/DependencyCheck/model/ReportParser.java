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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.digester3.Digester;
import org.jenkinsci.plugins.DependencyCheck.tools.Version;
import org.xml.sax.SAXException;

/**
 * A parser for DependencyCheck XML files.
 *
 * @author Steve Springett (steve.springett@owasp.org)
 * @since 1.0.0
 */
public final class ReportParser {
    private static final Version MIN_VERSION = new Version("5");

    private ReportParser() {
    }

    public static List<Finding> parse(final InputStream file)
            throws InvocationTargetException, ReportParserException {
        List<Finding> findings;
        try {
            // Parse dependency-check-report.xml files compatible with
            // dependency-check.2.0.xsd
            final Digester digester = new Digester();
            digester.setValidating(false);
            digester.setClassLoader(ReportParser.class.getClassLoader());

            digester.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            digester.setFeature("http://xml.org/sax/features/external-general-entities", false);
            digester.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            digester.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            digester.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            digester.addObjectCreate("analysis", Analysis.class);

            final String scanInfoXpath = "analysis/scanInfo";
            digester.addObjectCreate(scanInfoXpath, ScanInfo.class);
            digester.addBeanPropertySetter(scanInfoXpath + "/engineVersion");

            final String projectInfoXpath = "analysis/projectInfo";
            digester.addObjectCreate(projectInfoXpath, ProjectInfo.class);
            digester.addBeanPropertySetter(projectInfoXpath + "/name");
            digester.addBeanPropertySetter(projectInfoXpath + "/reportDate");
            digester.addBeanPropertySetter(projectInfoXpath + "/credits");

            final String depXpath = "analysis/dependencies/dependency";
            digester.addObjectCreate(depXpath, Dependency.class);
            digester.addBeanPropertySetter(depXpath + "/fileName");
            digester.addBeanPropertySetter(depXpath + "/filePath");
            digester.addBeanPropertySetter(depXpath + "/md5");
            digester.addBeanPropertySetter(depXpath + "/sha1");
            digester.addBeanPropertySetter(depXpath + "/sha256");
            digester.addBeanPropertySetter(depXpath + "/description");
            digester.addBeanPropertySetter(depXpath + "/license");

            final String projRefsPath = "analysis/dependencies/dependency/projectReferences";
            digester.addObjectCreate(projRefsPath, ArrayList.class);
            digester.addCallMethod(projRefsPath + "/projectReference", "add", 1);
            digester.addCallParam(projRefsPath + "/projectReference", 0);

            final String vulnXpath = "analysis/dependencies/dependency/vulnerabilities/vulnerability";
            digester.addFactoryCreate(vulnXpath, new VulnerabilityCreationFactory());
            digester.addBeanPropertySetter(vulnXpath + "/name");
            digester.addBeanPropertySetter(vulnXpath + "/description");
            digester.addBeanPropertySetter(vulnXpath + "/severity");

            final String cwesXpath = "analysis/dependencies/dependency/vulnerabilities/vulnerability/cwes";
            digester.addObjectCreate(cwesXpath, ArrayList.class);
            digester.addCallMethod(cwesXpath + "/cwe", "add", 1);
            digester.addCallParam(cwesXpath + "/cwe", 0);

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

            final String refXpath = "analysis/dependencies/dependency/vulnerabilities/vulnerability/references/reference";
            digester.addObjectCreate(refXpath, Reference.class);
            digester.addBeanPropertySetter(refXpath + "/source");
            digester.addBeanPropertySetter(refXpath + "/url");
            digester.addBeanPropertySetter(refXpath + "/name");

            digester.addSetNext(scanInfoXpath, "setScanInfo");
            digester.addSetNext(projectInfoXpath, "setProjectInfo");
            digester.addSetNext(cvssV2Xpath, "setCvssV2");
            digester.addSetNext(cvssV3Xpath, "setCvssV3");
            digester.addSetNext(refXpath, "addReference");
            digester.addSetNext(vulnXpath, "addVulnerability");
            digester.addSetNext(cwesXpath, "setCwes");
            digester.addSetNext(depXpath, "addDependency");
            digester.addSetNext(projRefsPath, "setProjectReferences");

            final Analysis analysis = digester.parse(file);
            if (analysis == null) {
                throw new SAXException("Input stream is not a Dependency-Check report file.");
            }

            if (analysis.getScanInfo() == null || analysis.getScanInfo().getEngineVersion() == null
                    || Version.parseVersion(analysis.getScanInfo().getEngineVersion()).compareTo(MIN_VERSION) < 0) {
                throw new ReportParserException("Unsupported Dependency-Check schema version detected");
            }
            findings = convert(analysis);
        } catch (IOException | SAXException | ParserConfigurationException e) {
            throw new InvocationTargetException(e);
        }
        return findings;
    }

    /**
     * Converts the dependency-check structure to findings.
     *
     * @param collection the internal maven module
     * @return a List of Finding objects
     */
    private static List<Finding> convert(final Analysis collection) {
        List<Finding> findings = new ArrayList<>();
        for (Dependency dependency : collection.getDependencies()) {
            for (Vulnerability vulnerability : dependency.getVulnerabilities()) {
                final Finding finding = new Finding(dependency, vulnerability);
                findings.add(finding);
            }
        }
        return findings;
    }

}
