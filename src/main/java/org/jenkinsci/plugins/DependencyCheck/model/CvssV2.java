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
import java.util.Objects;

/**
 * Java Bean class for CVSSv2 identified by DependencyCheck.
 *
 * @author Steve Springett (steve.springett@owasp.org)
 * @since 5.0.0
 */
public class CvssV2 implements Serializable {

    private static final long serialVersionUID = -3093529837834374013L;

    private String score;
    private String accessVector;
    private String accessComplexity;
    private String authenticationr;
    private String confidentialImpact;
    private String integrityImpact;
    private String availabilityImpact;
    private String severity;

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getAccessVector() {
        return accessVector;
    }

    public void setAccessVector(String accessVector) {
        this.accessVector = accessVector;
    }

    public String getAccessComplexity() {
        return accessComplexity;
    }

    public void setAccessComplexity(String accessComplexity) {
        this.accessComplexity = accessComplexity;
    }

    public String getAuthenticationr() {
        return authenticationr;
    }

    public void setAuthenticationr(String authenticationr) {
        this.authenticationr = authenticationr;
    }

    public String getConfidentialImpact() {
        return confidentialImpact;
    }

    public void setConfidentialImpact(String confidentialImpact) {
        this.confidentialImpact = confidentialImpact;
    }

    public String getIntegrityImpact() {
        return integrityImpact;
    }

    public void setIntegrityImpact(String integrityImpact) {
        this.integrityImpact = integrityImpact;
    }

    public String getAvailabilityImpact() {
        return availabilityImpact;
    }

    public void setAvailabilityImpact(String availabilityImpact) {
        this.availabilityImpact = availabilityImpact;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CvssV2 cvssV2 = (CvssV2) o;
        return Objects.equals(score, cvssV2.score) && Objects.equals(accessVector, cvssV2.accessVector) && Objects.equals(accessComplexity, cvssV2.accessComplexity) && Objects.equals(authenticationr, cvssV2.authenticationr) && Objects.equals(confidentialImpact, cvssV2.confidentialImpact) && Objects.equals(integrityImpact, cvssV2.integrityImpact) && Objects.equals(availabilityImpact, cvssV2.availabilityImpact) && Objects.equals(severity, cvssV2.severity);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(score, accessVector, accessComplexity, authenticationr, confidentialImpact, integrityImpact, availabilityImpact, severity);
    }
}
