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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Severity levels for dependency vulnerabilities.
 * Provides normalized parsing and utility comparison methods.
 *
 * @author Steve Springett
 * @since 5.0.0
 */
public enum Severity {

    CRITICAL(5),
    HIGH(4),
    MEDIUM(3),
    LOW(2),
    INFO(1),
    UNASSIGNED(0);

    private static final Map<String, Severity> LOOKUP;

    static {
        Map<String, Severity> map = new HashMap<>();
        for (Severity s : values()) {
            map.put(s.name(), s);
        }
        // synonyms
        map.put("MODERATE", MEDIUM);
        map.put("UNKNOWN", UNASSIGNED);
        map.put("INFORMATIONAL", INFO);
        LOOKUP = Collections.unmodifiableMap(map);
    }

    private final int rank;

    Severity(int rank) {
        this.rank = rank;
    }

    /**
     * Returns the numeric rank of this severity (higher means more critical).
     *
     * @return rank value
     */
    public int getRank() {
        return rank;
    }

    /**
     * Checks if this severity is greater than or equal to the given severity.
     *
     * @param other the severity to compare against
     * @return true if this severity >= other
     */
    public boolean isAtLeast(Severity other) {
        return this.rank >= other.rank;
    }

    /**
     * Normalizes a string to a {@link Severity}. Case-insensitive, handles common synonyms.
     *
     * @param severity level as string
     * @return matched Severity, or UNASSIGNED if none found
     */
    @JsonCreator
    public static Severity fromString(String severity) {
        if (severity == null || severity.isBlank()) {
            return UNASSIGNED;
        }
        Severity s = LOOKUP.get(severity.trim().toUpperCase());
        return s != null ? s : UNASSIGNED;
    }

    /**
     * Returns the string representation for serialization.
     *
     * @return name of the severity
     */
    @JsonValue
    @Override
    public String toString() {
        return name();
    }

    /**
     * @deprecated use {@link #fromString(String)} instead
     */
    @Deprecated
    public static Severity normalize(String severity) {
        return fromString(severity);
    }
}
