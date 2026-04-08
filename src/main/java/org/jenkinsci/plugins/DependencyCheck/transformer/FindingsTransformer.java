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

import static java.util.stream.Collectors.joining;
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
 * Converts a list of Findings into a JSON structure suitable
 * for the DataTables.net Javascript component.
 * <p>
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

        columns.add(buildColumn("dependency\\.fileName", "File Name", true, true, false));
        columns.add(buildColumn("dependency\\.filePath", "File Path", true, false, true));
        columns.add(buildColumn("dependency\\.sha1", "SHA-1", true, true, true));
        columns.add(buildColumn("dependency\\.sha256", "SHA-256", true, true, true));
        columns.add(buildColumn("vulnerability\\.nameLabel", "Vulnerability", true, true, false));
        columns.add(buildColumn("vulnerability\\.severityLabel", "Severity", true, true, false));
        columns.add(buildColumn("vulnerability\\.cwe", "Weakness", true, true, false));
        columns.add(buildColumn("vulnerability\\.description", "Description", true, false, true));
        columns.add(buildColumn("vulnerability\\.references", "References", true, false, true));
        columns.add(buildColumn("dependency\\.projectReferences", "Referenced In Projects/Scopes", true, true, true));

        final JSONArray rows = new JSONArray();
        for (Finding finding: findings) {
            final Dependency dependency = finding.getDependency();
            final Vulnerability vulnerability = finding.getVulnerability();
            final JSONObject row = new JSONObject();
            row.put("dependency.fileName", displaySort(escape(dependency.getFileName()), escape(dependency.getFilePath())));
            row.put("dependency.filePath", escape(dependency.getFilePath()));
            row.put("dependency.description", escape(dependency.getDescription()));
            row.put("dependency.license", escape(dependency.getLicense()));
            row.put("dependency.md5", escape(dependency.getMd5()));
            row.put("dependency.sha1", escape(dependency.getSha1()));
            row.put("dependency.sha256", escape(dependency.getSha256()));
            if (CollectionUtils.isNotEmpty(dependency.getProjectReferences())) {
                row.put("dependency.projectReferences", dependency.getProjectReferences().stream()
                        .map(s -> "<li>" + escape(s) + "</li>")
                        .collect(joining("", "<ul>", "</ul>")));
            }
            row.put("vulnerability.source", vulnerability.getSource());
            row.put("vulnerability.name", escape(vulnerability.getName()));
            row.put("vulnerability.nameLabel", displaySort(generateVulnerabilityField(vulnerability), escape(vulnerability.getName())));
            row.put("vulnerability.description", escape(vulnerability.getDescription()));
            if (CollectionUtils.isNotEmpty(vulnerability.getReferences())) {
                StringBuilder references = new StringBuilder();
                vulnerability.getReferences().forEach(ref -> {
                    if (isURL(ref.getUrl())) {
                        references.append("<a href=\"" + escape(ref.getUrl()) + "\" target=\"_blank\">");
                        references.append(escape(ref.getName()));
                        references.append("</a>");
                        references.append("<br />");
                        references.append("\n");
                    }
                });
                row.put("vulnerability.references", references.toString());
            }
            final Severity severity = Severity.normalize(vulnerability.getSeverity());
            row.put("vulnerability.severityLabel", displaySort(generateSeverityField(severity), severity.ordinal()));
            row.put("vulnerability.severity", vulnerability.getSeverity());
            row.put("vulnerability.severityRank", severity.ordinal());
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

    /**
     * Builds a DataTables column definition.
     *
     * @param data         the column data key (dots must already be escaped with a backslash for DataTables)
     * @param title        column header text
     * @param visible      whether the column is visible by default
     * @param searchable   whether the column participates in the global search
     * @param responsive   when true the column collapses on small screens (DataTables responsive "none" class)
     */
    private JSONObject buildColumn(String data, String title, boolean visible, boolean searchable, boolean responsive) {
        final JSONObject col = new JSONObject();
        col.put("data", data);
        col.put("title", title);
        col.put("visible", visible);
        col.put("searchable", searchable);
        col.put("defaultContent", "");
        if (responsive) {
            col.put("className", "none");
        }
        return col;
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

    /**
     * Builds a DataTables orthogonal data object with separate display and sort values.
     * DataTables will use {@code display} for rendering and {@code sort} for ordering.
     */
    private JSONObject displaySort(Object display, Object sort) {
        final JSONObject cell = new JSONObject();
        cell.put("display", display != null ? display.toString() : "");
        cell.put("sort", sort != null ? sort.toString() : "");
        return cell;
    }

    private String generateSeverityField(Severity severity) {
        return "<div style=\"display:flex; align-items:center; gap:4px;\">\n" +
                "<div class=\"severity-" + severity.name().toLowerCase() + "-bg text-center\" style=\"width:24px; height:24px; flex-shrink:0; color:#ffffff;\">\n" +
                bugSymbol +
                "</div>\n" +
                "<span class=\"severity-value\" style=\"font-size:12px;\">" + convert(severity.name()) + "</span>\n" +
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
