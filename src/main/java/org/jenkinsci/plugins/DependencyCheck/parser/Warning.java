/*
 * This file is part of Dependency-Check Jenkins plugin.
 *
 * Dependency-Check is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * Dependency-Check is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Dependency-Check. If not, see http://www.gnu.org/licenses/.
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
        if (getPriority() == Priority.HIGH)
            return Messages.Severity_High();
        else if (getPriority() == Priority.LOW)
            return Messages.Severity_Low();
        else
            return Messages.Severity_Medium();
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