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

import org.owasp.dependencycheck.reporting.ReportGenerator;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A container object that holds all of the configurable options to be used by
 * a DependencyCheck analysis.
 *
 * @author Steve Springett (steve.springett@owasp.org)
 */
@SuppressWarnings("unused")
public class Options implements Serializable {

    private static final long serialVersionUID = 4571161829818421072L;

    /**
     * The path to the project workspace.
     */
    private String workspace;

    /**
     * The name of the report to be displayed
     */
    private String name;

    /**
     * Specifies the directory[s] to scan.
     */
    private ArrayList<String> scanPath = new ArrayList<>();

    /**
     * Specifies the destination directory for the generated report.
     */
    private String outputDirectory;

    /**
     * Specifies the data directory.
     */
    private String dataDirectory;

    /**
     * Specifies the database connection string.
     */
    private String dbconnstr;

    /**
     * Specifies the database driver name.
     */
    private String dbdriver;

    /**
     * Specifies the database driver path.
     */
    private String dbpath;

    /**
     * Specifies the database user.
     */
    private String dbuser;

    /**
     * Specifies the database password.
     */
    private String dbpassword;

    /**
     * Boolean value (true/false) whether or not the evidence collected
     * about a dependency is displayed in the report. Default is true.
     */
    private boolean showEvidence = true;

    /**
     * The report format to be generated. Default is XML.
     */
    private List<ReportGenerator.Format> formats = new ArrayList<>();

    /**
     * Sets whether auto-updating of the NVD CVE/CPE data is enabled. It is not
     * recommended that this be turned to false. Default is true.
     */
    private boolean autoUpdate = true;

    /**
     * Sets whether an NVD update should be the only thing performed. If true,
     * a scan will not be performed.
     */
    private boolean updateOnly = false;

    /**
     * Specifies the suppression file to use
     */
    private String suppressionFile;

    /**
     * Specifies the hints file to use
     */
    private String hintsFile;

    /**
     * Specifies the file extensions to be treated a ZIP
     */
    private String zipExtensions;

    /**
     * Specifies the data mirroring type (scheme) to use
     */
    private int dataMirroringType;

    /**
     * Specifies the CVE 1.2 modified URL
     */
    private URL cveUrl12Modified;

    /**
     * Specifies the CVE 2.0 modified URL
     */
    private URL cveUrl20Modified;

    /**
     * Specifies the CVE 1.2 base URL
     */
    private URL cveUrl12Base;

    /**
     * Specifies the CVE 2.0 base URL
     */
    private URL cveUrl20Base;

    /**
     * Specifies the server (hostname/IP) of the proxy server
     */
    private String proxyServer;

    /**
     * Specifies the port number to the proxy server
     */
    private int proxyPort;

    /**
     * Specifies the username to use to authenticate through the proxy server
     */
    private String proxyUsername;

    /**
     * Specifies the password to use to authenticate through the proxy server
     */
    private String proxyPassword;

    /**
     * Specifies is the HTTP GET method is used for timestamp retrieval instead of HTTP HEAD.
     * If QuickQuery is enabled, HTTP HEAD will be used.
     */
    private boolean isQuickQueryTimestampEnabled = true;

    /**
     * Specifies if the Jar analyzer is enabled
     */
    private boolean jarAnalyzerEnabled;

    /**
     * Specifies if the Node.js analyzer is enabled
     */
    private boolean nodeJsAnalyzerEnabled;

    /**
     * Specifies if the NSP analyzer is enabled
     */
    private boolean nspAnalyzerEnabled;

    /**
     * Specifies if the PHP Composer.lock analyzer is enabled
     */
    private boolean composerLockAnalyzerEnabled;

    /**
     * Specifies if the Python distribution analyzer is enabled
     */
    private boolean pythonDistributionAnalyzerEnabled;

    /**
     * Specifies if the Python package analyzer is enabled
     */
    private boolean pythonPackageAnalyzerEnabled;

    /**
     * Specifies if the Ruby Bundler Audit analyzer is enabled
     */
    private boolean rubyBundlerAuditAnalyzerEnabled;

