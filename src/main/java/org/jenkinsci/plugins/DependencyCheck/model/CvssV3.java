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
 * Java Bean class for CVSSv3 identified by DependencyCheck.
 *
 * @author Steve Springett (steve.springett@owasp.org)
 * @since 5.0.0
 */
public class CvssV3 implements Serializable {

    @Serial
    private static final long serialVersionUID = 9178656430054916373L;

    private String baseScore;
    private String attackVector;
    private String attackComplexity;
    private String privilegesRequired;
    private String userInteraction;
    private String scope;
    private String confidentialityImpact;
    private String integrityImpact;
    private String availabilityImpact;
    private String baseSeverity;

    public String getBaseScore() {
        return baseScore;
    }

    public void setBaseScore(String baseScore) {
        this.baseScore = baseScore;
    }

    public String getAttackVector() {
        return attackVector;
    }

    public void setAttackVector(String attackVector) {
        this.attackVector = attackVector;
    }

    public String getAttackComplexity() {
        return attackComplexity;
    }

    public void setAttackComplexity(String attackComplexity) {
        this.attackComplexity = attackComplexity;
    }

    public String getPrivilegesRequired() {
        return privilegesRequired;
    }

    public void setPrivilegesRequired(String privilegesRequired) {
        this.privilegesRequired = privilegesRequired;
    }

    public String getUserInteraction() {
        return userInteraction;
    }

    public void setUserInteraction(String userInteraction) {
        this.userInteraction = userInteraction;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getConfidentialityImpact() {
        return confidentialityImpact;
    }

    public void setConfidentialityImpact(String confidentialityImpact) {
        this.confidentialityImpact = confidentialityImpact;
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

    public String getBaseSeverity() {
        return baseSeverity;
    }

    public void setBaseSeverity(String baseSeverity) {
        this.baseSeverity = baseSeverity;
    }
}
