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
package org.jenkinsci.plugins.DependencyCheck.parser;

import hudson.plugins.analysis.core.AbstractAnnotationParser;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;
import org.apache.commons.digester.Digester;
import org.apache.commons.lang.StringUtils;
import org.owasp.dependencycheck.dependency.Dependency;
import org.owasp.dependencycheck.dependency.Reference;
import org.owasp.dependencycheck.dependency.Vulnerability;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * A parser for DependencyCheck XML files.
 *
 * @author Steve Springett (steve.springett@owasp.org)
 */
public class ReportParser extends AbstractAnnotationParser {

    private static final long serialVersionUID = -1906443657161473919L;

    /**
     * Creates a new instance of {@link ReportParser}.
     */
    public ReportParser() {
        super(StringUtils.EMPTY);
    }

    /**
     * Creates a new instance of {@link ReportParser}.
     *
     * @param defaultEncoding the default encoding to be used when reading and parsing files
     */
    public ReportParser(final String defaultEncoding) {
        super(defaultEncoding);
    }

    @Override
    public Collection<FileAnnotation> parse(final InputStream file, final String moduleName) throws InvocationTargetException {
        try {
            // Parse dependency-check-report.xml files compatible with DependencyCheck.xsd v.1.1
            final Digester digester = new Digester();
            digester.setValidating(false);
            digester.setClassLoader(ReportParser.class.getClassLoader());

            digester.addObjectCreate("analysis", Analysis.class);

            final String depXpath = "analysis/dependencies/dependency";
            digester.addObjectCreate(depXpath, Dependency.class);
            digester.addBeanPropertySetter(depXpath + "/fileName");
            digester.addBeanPropertySetter(depXpath + "/filePath");
            digester.addBeanPropertySetter(depXpath + "/md5", "md5sum");
            digester.addBeanPropertySetter(depXpath + "/sha1", "sha1sum");
            digester.addBeanPropertySetter(depXpath + "/description");
            digester.addBeanPropertySetter(depXpath + "/license");

            final String identXpath = "analysis/dependencies/dependency/identifiers/identifier";
            digester.addObjectCreate(identXpath, org.owasp.dependencycheck.dependency.Identifier.class);
            digester.addBeanPropertySetter(identXpath + "/name", "value");
            digester.addBeanPropertySetter(identXpath + "/url");

            final String vulnXpath = "analysis/dependencies/dependency/vulnerabilities/vulnerability";
            digester.addFactoryCreate(vulnXpath, new VulnerabilityCreationFactory());
            digester.addBeanPropertySetter(vulnXpath + "/name");
            digester.addBeanPropertySetter(vulnXpath + "/cvssScore");
            digester.addBeanPropertySetter(vulnXpath + "/cvssAccessVector");
            digester.addBeanPropertySetter(vulnXpath + "/cvssAccessComplexity");
            digester.addBeanPropertySetter(vulnXpath + "/cvssConfidentialImpact", "cvssConfidentialityImpact");
            digester.addBeanPropertySetter(vulnXpath + "/cvssIntegrityImpact");
            digester.addBeanPropertySetter(vulnXpath + "/cvssAvailabilityImpact");
            digester.addBeanPropertySetter(vulnXpath + "/cwe");
            digester.addBeanPropertySetter(vulnXpath + "/description");

            final String refXpath = "analysis/dependencies/dependency/vulnerabilities/vulnerability/references/reference";
            digester.addObjectCreate(refXpath, Reference.class);
            digester.addBeanPropertySetter(refXpath + "/source");
            digester.addBeanPropertySetter(refXpath + "/url");
            digester.addBeanPropertySetter(refXpath + "/name");

            final String suppressedVulnXpath = "analysis/dependencies/dependency/vulnerabilities/suppressedVulnerability";
            digester.addObjectCreate(suppressedVulnXpath, Vulnerability.class);
            digester.addBeanPropertySetter(suppressedVulnXpath + "/name");
            digester.addBeanPropertySetter(suppressedVulnXpath + "/cvssScore");
            digester.addBeanPropertySetter(suppressedVulnXpath + "/cvssAccessVector");
            digester.addBeanPropertySetter(suppressedVulnXpath + "/cvssAccessComplexity");
            digester.addBeanPropertySetter(suppressedVulnXpath + "/cvssConfidentialImpact", "cvssConfidentialityImpact");
            digester.addBeanPropertySetter(suppressedVulnXpath + "/cvssIntegrityImpact");
            digester.addBeanPropertySetter(suppressedVulnXpath + "/cvssAvailabilityImpact");
            digester.addBeanPropertySetter(suppressedVulnXpath + "/cwe");
            digester.addBeanPropertySetter(suppressedVulnXpath + "/description");

            final String suppressedRefXpath = "analysis/dependencies/dependency/vulnerabilities/suppressedVulnerability/references/reference";
            digester.addObjectCreate(suppressedRefXpath, Reference.class);
            digester.addBeanPropertySetter(suppressedRefXpath + "/source");
            digester.addBeanPropertySetter(suppressedRefXpath + "/url");
            digester.addBeanPropertySetter(suppressedRefXpath + "/name");

            digester.addSetNext(suppressedRefXpath, "addReference");
            digester.addSetNext(suppressedVulnXpath, "addSuppressedVulnerability");
            digester.addSetNext(refXpath, "addReference");
            digester.addSetNext(identXpath, "addIdentifier");
            digester.addSetNext(vulnXpath, "addVulnerability");
            digester.addSetNext(depXpath, "addDependency");

            final Analysis module = (Analysis) digester.parse(file);
            if (module == null) {
                throw new SAXException("Input stream is not a Dependency-Check report file.");
            }

            return convert(module, moduleName);

        } catch (IOException exception) {
            throw new InvocationTargetException(exception);
        } catch (SAXException exception) {
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
    private Collection<FileAnnotation> convert(final Analysis collection, final String moduleName) {
        final ArrayList<FileAnnotation> annotations = new ArrayList<FileAnnotation>();

        for (Dependency dependency : collection.getDependencies()) {
            for (Vulnerability vulnerability : dependency.getVulnerabilities()) {
                // Analysis-core uses priority to rank vulnerabilities. Priority in the
                // context of DependencyCheck, doesn't make sense. DependencyCheck uses
                // severity, so let's use the Priority object and set the priority to
                // the value of severity.
                Priority priority;

                if (vulnerability.getCvssScore() >= 7.0) {
                    priority = Priority.HIGH;
                } else if (vulnerability.getCvssScore() < 4.0) {
                    priority = Priority.LOW;
                } else {
                    priority = Priority.NORMAL;
                }

                final Warning warning = new Warning(priority, vulnerability);
                warning.setModuleName(moduleName);
                warning.setFileName(dependency.getFilePath());
                warning.setContextHashCode(createContextHashCode(
                        dependency.getFilePath() +
                                StringUtils.trimToEmpty(dependency.getMd5sum()) +
                                StringUtils.trimToEmpty(dependency.getSha1sum()),
                        0,
                        vulnerability.getName())
                );
                annotations.add(warning);
            }
        }
        return annotations;
    }

}
