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

import java.io.Serial;
import java.io.Serializable;

/**
 * Ported from the Dependency-Track Jenkins plugin.
 *
 * @author Steve Springett (steve.springett@owasp.org)
 * @since 5.0.0
 */
public class Thresholds implements Serializable {
    public static class TotalFindings implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        public Integer unstableCritical;
        public Integer unstableHigh;
        public Integer unstableMedium;
        public Integer unstableLow;
        public Integer failedCritical;
        public Integer failedHigh;
        public Integer failedMedium;
        public Integer failedLow;
        public boolean limitToAnalysisExploitable;
    }

    public static class NewFindings implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        public Integer unstableCritical;
        public Integer unstableHigh;
        public Integer unstableMedium;
        public Integer unstableLow;
        public Integer failedCritical;
        public Integer failedHigh;
        public Integer failedMedium;
        public Integer failedLow;
        public boolean limitToAnalysisExploitable;
    }

    @Serial
    private static final long serialVersionUID = -6489027153777053306L;

    public final TotalFindings totalFindings = new TotalFindings();
    public final NewFindings newFindings = new NewFindings();

}