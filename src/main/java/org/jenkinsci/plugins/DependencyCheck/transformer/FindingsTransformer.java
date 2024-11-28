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
package org.jenkinsci.plugins.DependencyCheck.transformer;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.apache.commons.text.StringEscapeUtils.escapeHtml4;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.jenkins.ui.symbol.Symbol;
import org.jenkins.ui.symbol.SymbolRequest.Builder;
import org.jenkinsci.plugins.DependencyCheck.model.Dependency;
import org.jenkinsci.plugins.DependencyCheck.model.Finding;
import org.jenkinsci.plugins.DependencyCheck.model.Severity;
import org.jenkinsci.plugins.DependencyCheck.model.Vulnerability;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Converts a list of Findings into a data structure suitable
 * for the FooTable Javascript component.
 *
 * Ported from the Dependency-Track Jenkins plugin.
 *
 * @author Steve Springett (steve.springett@owasp.org)
 * @since 5.0.0
 */
public class FindingsTransformer {
    private static final String FONT_AWESOME_API = "font-awesome-api";

    private String bugSymbol;

    public FindingsTransformer() {
        initSymbols();
    }

    protected void initSymbols() {
        bugSymbol = Symbol.get(new Builder() //
                .withName("solid/bug") //
                .withPluginName(FONT_AWESOME_API) //
                .withClasses("no-issues-banner small-svg-icon") //
                .build());
    }

    public JSONObject transform(List<Finding> findings) {
        final JSONArray columns = new JSONArray();

        final JSONObject fileName = new JSONObject();
        fileName.put("name", "dependency.fileName");
        fileName.put("title", "File Name");
        fileName.put("visible", true);
        fileName.put("filterable", true);
        columns.add(fileName);

        final JSONObject filePath = new JSONObject();
        filePath.put("name", "dependency.filePath");
        filePath.put("title", "File Path");
        filePath.put("breakpoints", "all");
        filePath.put("visible", true);
        filePath.put("filterable", false);
        columns.add(filePath);

        final JSONObject sha1 = new JSONObject();
        sha1.put("name", "dependency.sha1");
        sha1.put("title", "SHA-1");
        sha1.put("breakpoints", "all");
        sha1.put("visible", true);
        sha1.put("filterable", true);
        columns.add(sha1);

        final JSONObject sha256 = new JSONObject();
        sha256.put("name", "dependency.sha256");
        sha256.put("title", "SHA-256");
        sha256.put("breakpoints", "all");
        sha256.put("visible", true);
        sha256.put("filterable", true);
        columns.add(sha256);

        final JSONObject vulnNameLabel = new JSONObject();
        vulnNameLabel.put("name", "vulnerability.nameLabel");
        vulnNameLabel.put("title", "Vulnerability");
        vulnNameLabel.put("visible", true);
        vulnNameLabel.put("filterable", true);
        columns.add(vulnNameLabel);

        final JSONObject severityLabel = new JSONObject();
        severityLabel.put("name", "vulnerability.severityLabel");
        severityLabel.put("title", "Severity");
        severityLabel.put("visible", true);
        severityLabel.put("filterable", true);
        columns.add(severityLabel);

        final JSONObject cwe = new JSONObject();
        cwe.put("name", "vulnerability.cwe");
        cwe.put("title", "Weakness");
        cwe.put("visible", true);
        cwe.put("filterable", true);
        columns.add(cwe);

        final JSONObject vulnDescription = new JSONObject();
        vulnDescription.put("name", "vulnerability.description");
        vulnDescription.put("title", "Description");
        vulnDescription.put("breakpoints", "all");
        vulnDescription.put("visible", true);
        vulnDescription.put("filterable", false);
        columns.add(vulnDescription);

        final JSONObject vulnReferences = new JSONObject();
        vulnReferences.put("name", "vulnerability.references");
        vulnReferences.put("title", "References");
        vulnReferences.put("breakpoints", "all");
        vulnReferences.put("visible", true);
        vulnReferences.put("filterable", false);
        columns.add(vulnReferences);

        final JSONObject projReferences = new JSONObject();
        projReferences.put("name", "dependency.projectReferences");
        projReferences.put("title", "Project References");
        projReferences.put("breakpoints", "all");
        projReferences.put("visible", true);
        projReferences.put("filterable", true);
        columns.add(projReferences);

        final JSONArray rows = new JSONArray();
        for (Finding finding: findings) {
            final Dependency dependency = finding.getDependency();
            final Vulnerability vulnerability = finding.getVulnerability();
            final JSONObject row = new JSONObject();
            row.put("dependency.fileName", createCellWithSortValue(escape(dependency.getFileName()), escape(dependency.getFilePath())));
            row.put("dependency.filePath", escape(dependency.getFilePath()));
            row.put("dependency.description", escape(dependency.getDescription()));
            row.put("dependency.license", escape(dependency.getLicense()));
            row.put("dependency.md5", escape(dependency.getMd5()));
            row.put("dependency.sha1", escape(dependency.getSha1()));
            row.put("dependency.sha256", escape(dependency.getSha256()));
            if (CollectionUtils.isNotEmpty(dependency.getProjectReferences())) {
                row.put("dependency.projectReferences", escape(String.join(", ", dependency.getProjectReferences())));
            }
            row.put("vulnerability.source", vulnerability.getSource());
            row.put("vulnerability.name", escape(vulnerability.getName()));
            row.put("vulnerability.nameLabel", createCellWithSortValue(generateVulnerabilityField(vulnerability), escape(vulnerability.getName())));
            row.put("vulnerability.description", escape(vulnerability.getDescription()));
            if (CollectionUtils.isNotEmpty(vulnerability.getReferences())) {
                StringBuilder referecens = new StringBuilder();
                vulnerability.getReferences().forEach(ref -> {
                    if (isURL(ref.getUrl())) {
                        referecens.append("<a href=\"" + escape(ref.getUrl()) + "\" target=\"_blank\">");
                        referecens.append(escape(ref.getName()));
                        referecens.append("</a>");
                        referecens.append("<br />");
                        referecens.append("\n");
                    }
                });
                row.put("vulnerability.references", referecens.toString());
            }
            row.put("vulnerability.severityLabel", createCellWithSortValue(generateSeverityField(Severity.normalize(vulnerability.getSeverity())), Severity.normalize(vulnerability.getSeverity()).ordinal()));
            row.put("vulnerability.severity", vulnerability.getSeverity());
            row.put("vulnerability.severityRank", Severity.normalize(vulnerability.getSeverity()).ordinal());
            if (CollectionUtils.isNotEmpty(vulnerability.getCwes())) {
                row.put("vulnerability.cwe", vulnerability.getCwes().get(0));
            }
            rows.add(row);
        }
        final JSONObject data = new JSONObject();
        data.put("columns", columns);
        data.put("rows", rows);
        return data;
    }