    /**
     * Specifies if the Ruby Gem analyzer is enabled
     */
    private boolean rubyGemAnalyzerEnabled;

    /**
     * Specifies if the CocoaPods analyzer is enabled
     */
    private boolean cocoaPodsAnalyzerEnabled;

    /**
     * Specifies if the Swift Package Manager analyzer is enabled
     */
    private boolean swiftPackageManagerAnalyzerEnabled;

    /**
     * Specifies if the Archive analyzer is enabled
     */
    private boolean archiveAnalyzerEnabled;

    /**
     * Specifies if the Assembly analyzer is enabled
     */
    private boolean assemblyAnalyzerEnabled;

    /**
     * Specifies if the NuSpec analyzer is enabled
     */
    private boolean nuspecAnalyzerEnabled;

    /**
     * Specifies if the Nexus analyzer is enabled
     */
    private boolean nexusAnalyzerEnabled;

    /**
     * Specifies if the autoconf analyzer is enabled
     */
    private boolean autoconfAnalyzerEnabled;

    /**
     * Specifies if the cmake analyzer is enabled
     */
    private boolean cmakeAnalyzerEnabled;

    /**
     * Specifies if the OpenSSL analyzer is enabled
     */
    private boolean opensslAnalyzerEnabled;

    /**
     * Specifies the Nexus URL to use if enabled
     */
    private URL nexusUrl;

    /**
     * Specifies if the Nexus analyzer should bypass any proxy defined in Jenkins
     */
    private boolean nexusProxyBypass;

    /**
     * Specifies if the Maven Central analyzer is enabled
     */
    private boolean centralAnalyzerEnabled;

    /**
     * Specifies the Maven Central URL to use if enabled
     */
    private URL centralUrl;

    /**
     * Specifies the full path and filename to the Mono binary
     */
    private String monoPath;

    /**
     * Specifies the full path and filename to the bundle-audit binary
     */
    private String bundleAuditPath;

    /**
     * Specifies the full path to the temporary directory
     */
    private String tempPath;

    /**
     * Default Constructor.
     */
    public Options() {
        formats.add(ReportGenerator.Format.XML);
    }

    /**
     * Returns the path to the project workspace.
     */
    public String getWorkspace() {
        return workspace;
    }

    /**
     * Returns the name of the report to be displayed.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the report to be displayed.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the files and/or directory[s] to scan.
     */
    public ArrayList<String> getScanPath() {
        return scanPath;
    }

    /**
     * Sets the file[s] and/or directory[s] to scan.
     */
    public void setScanPath(ArrayList<String> scanPath) {
        this.scanPath = scanPath;
    }

    /**
     * Add a file and/or directory to scan.
     */
    public void addScanPath(String scanPath) {
        if (this.scanPath == null) {
            this.scanPath = new ArrayList<String>();
        }
        this.scanPath.add(scanPath);
    }

    /**
     * Returns the destination directory for the generated report.
     */
    public String getOutputDirectory() {
        return outputDirectory;
    }

    /**
     * Sets the destination directory for the generated report.
     */
    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    /**
     * Returns the data directory.
     */
    public String getDataDirectory() {
        return dataDirectory;
    }

