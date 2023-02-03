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
package org.jenkinsci.plugins.DependencyCheck.charts;

import java.util.LinkedHashMap;
import java.util.Map;

import org.jenkinsci.plugins.DependencyCheck.ResultAction;
import org.jenkinsci.plugins.DependencyCheck.model.SeverityDistribution;

import edu.hm.hafner.echarts.SeriesBuilder;

public class SeverityDistributionSeriesBuilder extends SeriesBuilder<ResultAction> {

    @Override
    protected Map<String, Integer> computeSeries(ResultAction currentAction) {
        Map<String, Integer> series = new LinkedHashMap<>();

        SeverityDistribution severityDistribution = currentAction.getSeverityDistribution();
        if (severityDistribution == null) {
            severityDistribution = new SeverityDistribution(currentAction.getOwner().getNumber());
        }
        series.put(SeverityThrendChart.CRITICAL.getLineSeriesName(), severityDistribution.getCritical());
        series.put(SeverityThrendChart.HIGH.getLineSeriesName(), severityDistribution.getHigh());
        series.put(SeverityThrendChart.MEDIUM.getLineSeriesName(), severityDistribution.getMedium());
        series.put(SeverityThrendChart.LOW.getLineSeriesName(), severityDistribution.getLow());
        series.put(SeverityThrendChart.UNASSIGNED.getLineSeriesName(), severityDistribution.getUnassigned());
        return series;
    }

}
