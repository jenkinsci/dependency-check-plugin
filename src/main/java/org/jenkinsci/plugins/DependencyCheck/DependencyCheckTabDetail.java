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
package org.jenkinsci.plugins.DependencyCheck;

import hudson.model.AbstractBuild;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.views.DetailFactory;
import hudson.plugins.analysis.views.TabDetail;

import java.util.Collection;

/**
 * Detail view for the DependencyCheck plug-in: uses different table visualization.
 *
 * @author Steve Springett (steve.springett@owasp.org), based on DryTabDetail by Ulli Hafner
 */
public class DependencyCheckTabDetail extends TabDetail {

    private static final long serialVersionUID = -210918729676460128L;

    /**
     * Creates a new instance of {@link TabDetail}.
     *
     * @param owner           current build as owner of this action.
     * @param detailFactory   the detail factory to use
     * @param annotations     the module to show the details for
     * @param url             URL to render the content of this tab
     * @param defaultEncoding the default encoding to be used when reading and parsing files
     */
    public DependencyCheckTabDetail(final AbstractBuild<?, ?> owner, final DetailFactory detailFactory, final Collection<FileAnnotation> annotations, final String url, final String defaultEncoding) {
        super(owner, detailFactory, annotations, url, defaultEncoding);
    }

    @Override
    public String getWarnings() {
        return "warnings.jelly";
    }

    @Override
    public String getDetails() {
        return "details.jelly";
    }

    @Override
    public String getFixed() {
        return "fixed.jelly";
    }

}