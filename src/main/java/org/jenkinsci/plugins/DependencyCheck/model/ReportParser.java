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

import org.apache.commons.digester.Digester;
import org.owasp.dependencycheck.dependency.Dependency;
import org.owasp.dependencycheck.dependency.Reference;
import org.owasp.dependencycheck.dependency.Vulnerability;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

/**
 * A parser for DependencyCheck XML files.
 *
 * @author Steve Springett (steve.springett@owasp.org)
 */
public class ReportParser {

    public Analysis parse(final InputStream file) throws InvocationTargetException {
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
            digester.addBeanPropertySetter(depXpath + "/md5", "md5sum");
            digester.addBeanPropertySetter(depXpath + "/sha1", "sha1sum");
            digester.addBeanPropertySetter(depXpath + "/description");
            digester.addBeanPropertySetter(depXpath + "/license");

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
            digester.addSetNext(vulnXpath, "addVulnerability");
            digester.addSetNext(depXpath, "addDependency");

            final Analysis analysis = (Analysis) digester.parse(file);
            if (analysis == null) {
                throw new SAXException("Input stream is not a Dependency-Check report file.");
            }
            return analysis;
        } catch (IOException | SAXException e) {
            throw new InvocationTargetException(e);
        }
    }
}
