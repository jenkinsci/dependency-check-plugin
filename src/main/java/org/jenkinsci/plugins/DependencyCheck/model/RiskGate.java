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

import hudson.model.Result;
import java.io.Serializable;
import java.util.List;

/**
 * Ported from the Dependency-Track Jenkins plugin
 */
public class RiskGate implements Serializable {

    private static final long serialVersionUID = 171256230735670985L;

    private Thresholds thresholds;

    public RiskGate(Thresholds thresholds) {
        this.thresholds = thresholds;
    }

    /**
     * Evaluates if the current results meet or exceed the defined threshold.
     * @param previousDistribution
     * @param previousFindings
     * @param currentDistribution
     * @param currentFindings
     * @return a Result
     */
    public Result evaluate(final SeverityDistribution previousDistribution,
                           final List<Finding> previousFindings,
                           final SeverityDistribution currentDistribution,
                           final List<Finding> currentFindings) {

        Result result = Result.SUCCESS;
        if (currentDistribution != null) {
            if ((thresholds.totalFindings.failedCritical != null && currentDistribution.getCritical() > 0 && currentDistribution.getCritical() >= thresholds.totalFindings.failedCritical)
                    || (thresholds.totalFindings.failedHigh != null && currentDistribution.getHigh() > 0 && currentDistribution.getHigh() >= thresholds.totalFindings.failedHigh)
                    || (thresholds.totalFindings.failedMedium != null && currentDistribution.getMedium() > 0 && currentDistribution.getMedium() >= thresholds.totalFindings.failedMedium)
                    || (thresholds.totalFindings.failedLow != null && currentDistribution.getLow() > 0 && currentDistribution.getLow() >= thresholds.totalFindings.failedLow)) {

                return Result.FAILURE;
            }
            if ((thresholds.totalFindings.unstableCritical != null && currentDistribution.getCritical() > 0 && currentDistribution.getCritical() >= thresholds.totalFindings.unstableCritical)
                    || (thresholds.totalFindings.unstableHigh != null && currentDistribution.getHigh() > 0 && currentDistribution.getHigh() >= thresholds.totalFindings.unstableHigh)
                    || (thresholds.totalFindings.unstableMedium != null && currentDistribution.getMedium() > 0 && currentDistribution.getMedium() >= thresholds.totalFindings.unstableMedium)
                    || (thresholds.totalFindings.unstableLow != null && currentDistribution.getLow() > 0 && currentDistribution.getLow() >= thresholds.totalFindings.unstableLow)) {

                result = Result.UNSTABLE;
            }
        }

        if (currentDistribution != null && previousDistribution != null) {
            if ((thresholds.newFindings.failedCritical != null && currentDistribution.getCritical() > 0 && currentDistribution.getCritical() >= previousDistribution.getCritical() + thresholds.newFindings.failedCritical)
                    || (thresholds.newFindings.failedHigh != null && currentDistribution.getHigh() > 0 && currentDistribution.getHigh() >= previousDistribution.getHigh() + thresholds.newFindings.failedHigh)
                    || (thresholds.newFindings.failedMedium != null && currentDistribution.getMedium() > 0 && currentDistribution.getMedium() >= previousDistribution.getMedium() + thresholds.newFindings.failedMedium)
                    || (thresholds.newFindings.failedLow != null && currentDistribution.getLow() > 0 && currentDistribution.getLow() >= previousDistribution.getLow() + thresholds.newFindings.failedLow)) {

                return Result.FAILURE;
            }
            if ((thresholds.newFindings.unstableCritical != null && currentDistribution.getCritical() > 0 && currentDistribution.getCritical() >= previousDistribution.getCritical() + thresholds.newFindings.unstableCritical)
                    || (thresholds.newFindings.unstableHigh != null && currentDistribution.getHigh() > 0 && currentDistribution.getHigh() >= previousDistribution.getHigh() + thresholds.newFindings.unstableHigh)
                    || (thresholds.newFindings.unstableMedium != null && currentDistribution.getMedium() > 0 && currentDistribution.getMedium() >= previousDistribution.getMedium() + thresholds.newFindings.unstableMedium)
                    || (thresholds.newFindings.unstableLow != null && currentDistribution.getLow() > 0 && currentDistribution.getLow() >= previousDistribution.getLow() + thresholds.newFindings.unstableLow)) {

                result = Result.UNSTABLE;
            }
        }

        return result;
    }
}
