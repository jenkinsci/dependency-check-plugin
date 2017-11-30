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
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.Base64;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.security.MasterToSlaveCallable;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import javax.annotation.Nonnull;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DependencyTrackPublisher extends Recorder implements SimpleBuildStep, Serializable {

    private static final long serialVersionUID = 831930330658830569L;

    private final String projectId;
    private final String scanResult;
    private transient PrintStream logger;

    @DataBoundConstructor // Fields in config.jelly must match the parameter names
    public DependencyTrackPublisher(final String projectId, final String scanResult) {
        this.projectId = projectId;
        this.scanResult = scanResult;
    }

    /**
     * Retrieves the project ID to upload to. This is a per-build config item.
     * This method must match the value in <tt>config.jelly</tt>.
     */
    public String getProjectId() {
        return projectId;
    }

    /**
     * Retrieves the path and filename of the scan result. This is a per-build config item.
     * This method must match the value in <tt>config.jelly</tt>.
     */
    public String getScanResult() {
        return scanResult;
    }

    /**
     * This method is called whenever the build step is executed.
     *
     * @param build    A Run object
     * @param filePath A FilePath object
     * @param launcher A Launcher object
     * @param listener A BuildListener object
     */
    @Override
    public void perform(@Nonnull final Run<?, ?> build,
                        @Nonnull final FilePath filePath,
                        @Nonnull final Launcher launcher,
                        @Nonnull final TaskListener listener) throws InterruptedException, IOException {

        this.logger = listener.getLogger();
        log(Messages.DtrackBuilder_Publishing());

        final String projectId = PluginUtil.substituteVariable(build, listener, this.projectId);
        final String scanResult = PluginUtil.substituteVariable(build, listener, this.scanResult);

        boolean success = launcher.getChannel().call(new MasterToSlaveCallable<Boolean, IOException>() {
            public Boolean call() throws IOException {
                return uploadScan(listener, projectId, scanResult);
            }
        });

        if (!success) {
            build.setResult(Result.FAILURE);
        }
    }

    /**
     * Log messages to the builds console.
     * @param message The message to log
     */
    private void log(String message) {
        if (logger == null || message == null) {
            return;
        }
        final String outtag = "[" + DependencyCheckPlugin.DT_PLUGIN_NAME + "] ";
        logger.println(outtag + message.replaceAll("\\n", "\n" + outtag));
    }

    private boolean uploadScan(TaskListener listener, String projectId, String scanResult) throws IOException {
        final FilePath filePath = new FilePath(new File(scanResult));
        final String encodedScan;
        try {
            if (!filePath.exists()) {
                log(Messages.DtrackBuilder_Result_NonExist());
                return false;
            }
            encodedScan = Base64.encode(filePath.readToString().getBytes());
        } catch (IOException | InterruptedException e) {
            log(Messages.DtrackBuilder_Error_Processing() + ": " + e.getMessage());
            return false;
        }

        // Creates the JSON payload that will be sent to Dependency-Track
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        JsonObject jsonObject = jsonObjectBuilder
                .add("project", projectId)
                .add("scan", encodedScan).build();

        byte[] payloadBytes = jsonObject.toString().getBytes(StandardCharsets.UTF_8);

        // Creates the request and connects
        final HttpURLConnection conn = (HttpURLConnection) new URL(getDescriptor().getDependencyTrackUrl()
                + "/api/v1/scan").openConnection();
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Content-Length", Integer.toString(payloadBytes.length));
        conn.setRequestProperty("X-Api-Key", getDescriptor().getDependencyTrackApiKey());
        conn.connect();

        // Sends the payload bytes
        try (OutputStream os = new BufferedOutputStream(conn.getOutputStream())) {
            os.write(payloadBytes);
            os.flush();
        }

        // Checks the server response
        if (conn.getResponseCode() == 200) {
            log(Messages.DtrackBuilder_Success());
            return true;
        } else if (conn.getResponseCode() == 400) {
            log(Messages.DtrackBuilder_Payload_Invalid());
        } else if (conn.getResponseCode() == 401) {
            log(Messages.DtrackBuilder_Unauthorized());
        } else if (conn.getResponseCode() == 404) {
            log(Messages.DtrackBuilder_Project_NotFound());
        } else {
            log(Messages.DtrackBuilder_Error_Connect() + ": "
                    + conn.getResponseCode() + " " + conn.getResponseMessage());
        }
        return false;
    }

    /**
     * A Descriptor Implementation.
     */
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    /**
     * Descriptor for {@link DependencyTrackPublisher}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     * <p/>
     * <p/>
     * See <tt>src/main/resources/org/jenkinsci/plugins/DependencyCheck/DependencyTrackPublisher/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension @Symbol("dependencyTrackPublisher") // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> implements Serializable {

        private static final long serialVersionUID = -2018722914973282748L;

        /**
         * Specifies the base URL to Dependency-Track v3 or higher.
         */
        private String dependencyTrackUrl;

        /**
         * Specifies an API Key used for authentication (if authentication is required).
         */
        private String dependencyTrackApiKey;

        /**
         * Default constructor. Obtains the Descriptor used in DependencyCheckBuilder as this contains
         * the global Dependency-Check Jenkins plugin configuration.
         */
        public DescriptorImpl() {
            super(DependencyTrackPublisher.class);
            load();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types
            return true;
        }

        /**
         * Retrieve the projects to populate the dropdown.
         */
        public ListBoxModel doFillProjectIdItems() {
            final ListBoxModel projects = new ListBoxModel();
            try {
                // Creates the request and connects
                final HttpURLConnection conn = (HttpURLConnection) new URL(getDependencyTrackUrl() + "/api/v1/project")
                        .openConnection();
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("X-Api-Key", getDependencyTrackApiKey());
                conn.connect();

                // Checks the server response
                if (conn.getResponseCode() == 200) {
                    try (InputStream in = new BufferedInputStream(conn.getInputStream())) {
                        JsonReader jsonReader = Json.createReader(in);
                        JsonArray array = jsonReader.readArray();
                        if (array != null) {
                            for (int i = 0; i < array.size(); i++) {
                                JsonObject jsonObject = array.getJsonObject(i);
                                String name = jsonObject.getString("name");
                                String version = jsonObject.getString("version", "null");
                                String uuid = jsonObject.getString("uuid");
                                if (!version.equals("null")) {
                                    name = name + " " + version;
                                }
                                projects.add(name, uuid);
                            }
                        }
                    }
                } else {
                    projects.add(Messages.DtrackBuilder_Error_Projects() + ": " + conn.getResponseCode());
                }
            } catch (IOException e) {
                projects.add(e.getMessage());
            }
            return projects;
        }

        /**
         * Performs input validation when submitting the global config
         * @param value The value of the URL as specified in the global config
         * @return a FormValidation object
         */
        public FormValidation doCheckDependencyTrackUrl(@QueryParameter String value) {
            return PluginUtil.doCheckUrl(value);
        }

        /**
         * Takes the /apply/save step in the global config and saves the JSON data.
         * @param req the request
         * @param formData the form data
         * @return a boolean
         * @throws FormException
         */
        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            dependencyTrackUrl = formData.getString("dependencyTrackUrl");
            dependencyTrackApiKey = formData.getString("dependencyTrackApiKey");
            save();
            return super.configure(req, formData);
        }

        /**
         * This name is used on the build configuration screen.
         */
        @Override
        public String getDisplayName() {
            return Messages.Publisher_DependencyTrack_Name();
        }

        /**
         * This method returns the global configuration for dependencyTrackUrl.
         */
        public String getDependencyTrackUrl() {
            return dependencyTrackUrl;
        }

        /**
         * This method returns the global configuration for dependencyTrackApiKey.
         */
        public String getDependencyTrackApiKey() {
            return dependencyTrackApiKey;
        }

    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

}