    /**
     * Sets the path to the project workspace.
     */
    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    /**
     * Sets the data directory.
     */
    public void setDataDirectory(String dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    /**
     * Sets the database connection string.
     */
    public void setDbconnstr(String dbconnstr) {
        this.dbconnstr = dbconnstr;
    }

    /**
     * Gets the database connection string.
     */
    public String getDbconnstr() {
        return dbconnstr;
    }

    /**
     * Sets the database driver name.
     */
    public void setDbdriver(String dbdriver) {
        this.dbdriver = dbdriver;
    }

    /**
     * Gets the database driver name.
     */
    public String getDbdriver() {
        return dbdriver;
    }

    /**
     * Sets the database driver path.
     */
    public void setDbpath(String dbpath) {
        this.dbpath = dbpath;
    }

    /**
     * Gets the database driver path.
     */
    public String getDbpath() {
        return dbpath;
    }

    /**
     * Sets the database user.
     */
    public void setDbuser(String dbuser) {
        this.dbuser = dbuser;
    }

    /**
     * Gets the database user.
     */
    public String getDbuser() {
        return dbuser;
    }

    /**
     * Sets the database password.
     */
    public void setDbpassword(String dbpassword) {
        this.dbpassword = dbpassword;
    }

    /**
     * Gets the database password.
     */
    public String getDbpassword() {
        return dbpassword;
    }

    /**
     * Returns a boolean value (true/false) whether or not the evidence collected
     * about a dependency is displayed in the report. Default is false.
     */
    public boolean isShowEvidence() {
        return showEvidence;
    }

    /**
     * Sets a boolean value (true/false) whether or not the evidence collected
     * about a dependency is displayed in the report.
     */
    public void setShowEvidence(boolean showEvidence) {
        this.showEvidence = showEvidence;
    }

    /**
     * Returns the report formats to be generated. Default is XML.
     */
    public List<ReportGenerator.Format> getFormats() {
        return formats;
    }

    /**
     * Adds a report format to be generated.
     */
    public void addFormat(ReportGenerator.Format format) {
        formats.add(format);
    }

    /**
     * Returns whether auto-updating of the NVD CVE/CPE data is enabled. It is not
     * recommended that this be turned to false. Default is true.
     */
    public boolean isAutoUpdate() {
        return autoUpdate;
    }

    /**
     * Sets whether auto-updating of the NVD CVE/CPE data is enabled. It is not
     * recommended that this be turned to false.
     */
    public void setAutoUpdate(boolean autoUpdate) {
        this.autoUpdate = autoUpdate;
    }

    /**
     * Returns whether updates should be the only task performed.
     */
    public boolean isUpdateOnly() {
        return updateOnly;
    }

    /**
     * Sets whether an NVD update should be the only thing performed. If true,
     * a scan will not be performed.
     */
    public void setUpdateOnly(boolean updateOnly) {
        this.updateOnly = updateOnly;
    }

    /**
     * Returns the suppression file.
     */
    public String getSuppressionFile() {
        return suppressionFile;
    }

    /**
     * Sets the suppression file to use.
     */
    public void setSuppressionFile(String file) {
        this.suppressionFile = file;
    }

    /**
     * Returns the hints file.
     */
    public String getHintsFile() {
        return hintsFile;
    }

    /**
     * Sets the hints file to use.
     */
    public void setHintsFile(String file) {
        this.hintsFile = file;
    }

    /**
     * Returns the zip extensions.
     */
    public String getZipExtensions() {
        return zipExtensions;
    }

    /**
     * Sets the zip extensions - must be comma separated.
     */
    public void setZipExtensions(String extensions) {
        this.zipExtensions = extensions;
    }

    /**
     * Returns the data mirroring type (scheme) to use where 0 = none and 1 = NIST CPE/CVE.
     */
    public int getDataMirroringType() {
        return dataMirroringType;
    }

    /**
     * Sets the data mirroring type.
     */
    public void setDataMirroringType(int dataMirroringType) {
        this.dataMirroringType = dataMirroringType;
    }

    /**
     * Returns the CVE 1.2 modified URL.
     */
    public URL getCveUrl12Modified() {
        return cveUrl12Modified;
    }

    /**
     * Sets the CVE 1.2 modified URL.
     */
    public void setCveUrl12Modified(URL url) {
        this.cveUrl12Modified = url;
    }

    /**
     * Returns the CVE 2.0 modified URL.
     */
    public URL getCveUrl20Modified() {
        return cveUrl20Modified;
    }

    /**
     * Sets the CVE 2.0 modified URL.
     */
    public void setCveUrl20Modified(URL url) {
        this.cveUrl20Modified = url;
    }

    /**
     * Returns the CVE 1.2 base URL.
     */
    public URL getCveUrl12Base() {
        return cveUrl12Base;
    }

    /**
     * Sets the CVE 1.2 base URL.
     */
    public void setCveUrl12Base(URL url) {
        this.cveUrl12Base = url;
    }

    /**
     * Returns the CVE 2.0 base URL.
     */
    public URL getCveUrl20Base() {
        return cveUrl20Base;
    }

    /**
     * Sets the CVE 2.0 base URL.
     */
    public void setCveUrl20Base(URL url) {
        this.cveUrl20Base = url;
    }

    /**
     * Returns the server (hostname/IP) of the proxy server.
     */
    public String getProxyServer() {
        return proxyServer;
    }

    /**
     * Sets the server (hostname/IP) of the proxy server.
     */
    public void setProxyServer(String proxyServer) {
        this.proxyServer = proxyServer;
    }

    /**
     * Returns the port number to the proxy server.
     */
    public int getProxyPort() {
        return proxyPort;
    }

    /**
     * Sets the port number to the proxy server.
     */
    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    /**
     * Returns the username to use to authenticate through the proxy server.
     */
    public String getProxyUsername() {
        return proxyUsername;
    }

    /**
     * Sets the username to use to authenticate through the proxy server.
     */
    public void setProxyUsername(String proxyUsername) {
        this.proxyUsername = proxyUsername;
    }

    /**
     * Returns the password to use to authenticate through the proxy server.
     */
    public String getProxyPassword() {
        return proxyPassword;
    }

    /**
     * Sets the password to use to authenticate through the proxy server.
     */
    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    /**
     * Returns if HTTP GET is used to retrieve timestamp information instead of HTTP HEAD.
     * If QuickQuery is enabled, HTTP HEAD will be used.
     */
    public boolean isQuickQueryTimestampEnabled() {
        return isQuickQueryTimestampEnabled;
    }

    /**
     * Sets if HTTP GET is used to retrieve timestamp information instead of HTTP HEAD.
     * If QuickQuery is enabled, HTTP HEAD will be used.
     */
    public void setIsQuickQueryTimestampEnabled(boolean isQuickQueryTimestampEnabled) {
        this.isQuickQueryTimestampEnabled = isQuickQueryTimestampEnabled;
    }

    /**
     * Returns if the Jar analyzer is enabled or not.
     */
    public boolean isJarAnalyzerEnabled() {
        return jarAnalyzerEnabled;
    }

    /**
     * Sets if the Jar analyzer is enabled or not.
     */
    public void setJarAnalyzerEnabled(boolean jarAnalyzerEnabled) {
        this.jarAnalyzerEnabled = jarAnalyzerEnabled;
    }

    /**
     * Returns if the Node.js analyzer is enabled or not.
     */
    public boolean isNodeJsAnalyzerEnabled() {
        return nodeJsAnalyzerEnabled;
    }

    /**
     * Sets if the Node.js analyzer is enabled or not.
     */
    public void setNodeJsAnalyzerEnabled(boolean nodeJsAnalyzerEnabled) {
        this.nodeJsAnalyzerEnabled = nodeJsAnalyzerEnabled;
    }

    /**
     * Returns if the NSP analyzer is enabled or not.
     */
    public boolean isNspAnalyzerEnabled() {
        return nspAnalyzerEnabled;
    }

    /**
     * Sets if the NSP analyzer is enabled or not.
     */
    public void setNspAnalyzerEnabled(boolean nspAnalyzerEnabled) {
        this.nspAnalyzerEnabled = nspAnalyzerEnabled;
    }

    /**
     * Returns if the PHP Composer.lock analyzer is enabled or not.
     */
    public boolean isComposerLockAnalyzerEnabled() {
        return composerLockAnalyzerEnabled;
    }

    /**
     * Sets if the PHP Composer.lock analyzer is enabled or not.
     */
    public void setComposerLockAnalyzerEnabled(boolean composerLockAnalyzerEnabled) {
        this.composerLockAnalyzerEnabled = composerLockAnalyzerEnabled;
    }

    /**
     * Returns if the Python distribution analyzer is enabled or not.
     */
    public boolean isPythonDistributionAnalyzerEnabled() {
        return pythonDistributionAnalyzerEnabled;
    }

    /**
     * Sets if the Python distribution analyzer is enabled or not.
     */
    public void setPythonDistributionAnalyzerEnabled(boolean pythonDistributionAnalyzerEnabled) {
        this.pythonDistributionAnalyzerEnabled = pythonDistributionAnalyzerEnabled;
    }

    /**
     * Returns if the Python package analyzer is enabled or not.
     */
    public boolean isPythonPackageAnalyzerEnabled() {
        return pythonPackageAnalyzerEnabled;
    }

    /**
     * Sets if the Python package analyzer is enabled or not.
     */
    public void setPythonPackageAnalyzerEnabled(boolean pythonPackageAnalyzerEnabled) {
        this.pythonPackageAnalyzerEnabled = pythonPackageAnalyzerEnabled;
    }

    /**
     * Returns if the Ruby Bundler Audit analyzer is enabled or not.
     */
    public boolean isRubyBundlerAuditAnalyzerEnabled() {
        return rubyBundlerAuditAnalyzerEnabled;
    }

    /**
     * Sets if the Ruby Bundler Audit analyzer is enabled or not.
     */
    public void setRubyBundlerAuditAnalyzerEnabled(boolean rubyBundlerAuditAnalyzerEnabled) {
        this.rubyBundlerAuditAnalyzerEnabled = rubyBundlerAuditAnalyzerEnabled;
    }

    /**
     * Returns if the Ruby Gem analyzer is enabled or not.
     */
    public boolean isRubyGemAnalyzerEnabled() {
        return rubyGemAnalyzerEnabled;
    }

    /**
     * Sets if the Ruby Gem analyzer is enabled or not.
     */
    public void setRubyGemAnalyzerEnabled(boolean rubyGemAnalyzerEnabled) {
        this.rubyGemAnalyzerEnabled = rubyGemAnalyzerEnabled;
    }

    /**
     * Returns if the CocoaPods analyzer is enabled or not.
     */
    public boolean isCocoaPodsAnalyzerEnabled() {
        return cocoaPodsAnalyzerEnabled;
    }

    /**
     * Sets if the CocoaPods analyzer is enabled or not.
     */
    public void setCocoaPodsAnalyzerEnabled(boolean cocoaPodsAnalyzerEnabled) {
        this.cocoaPodsAnalyzerEnabled = cocoaPodsAnalyzerEnabled;
    }

    /**
     * Returns if the Swift Package Manager analyzer is enabled or not.
     */
    public boolean isSwiftPackageManagerAnalyzerEnabled() {
        return swiftPackageManagerAnalyzerEnabled;
    }

    /**
     * Sets if the Swift Package Manager analyzer is enabled or not.
     */
    public void setSwiftPackageManagerAnalyzerEnabled(boolean swiftPackageManagerAnalyzerEnabled) {
        this.swiftPackageManagerAnalyzerEnabled = swiftPackageManagerAnalyzerEnabled;
    }

    /**
     * Returns if the Archive analyzer is enabled or not.
     */
    public boolean isArchiveAnalyzerEnabled() {
        return archiveAnalyzerEnabled;
    }

    /**
     * Sets if the Archive analyzer is enabled or not.
     */
    public void setArchiveAnalyzerEnabled(boolean archiveAnalyzerEnabled) {
        this.archiveAnalyzerEnabled = archiveAnalyzerEnabled;
    }

    /**
     * Returns if the Assembly analyzer is enabled or not.
     */
    public boolean isAssemblyAnalyzerEnabled() {
        return assemblyAnalyzerEnabled;
    }

    /**
     * Sets if the Assembly analyzer is enabled or not.
     */
    public void setAssemblyAnalyzerEnabled(boolean assemblyAnalyzerEnabled) {
        this.assemblyAnalyzerEnabled = assemblyAnalyzerEnabled;
    }

    /**
     * Returns if the NuSpec analyzer is enabled or not.
     */
    public boolean isNuspecAnalyzerEnabled() {
        return nuspecAnalyzerEnabled;
    }

    /**
     * Sets if the NuSpec analyzer is enabled or not.
     */
    public void setNuspecAnalyzerEnabled(boolean nuspecAnalyzerEnabled) {
        this.nuspecAnalyzerEnabled = nuspecAnalyzerEnabled;
    }

    /**
     * Returns if the Nexus analyzer is enabled or not.
     */
    public boolean isNexusAnalyzerEnabled() {
        return nexusAnalyzerEnabled;
    }

    /**
     * Sets if the Nexus analyzer is enabled or not.
     */
    public void setNexusAnalyzerEnabled(boolean nexusAnalyzerEnabled) {
        this.nexusAnalyzerEnabled = nexusAnalyzerEnabled;
    }

    /**
     * Returns if the autoconf analyzer is enabled or not.
     */
    public boolean isAutoconfAnalyzerEnabled() {
        return autoconfAnalyzerEnabled;
    }

    /**
     * Sets if the autoconf analyzer is enabled or not.
     */
    public void setAutoconfAnalyzerEnabled(boolean autoconfAnalyzerEnabled) {
        this.autoconfAnalyzerEnabled = autoconfAnalyzerEnabled;
    }

    /**
     * Returns if the cmake analyzer is enabled or not.
     */
    public boolean isCmakeAnalyzerEnabled() {
        return cmakeAnalyzerEnabled;
    }

    /**
     * Sets if the cmake analyzer is enabled or not.
     */
    public void setCmakeAnalyzerEnabled(boolean cmakeAnalyzerEnabled) {
        this.cmakeAnalyzerEnabled = cmakeAnalyzerEnabled;
    }

    /**
     * Returns if the OpenSSL analyzer is enabled or not.
     */
    public boolean isOpensslAnalyzerEnabled() {
        return opensslAnalyzerEnabled;
    }

    /**
     * Sets if the OpenSSL analyzer is enabled or not.
     */
    public void setOpensslAnalyzerEnabled(boolean opensslAnalyzerEnabled) {
        this.opensslAnalyzerEnabled = opensslAnalyzerEnabled;
    }

    /**
     * Returns the non-default Nexus URL to use.
     */
    public URL getNexusUrl() {
        return nexusUrl;
    }

    /**
     * Specifies the non-default Nexus URL to use.
     */
    public void setNexusUrl(URL nexusUrl) {
        this.nexusUrl = nexusUrl;
    }

    /**
     * Returns if the Nexus analyzer should bypass any proxy defined in Jenkins.
     */
    public boolean isNexusProxyBypassed() {
        return nexusProxyBypass;
    }

    /**
     * Specifies if the Nexus analyzer should bypass any proxy defined in Jenkins.
     */
    public void setNexusProxyBypassed(boolean nexusProxyBypass) {
        this.nexusProxyBypass = nexusProxyBypass;
    }

    /**
     * Returns if the Maven Central analyzer is enabled or not.
     */
    public boolean isCentralAnalyzerEnabled() {
        return centralAnalyzerEnabled;
    }

    /**
     * Sets if the Maven Central analyzer is enabled or not.
     */
    public void setCentralAnalyzerEnabled(boolean centralAnalyzerEnabled) {
        this.centralAnalyzerEnabled = centralAnalyzerEnabled;
    }

    /**
     * Returns the non-default Maven Central URL to use.
     */
    public URL getCentralUrl() {
        return centralUrl;
    }

    /**
     * Specifies the non-default Maven Central URL to use.
     */
    public void setCentralUrl(URL centralUrl) {
        this.centralUrl = centralUrl;
    }

    /**
     * Returns the full path and filename to the Mono binary.
     */
    public String getMonoPath() {
        return monoPath;
    }

    /**
     * Specifies the full path and filename to the mono binary.
     */
    public void setMonoPath(String monoPath) {
        this.monoPath = monoPath;
    }

    /**
     * Returns the full path and filename to the bundle-audit binary.
     */
    public String getBundleAuditPath() {
        return bundleAuditPath;
    }

    /**
     * Specifies the full path and filename to the bundle-audit binary.
     */
    public void setBundleAuditPath(String bundleAuditPath) {
        this.bundleAuditPath = bundleAuditPath;
    }

    /**
     * Returns the full path of the temporary directory.
     */
    public String getTempPath() {
        return tempPath;
    }

    /**
     * Specifies the full path of the temporary directory.
     */
    public void setTempPath(String tempPath) {
        this.tempPath = tempPath;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        if (name == null) {
            sb.append(" -name = ").append("ERROR - NAME NOT SPECIFIED OR INVALID.\n");
        } else {
            sb.append(" -name = ").append(name).append("\n");
        }
        if (!updateOnly && (scanPath == null || scanPath.size() == 0)) {
            sb.append(" -scanPath = ").append("ERROR - PATH NOT SPECIFIED OR INVALID.\n");
        } else {
            for (String filePath: scanPath) {
                sb.append(" -scanPath = ").append(filePath).append("\n");
            }
        }
        if (outputDirectory == null) {
            sb.append(" -outputDirectory = ").append("ERROR - OUTPUT DIRECTORY NOT SPECIFIED OR INVALID.\n");
        } else {
            sb.append(" -outputDirectory = ").append(outputDirectory).append("\n");
        }
        if (dataDirectory == null) {
            sb.append(" -dataDirectory = ").append("ERROR - DATA DIRECTORY NOT SPECIFIED OR INVALID.\n");
        } else {
            sb.append(" -dataDirectory = ").append(dataDirectory).append("\n");
        }
        if (dbconnstr != null) {
            sb.append(" -connectionString = ").append(dbconnstr).append("\n");
        }
        if (dbdriver != null) {
            sb.append(" -dbDriverName = ").append(dbdriver).append("\n");
        }
        if (dbpath != null) {
            sb.append(" -dbDriverPath = ").append(dbpath).append("\n");
        }
        if (dbuser != null) {
            sb.append(" -dbUser = ").append(dbuser).append("\n");
        }
        if (dbpassword != null) {
            /*
             * It is likely that the global configuration, set by the
             * Jenkins administrator, includes a password that should
             * not be exposed to any user able to read the console
             * log from any job using this plugin.
             */
            sb.append(" -dbPassword = ").append("OBSCURED").append("\n");
        }
        if (suppressionFile != null) {
            sb.append(" -suppressionFile = ").append(suppressionFile).append("\n");
        }
        if (hintsFile != null) {
            sb.append(" -hintsFile = ").append(hintsFile).append("\n");
        }
        if (zipExtensions != null) {
            sb.append(" -zipExtensions = ").append(zipExtensions).append("\n");
        }

        sb.append(" -dataMirroringType = ").append(dataMirroringType == 0 ? "none" : "NIST CPE/CVE").append("\n");
        if (dataMirroringType != 0) {
            if (cveUrl12Modified == null) {
                sb.append(" -cveUrl12Modified = ").append("ERROR - CVE 1.2 MODIFIED URL NOT SPECIFIED OR INVALID.\n");
            } else {
                sb.append(" -cveUrl12Modified = ").append(cveUrl12Modified.toExternalForm()).append("\n");
            }
            if (cveUrl20Modified == null) {
                sb.append(" -cveUrl20Modified = ").append("ERROR - CVE 2.0 MODIFIED URL NOT SPECIFIED OR INVALID.\n");
            } else {
                sb.append(" -cveUrl20Modified = ").append(cveUrl20Modified.toExternalForm()).append("\n");
            }
            if (cveUrl12Base == null) {
                sb.append(" -cveUrl12Base = ").append("ERROR - CVE 1.2 BASE URL NOT SPECIFIED OR INVALID.\n");
            } else {
                sb.append(" -cveUrl12Base = ").append(cveUrl12Base.toExternalForm()).append("\n");
            }
            if (cveUrl20Base == null) {
                sb.append(" -cveUrl20Base = ").append("ERROR - CVE 2.0 BASE URL NOT SPECIFIED OR INVALID.\n");
            } else {
                sb.append(" -cveUrl20Base = ").append(cveUrl20Base.toExternalForm()).append("\n");
            }
        }

        if (proxyServer != null) {
            sb.append(" -proxyServer = ").append(proxyServer).append("\n");
            sb.append(" -proxyPort = ").append(proxyPort).append("\n");
        }
        if (proxyUsername != null) {
            sb.append(" -proxyUsername = ").append(proxyUsername).append("\n");
        }
        if (proxyPassword != null) {
            sb.append(" -proxyPassword = ").append("********").append("\n");
        }

        sb.append(" -isQuickQueryTimestampEnabled = ").append(isQuickQueryTimestampEnabled).append("\n");

        sb.append(" -jarAnalyzerEnabled = ").append(jarAnalyzerEnabled).append("\n");
        sb.append(" -nodeJsAnalyzerEnabled = ").append(nodeJsAnalyzerEnabled).append("\n");
        sb.append(" -nspAnalyzerEnabled = ").append(nspAnalyzerEnabled).append("\n");
        sb.append(" -composerLockAnalyzerEnabled = ").append(composerLockAnalyzerEnabled).append("\n");
        sb.append(" -pythonDistributionAnalyzerEnabled = ").append(pythonDistributionAnalyzerEnabled).append("\n");
        sb.append(" -pythonPackageAnalyzerEnabled = ").append(pythonPackageAnalyzerEnabled).append("\n");
        sb.append(" -rubyBundlerAuditAnalyzerEnabled = ").append(rubyBundlerAuditAnalyzerEnabled).append("\n");
        sb.append(" -rubyGemAnalyzerEnabled = ").append(rubyGemAnalyzerEnabled).append("\n");
        sb.append(" -cocoaPodsAnalyzerEnabled = ").append(cocoaPodsAnalyzerEnabled).append("\n");
        sb.append(" -swiftPackageManagerAnalyzerEnabled = ").append(swiftPackageManagerAnalyzerEnabled).append("\n");
        sb.append(" -archiveAnalyzerEnabled = ").append(archiveAnalyzerEnabled).append("\n");
        sb.append(" -assemblyAnalyzerEnabled = ").append(assemblyAnalyzerEnabled).append("\n");
        sb.append(" -centralAnalyzerEnabled = ").append(centralAnalyzerEnabled).append("\n");
        sb.append(" -nuspecAnalyzerEnabled = ").append(nuspecAnalyzerEnabled).append("\n");
        sb.append(" -nexusAnalyzerEnabled = ").append(nexusAnalyzerEnabled).append("\n");
        sb.append(" -autoconfAnalyzerEnabled = ").append(autoconfAnalyzerEnabled).append("\n");
        sb.append(" -cmakeAnalyzerEnabled = ").append(cmakeAnalyzerEnabled).append("\n");
        sb.append(" -opensslAnalyzerEnabled = ").append(opensslAnalyzerEnabled).append("\n");
        if (nexusAnalyzerEnabled && nexusUrl != null) {
            sb.append(" -nexusUrl = ").append(nexusUrl.toExternalForm()).append("\n");
        }
        if (nexusAnalyzerEnabled) {
            sb.append(" -nexusProxyBypassed = ").append(nexusProxyBypass).append("\n");
        }
        if (monoPath != null) {
            sb.append(" -monoPath = ").append(monoPath).append("\n");
        }
        if (bundleAuditPath != null) {
            sb.append(" -bundleAuditPath = ").append(bundleAuditPath).append("\n");
        }
        if (tempPath != null) {
            sb.append(" -tempPath = ").append(tempPath).append("\n");
        }
        sb.append(" -showEvidence = ").append(showEvidence).append("\n");

        sb.append(" -formats = ");
        for (ReportGenerator.Format format: formats) {
            sb.append(format.name()).append(" ");
        }
        sb.append("\n");

        sb.append(" -autoUpdate = ").append(autoUpdate).append("\n");
        sb.append(" -updateOnly = ").append(updateOnly);
        return sb.toString();
    }

}
