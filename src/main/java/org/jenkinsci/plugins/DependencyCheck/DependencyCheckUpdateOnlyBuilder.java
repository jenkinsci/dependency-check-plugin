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

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.io.Serializable;

/**
 * The DependencyCheck builder class provides the ability to invoke a DependencyCheck build as
 * a Jenkins build step. This class takes the configuration from the UI, creates options from
 * them and passes them to the DependencyCheckExecutor for the actual execution of the
 * DependencyCheck Engine and ReportGenerator.
 *
 * @author Steve Springett (steve.springett@owasp.org)
 */
@SuppressWarnings("unused")
public class DependencyCheckUpdateOnlyBuilder extends AbstractDependencyCheckBuilder implements Serializable {

    private static final long serialVersionUID = -1028800761683685381L;

    private final String datadir;
    private final boolean isVerboseLoggingEnabled;


    @DataBoundConstructor // Fields in config.jelly must match the parameter names
    public DependencyCheckUpdateOnlyBuilder(String datadir, Boolean isVerboseLoggingEnabled, Boolean includeHtmlReports) {
        this.datadir = datadir;
        this.isVerboseLoggingEnabled = (isVerboseLoggingEnabled != null) && isVerboseLoggingEnabled;
    }

    /**
     * Retrieves the data directory that DependencyCheck will use. This is a per-build config item.
     * This method must match the value in <tt>config.jelly</tt>.
     */
    public String getDatadir() {
        return datadir;
    }

    /**
     * Retrieves whether verbose logging is enabled or not. This is a per-build config item.
     * This method must match the value in <tt>config.jelly</tt>.
     */
    public boolean isVerboseLoggingEnabled() {
        return isVerboseLoggingEnabled;
    }

    /**
     * This method is called whenever the DependencyCheck build step is executed.
     *
     * @param build    A Build object
     * @param launcher A Launcher object
     * @param listener A BuildListener object
     * @return A true or false value indicating if the build was successful or if it failed
     */
    @Override
    public boolean perform(final AbstractBuild build, final Launcher launcher, final BuildListener listener)
            throws InterruptedException, IOException {

        final Options options = generateOptions(build, listener);
        setOptions(options);
        return super.perform(build, launcher, listener);
    }

    /**
     * Generate Options from build configuration preferences that will be passed to
     * the build step in DependencyCheck
     * @param build an AbstractBuild object
     * @return DependencyCheck Options
     */
    private Options generateOptions(AbstractBuild build, BuildListener listener) {
        // Generate Options object with universal settings necessary for all Builder steps
        final Options options = optionsBuilder(build, listener, null, isVerboseLoggingEnabled, this.getDescriptor().getTempPath());

        // Configure universal settings useful for all Builder steps
        configureDataDirectory(build, listener, options, datadir);
        configureDataMirroring(options, this.getDescriptor().getDataMirroringType(),
                this.getDescriptor().getCveUrl12Modified(), this.getDescriptor().getCveUrl20Modified(),
                this.getDescriptor().getCveUrl12Base(), this.getDescriptor().getCveUrl20Base());
        configureProxySettings(options, this.getDescriptor().getIsNvdProxyBypassed());

        // Begin configuration for Builder specific settings

        options.setAutoUpdate(true);
        options.setUpdateOnly(true);

        return options;
    }

    /**
     * A Descriptor Implementation.
     */
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    /**
     * Descriptor for {@link DependencyCheckBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     * <p/>
     * <p/>
     * See <tt>src/main/resources/org/jenkinsci/plugins/DependencyCheck/DependencyCheckBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        private DependencyCheckBuilder.DescriptorImpl globalDcDescriptor;


        public DescriptorImpl() {
            globalDcDescriptor = (DependencyCheckBuilder.DescriptorImpl)Jenkins.getInstance().getDescriptor(DependencyCheckBuilder.class);
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types
            return true;
        }

        /**
         * This name is used on the build configuration screen.
         */
        @Override
        public String getDisplayName() {
            return Messages.Builder_UpdateOnly_Name();
        }

        /**
         * This method returns the global configuration for dataMirroringType.
         */
        public int getDataMirroringType() {
            return globalDcDescriptor.getDataMirroringType();
        }

        /**
         * Returns the global configuration to determine if downloading the NVD data feeds shall bypass any proxy defined in Jenkins.
         */
        public boolean getIsNvdProxyBypassed() {
            return globalDcDescriptor.getIsNvdProxyBypassed();
        }

        /**
         * Returns the global configuration for CVE 1.2 modified URL.
         */
        public String getCveUrl12Modified() {
            return globalDcDescriptor.getCveUrl12Modified();
        }

        /**
         * Returns the global configuration for CVE 2.0 modified URL.
         */
        public String getCveUrl20Modified() {
            return globalDcDescriptor.getCveUrl20Modified();
        }

        /**
         * Returns the global configuration for CVE 1.2 base URL.
         */
        public String getCveUrl12Base() {
            return globalDcDescriptor.getCveUrl12Base();
        }

        /**
         * Returns the global configuration for CVE 2.0 base URL.
         */
        public String getCveUrl20Base() {
            return globalDcDescriptor.getCveUrl20Base();
        }

        /**
         * Returns the global configuration for the path to the temporary directory.
         */
        public String getTempPath() {
            return globalDcDescriptor.getTempPath();
        }

    }

}
