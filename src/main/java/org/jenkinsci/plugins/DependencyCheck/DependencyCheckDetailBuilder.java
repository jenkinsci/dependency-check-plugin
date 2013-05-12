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
 * A detail builder for DependencyCheck annotations capable of showing details of linked annotations.
 *
 * @author Steve Springett
 */
public class DependencyCheckDetailBuilder extends DetailFactory {

    @Override
    protected TabDetail createTabDetail(final AbstractBuild<?, ?> owner, final Collection<FileAnnotation> annotations,
                                        final String url, final String defaultEncoding) {
        return new DependencyCheckTabDetail(owner, this, annotations, url, defaultEncoding);
    }
}