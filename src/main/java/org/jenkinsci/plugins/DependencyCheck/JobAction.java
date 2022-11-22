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
package org.jenkinsci.plugins.DependencyCheck;

import java.util.stream.Stream;

import org.jenkinsci.plugins.DependencyCheck.charts.SeverityDistributionSeriesBuilder;
import org.jenkinsci.plugins.DependencyCheck.charts.SeverityThrendChart;

import edu.hm.hafner.echarts.ChartModelConfiguration;
import edu.hm.hafner.echarts.ChartModelConfiguration.AxisType;
import edu.hm.hafner.echarts.LineSeries;
import edu.hm.hafner.echarts.LineSeries.FilledMode;
import edu.hm.hafner.echarts.LineSeries.StackedMode;
import edu.hm.hafner.echarts.LinesChartModel;
import edu.hm.hafner.echarts.LinesDataSet;
import hudson.model.Job;
import io.jenkins.plugins.echarts.AsyncTrendJobAction;

/**
 * Ported from the Dependency-Track Jenkins plugin.
 *
 * @author Steve Springett (steve.springett@owasp.org)
 * @since 5.0.0
 */
public class JobAction extends AsyncTrendJobAction<ResultAction> {

    public JobAction(final Job<?, ?> project) {
        super(project, ResultAction.class);
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return "dependency-check-findings";
    }

    @Override
    protected LinesChartModel createChartModel() {
        SeverityDistributionSeriesBuilder builder = new SeverityDistributionSeriesBuilder();
        LinesDataSet lineModel = builder.createDataSet(new ChartModelConfiguration(AxisType.BUILD), createBuildHistory());
        LinesChartModel chart = new LinesChartModel(lineModel);
        Stream.of(SeverityThrendChart.values()).forEach(severity -> {
            LineSeries lineSeries = new LineSeries(severity.getLineSeriesName(), severity.getColor(), StackedMode.SEPARATE_LINES, FilledMode.LINES);
            lineSeries.addAll(lineModel.getSeries(severity.getLineSeriesName()));
            chart.addSeries(lineSeries);
        });
        chart.setDomainAxisItemName("vulnerabilities");
        return chart;
    }
}