    // verify if the url is a real URL
    private boolean isURL(String url) {
        if (isNotBlank(url)) {
            try {
                new URL(url);
                return true;
            } catch (MalformedURLException e) {
            }
        }
        return false;
    }

    private String escape(String content) {
        return escapeHtml4(trimToEmpty(content));
    }

    private JSONObject createCellWithSortValue(Object aValue, Object aSortValue) {
        JSONObject tObject = new JSONObject();
        tObject.put("value", aValue != null ? aValue.toString() : "");
        JSONObject tOptions = new JSONObject();
        tOptions.put("sortValue", aSortValue != null ? aSortValue.toString() : "");
        tObject.put("options", tOptions);
        return tObject;
    }

    private String generateSeverityField(Severity severity) {
        return "<div style=\"height:24px;margin:-4px;\">\n" +
                "<div class=\"severity-" + severity.name().toLowerCase() + "-bg text-center pull-left\" style=\"width:24px; height:24px; color:#ffffff\">\n" +
                bugSymbol + //
                "</div>\n" +
                "<div class=\"text-center pull-left\" style=\"height:24px;\">\n" +
                "  <div style=\"font-size:12px; padding:4px\"><span class=\"severity-value\">" + convert(severity.name()) + "</span></div>\n" +
                "</div>\n" +
                "</div>";
    }

    private String generateVulnerabilityField(Vulnerability vulnerability) {
        String source = vulnerability.getSource().name();
        String id = escape(vulnerability.getName());
        return "<span class=\"vuln-source vuln-source-" + source.toLowerCase() + "\">" + source + "</span>" + escape(id);
    }

    private String convert(String str) {
        char[] ch = str.toCharArray();
        for (int i = 0; i < str.length(); i++) {
            if (i == 0 && ch[i] != ' ' || ch[i] != ' ' && ch[i - 1] == ' ') {
                if (ch[i] >= 'a' && ch[i] <= 'z') {
                    ch[i] = (char) (ch[i] - 'a' + 'A');
                }
            } else if (ch[i] >= 'A' && ch[i] <= 'Z') {
                ch[i] = (char) (ch[i] + 'a' - 'A');
            }
        }
        return new String(ch);
    }
}