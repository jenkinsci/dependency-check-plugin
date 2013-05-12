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
import hudson.plugins.analysis.util.model.AnnotationContainer;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.views.DetailFactory;
import hudson.plugins.analysis.views.TabDetail;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;

/**
 * A detail builder for DependencyCheck annotations capable of showing details of linked annotations.
 *
 * @author Steve Springett, based on DryDetailBuilder by Ulli Hafner
 */
public class DependencyCheckDetailBuilder extends DetailFactory
{
    /*
    @Override
    public Object createDetails(final String link, final AbstractBuild<?, ?> owner,
                                final AnnotationContainer container, final String defaultEncoding, final String displayName)
    {
        //todo: do I need this block?
        if (link.startsWith("link."))
        {
            String suffix = StringUtils.substringAfter(link, "link.");
            String[] fromToStrings = StringUtils.split(suffix, ".");
            if (fromToStrings.length == 2)
            {
                return createDrySourceDetail(owner, container, defaultEncoding, fromToStrings[0], fromToStrings[1]);
            }
            return null;
        }
        return super.createDetails(link, owner, container, defaultEncoding, displayName);
    }
/*
    private Object createDrySourceDetail(final AbstractBuild<?, ?> owner,
                                         final AnnotationContainer container, final String defaultEncoding,
                                         final String fromString, final String toString)
    {
        long from = Long.parseLong(fromString);
        long to = Long.parseLong(toString);
        //todo: do I need this method?
        FileAnnotation fromAnnotation = container.getAnnotation(from);
        //if (fromAnnotation instanceof DuplicateCode) {
        //    return new SourceDetail(owner, ((DuplicateCode)fromAnnotation).getLink(to), defaultEncoding);
        //}
        return null;
    }
    */

    @Override
    protected TabDetail createTabDetail(final AbstractBuild<?, ?> owner, final Collection<FileAnnotation> annotations,
                                        final String url, final String defaultEncoding)
    {
        return new DependencyCheckTabDetail(owner, this, annotations, url, defaultEncoding);
    }
}