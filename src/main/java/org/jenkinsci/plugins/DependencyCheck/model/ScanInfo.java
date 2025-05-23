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
 * Java Bean class for ScanInfo identified by DependencyCheck.
 *
 * @author Steve Springett (steve.springett@owasp.org)
 * @since 5.0.0
 */
public class ScanInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = -8845107789941894310L;

    private String engineVersion;

    public String getEngineVersion() {
        return engineVersion;
    }

    public void setEngineVersion(String engineVersion) {
        this.engineVersion = engineVersion;
    }
}
