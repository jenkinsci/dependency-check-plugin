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

import hudson.plugins.analysis.util.model.AbstractAnnotation;
import hudson.plugins.analysis.util.model.Priority;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.DependencyCheck.Messages;
import org.owasp.dependencycheck.dependency.Vulnerability;

/**
 * A serializable Java Bean class representing a warning.
 * <p>
 * Note: this class has a natural ordering that is inconsistent with equals.
 * </p>
 *
 * @author Steve Springett (steve.springett@owasp.org)
 */
public class Warning extends AbstractAnnotation {

    private static final long serialVersionUID = -6132623961334474815L;
    public static final String ORIGIN = "dependency-check";

    private Vulnerability vulnerability;

    /**
     * Creates a new instance of <code>Warning</code>.
     *
     * @param priority      the priority
     * @param vulnerability the vulnerability to make the annotation from
     */
    public Warning(final Priority priority, final Vulnerability vulnerability) {
        super(priority, vulnerability.getDescription(), 0, 0, vulnerability.getCwe(), vulnerability.getName());
        setOrigin(ORIGIN);

        this.vulnerability = vulnerability;
    }

    public Vulnerability getVulnerability() {
        return vulnerability;
    }

    /**
     * Analysis-core only supports Priority, not Severity. So we use Priority internally
     * and map that to Severity. This method takes the priority that was assigned to the
     * warning, and returns the localized string representation of the corresponding severity
     *
     * @return a String representation of the warnings severity
     */
    public String getSeverity() {
        if (getPriority() == Priority.HIGH) {
            return Messages.Severity_High();
        } else if (getPriority() == Priority.LOW) {
            return Messages.Severity_Low();
        } else {
            return Messages.Severity_Medium();
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getToolTip() {
        return StringUtils.EMPTY;
    }

    /**
     * The tooltip.
     */
    @SuppressWarnings("PMD")
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("SS")
    private final String tooltip = StringUtils.EMPTY; // backward compatibility
}
