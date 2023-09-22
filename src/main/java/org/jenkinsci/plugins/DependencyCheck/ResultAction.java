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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jenkinsci.plugins.DependencyCheck.action.ResultProjectAction;
import org.jenkinsci.plugins.DependencyCheck.charts.DependencyCheckBuildResult;
import org.jenkinsci.plugins.DependencyCheck.charts.DependencyCheckBuildResultXmlStream;
import org.jenkinsci.plugins.DependencyCheck.model.Finding;
import org.jenkinsci.plugins.DependencyCheck.model.SeverityDistribution;
import org.jenkinsci.plugins.DependencyCheck.transformer.FindingsTransformer;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.json.JsonHttpResponse;
import org.kohsuke.stapler.verb.GET;

import hudson.model.Action;
import hudson.model.Api;
import hudson.model.Run;
import io.jenkins.plugins.util.AbstractXmlStream;
import io.jenkins.plugins.util.BuildAction;
import io.jenkins.plugins.util.JobAction;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

/**
 * Ported from the Dependency-Track Jenkins plugin.
 *
 * @author Steve Springett (steve.springett@owasp.org)
 * @since 5.0.0
 */
@ExportedBean
public class ResultAction extends BuildAction<DependencyCheckBuildResult> {

    private static final long serialVersionUID = -6533677178186658819L;
    private transient List<Finding> findings; // required for backward compatibility, will be removed
    private transient SeverityDistribution severityDistribution; // required for backward compatibility, will be removed

    public ResultAction(final Run<?, ?> owner, List<Finding> findings, SeverityDistribution severityDistribution) {
        super(owner, new DependencyCheckBuildResult(findings, severityDistribution));
    }

    @Override
    public String getIconFileName() {
        return "/plugin/" + DependencyCheckConstants.PLUGIN_ID + "/icons/dependency-check-icon.svg";
    }

    @Override
    public String getDisplayName() {
        return "Dependency-Check";
    }

    @Override
    public String getUrlName() {
        return "dependency-check-findings";
    }

    @Override
    protected AbstractXmlStream<DependencyCheckBuildResult> createXmlStream() {
        return new DependencyCheckBuildResultXmlStream();
    }

    @Override
    public Collection<? extends Action> getProjectActions() {
        Collection<Action> prjActions = new ArrayList<>(2);
        prjActions.addAll(super.getProjectActions());
        prjActions.add(new ResultProjectAction(getOwner().getParent()));
        return Collections.unmodifiableCollection(prjActions);
    }

    @Override
    protected JobAction<? extends BuildAction<DependencyCheckBuildResult>> createProjectAction() {
        return new org.jenkinsci.plugins.DependencyCheck.JobAction(getOwner().getParent());
    }

    @Override
    protected String getBuildResultBaseName() {
        return "vulnerabilityReport.xml";
    }

    public SeverityDistribution getSeverityDistribution() {
        migrate();
        return getResult().getSeverityDistribution();
    }

    private void migrate() {
        if (severityDistribution != null || findings != null) {
            Path resultXML = getOwner().getRootDir().toPath().resolve(getBuildResultBaseName());
            if (!resultXML.toFile().exists()) {
                createXmlStream().write(resultXML, new DependencyCheckBuildResult(findings, severityDistribution));
            }
            findings = null;
            severityDistribution = null;
        }
    }

    public List<Finding> getFindings() {
        migrate();
        return getResult().getFindings();
    }

    /**
     * Returns the UI model for an ECharts line chart that shows the findings.
     *
     * @return the UI model as JSON
     */
    @JavaScriptMethod
    public JSONObject getFindingsJson() {
        final FindingsTransformer transformer = new FindingsTransformer();
        return transformer.transform(getFindings());
    }

    /**
     * Returns a JSON response with the statistics for severity.
     *
     * @return the UI model as JSON
     */
    @JavaScriptMethod
    public JSONObject getSeverityDistributionJson() {
        JsonConfig jsonConfig = new JsonConfig();
        jsonConfig.setExcludes(new String[] { "buildNumber" });
        return JSONObject.fromObject(getSeverityDistribution(), jsonConfig);
    }

    public Api getApi() {
        return new Api(this);
    }

    @Exported(visibility = 2)
    public int getCriticalCount() {
        return getSeverityDistribution().getCritical();
    }

    @Exported(visibility = 2)
    public int getHighCount() {
        return getSeverityDistribution().getHigh();
    }

    @Exported(visibility = 2)
    public int getMediumCount() {
        return getSeverityDistribution().getMedium();
    }

    @Exported(visibility = 2)
    public int getLowCount() {
        return getSeverityDistribution().getLow();
    }

    @Exported(visibility = 2)
    public int getInfoCount() {
        return getSeverityDistribution().getInfo();
    }

    @Exported(visibility = 2)
    public int getUnassignedCount() {
        return getSeverityDistribution().getUnassigned();
    }

    @GET
    @WebMethod(name = "findings")
    public JsonHttpResponse getFindingsExposedInRemoteApi() {
        JSONObject tResult = new JSONObject();
        JsonConfig jsonConfig = new JsonConfig();
        jsonConfig.setExcludes(new String[] { "count", });
        tResult.put("findings", JSONArray.fromObject(getFindings(), jsonConfig));
        return new JsonHttpResponse(tResult, 200);
    }
}
