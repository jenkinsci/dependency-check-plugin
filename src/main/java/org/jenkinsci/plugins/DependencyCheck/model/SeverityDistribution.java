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

import java.io.Serializable;

/**
 * Ported from the Dependency-Track Jenkins plugin.
 *
 * @author Steve Springett (steve.springett@owasp.org)
 * @since 5.0.0
 */
public class SeverityDistribution implements Serializable {

    private static final long serialVersionUID = -8061827374550831502L;

    private int critical;
    private int high;
    private int medium;
    private int low;
    private int info;
    private int unassigned;

    public int getCritical() {
        return critical;
    }

    public int getHigh() {
        return high;
    }

    public int getMedium() {
        return medium;
    }

    public int getLow() {
        return low;
    }

    public int getInfo() {
        return info;
    }

    public int getUnassigned() {
        return unassigned;
    }

    public void add(Severity severity) {
        if (Severity.CRITICAL == severity) {
            critical++;
        } else if (Severity.HIGH == severity) {
            high++;
        } else if (Severity.MEDIUM == severity) {
            medium++;
        } else if (Severity.LOW == severity) {
            low++;
        } else if (Severity.INFO == severity) {
            info++;
        } else if (Severity.UNASSIGNED == severity) {
            unassigned++;
        }
    }
}