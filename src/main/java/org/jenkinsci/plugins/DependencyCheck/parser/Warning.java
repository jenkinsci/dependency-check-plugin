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

import hudson.plugins.analysis.util.model.AbstractAnnotation;
import hudson.plugins.analysis.util.model.Priority;
import org.apache.commons.lang.StringUtils;

/**
 * A serializable Java Bean class representing a warning.
 * <p>
 * Note: this class has a natural ordering that is inconsistent with equals.
 * </p>
 *
 * @author Steve Springett
 */
public class Warning extends AbstractAnnotation
{

    private static final long serialVersionUID = -6132623961334474815L;
    public static final String ORIGIN = "dependency-check";


    /**
     * Creates a new instance of <code>Warning</code>.
     *
     * @param priority the priority
     * @param message  the message of the warning
     * @param category the warning category
     * @param type     the identifier of the warning type
     */
    public Warning(final Priority priority, final String message, final String category, final String type)
    {
        super(priority, message, 0, 0, category, type);
        setOrigin(ORIGIN);
    }

    /**
     * {@inheritDoc}
     */
    public String getToolTip()
    {
        return "Warning tooltip goes here";
        //return PmdMessages.getInstance().getMessage(getCategory(), getType());
    }

    /**
     * The tooltip.
     */
    @SuppressWarnings("PMD")
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("SS")
    private final String tooltip = StringUtils.EMPTY; // backward compatibility
}