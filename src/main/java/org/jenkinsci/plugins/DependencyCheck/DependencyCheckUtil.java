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

import edu.umd.cs.findbugs.annotations.Nullable;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.DependencyCheck.tools.DependencyCheckInstallation;
import org.jenkinsci.plugins.DependencyCheck.tools.DependencyCheckInstallation.DescriptorImpl;

/*package */ final class DependencyCheckUtil {

    private DependencyCheckUtil() {
    }

    /**
     * Gets the DependencyCheck to invoke, or null to invoke the default one.
     *
     * @param name
     *            the name of DependencyCheck installation
     * @return a DependencyCheck installation for the given name if exists, {@code null}
     *         otherwise.
     */
    @Nullable
    public static DependencyCheckInstallation getDependencyCheck(@Nullable String name) {
        if (name != null) {
            for (DependencyCheckInstallation installation : getInstallations()) {
                if (name.equals(installation.getName()))
                    return installation;
            }
        }
        return null;
    }

    public static DependencyCheckInstallation[] getInstallations() {
        DescriptorImpl descriptor = Jenkins.get().getDescriptorByType(DependencyCheckInstallation.DescriptorImpl.class);
        if (descriptor == null) {
            throw new IllegalStateException("Impossible retrieve DependencyCheckInstallation descriptor");
        }
        return descriptor.getInstallations();
    }
}
