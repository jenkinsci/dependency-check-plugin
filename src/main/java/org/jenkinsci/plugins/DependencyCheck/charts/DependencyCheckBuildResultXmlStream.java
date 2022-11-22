package org.jenkinsci.plugins.DependencyCheck.charts;

import java.util.Collections;

import org.jenkinsci.plugins.DependencyCheck.model.SeverityDistribution;

import io.jenkins.plugins.util.AbstractXmlStream;

public class DependencyCheckBuildResultXmlStream extends AbstractXmlStream<DependencyCheckBuildResult> {

    public DependencyCheckBuildResultXmlStream() {
        super(DependencyCheckBuildResult.class);
    }

    @Override
    protected DependencyCheckBuildResult createDefaultValue() {
        return new DependencyCheckBuildResult(Collections.emptyList(), new SeverityDistribution(0));
    }

}
