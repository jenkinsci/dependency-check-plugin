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

import java.io.Serializable;
import java.net.URL;

/**
 * A container object that holds all of the configurable options to be used by
 * a DependencyCheck analysis.
 *
 * @author Steve Springett (steve.springett@owasp.org)
 */
@SuppressWarnings("unused")
public class GlobalOptions implements Serializable {

    private static final long serialVersionUID = 4571161829818421072L;

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
     * Specifies the data mirroring type (scheme) to use
     */
    private int dataMirroringType;

    /**
     * Specifies the CVE JSON modified URL
     */
    private URL cveJsonUrlModified;

    /**
     * Specifies the CVE JSON base URL
     */
    private URL cveJsonUrlBase;

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
     * Specifies the exclusions lists for the proxy
     */
    private String nonProxyHosts;

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
     * Specifies if the Node Package analyzer is enabled
     */
    private boolean nodePackageAnalyzerEnabled;

    /**
     * Specifies if the Node Audit analyzer is enabled
     */
    private boolean nodeAuditAnalyzerEnabled;

    /**
     * Specifies if the RetireJS analyzer is enabled
     */
    private boolean retireJsAnalyzerEnabled;

    /**
     * Specifies the URL to the Javascript feed for Retire.js
     */
    private URL retireJsRepoJsUrl;

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
     * Specifies if the MS Build Project analyzer is enabled
     */
    private boolean msBuildProjectAnalyzerEnabled;

    /**
     * Specifies if the NuGet Config analyzer is enabled
     */
    private boolean nuGetConfigAnalyzerEnabled;

    /**
     * Specifies if the Artifactory analyzer is enabled
     */
    private boolean artifactoryAnalyzerEnabled;

    /**
     * Specifies the Artifactory URL to use if enabled
     */
    private URL artifactoryUrl;

    /**
     * Specifies if the Artifactory analyzer should bypass any proxy defined in Jenkins
     */
    private boolean artifactoryProxyBypassed;

    /**
     * Specifies the Artifactory API token
     */
    private String artifactoryApiToken;

    /**
     * Specifies the Artifactory API username
     */
    private String artifactoryApiUsername;

    /**
     * Specifies the Artifactory Bearer token
     */
    private String artifactoryBearerToken;

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
     * Returns the data mirroring type (scheme) to use where:
     * -1 = all
     * 0 = none
     * 1 = NIST CPE/CVE
     * 2 = Retire.js
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
     * Returns the CVE JSON modified URL.
     */
    public URL getCveJsonUrlModified() {
        return cveJsonUrlModified;
    }

    /**
     * Sets the CVE JSON modified URL.
     */
    public void setCveJsonUrlModified(URL url) {
        this.cveJsonUrlModified = url;
    }

    /**
     * Returns the CVE JSON base URL.
     */
    public URL getCveJsonUrlBase() {
        return cveJsonUrlBase;
    }

    /**
     * Sets the CVE JSON base URL.
     */
    public void setCveJsonUrlBase(URL url) {
        this.cveJsonUrlBase = url;
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
     * Returns the proxy exclusions.
     */
    public String getNonProxyHosts() {
        return nonProxyHosts;
    }

    /**
     * Sets the proxy exclusions.
     */
    public void setNonProxyHosts(String nonProxyHosts) {
        this.nonProxyHosts = nonProxyHosts;
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
     * Returns if the Node Package analyzer is enabled or not.
     */
    public boolean isNodePackageAnalyzerEnabled() {
        return nodePackageAnalyzerEnabled;
    }

    /**
     * Sets if the Node Package analyzer is enabled or not.
     */
    public void setNodePackageAnalyzerEnabled(boolean nodePackageAnalyzerEnabled) {
        this.nodePackageAnalyzerEnabled = nodePackageAnalyzerEnabled;
    }

    /**
     * Returns if the Node Audit analyzer is enabled or not.
     */
    public boolean isNodeAuditAnalyzerEnabled() {
        return nodeAuditAnalyzerEnabled;
    }

    /**
     * Sets if the Node Audit analyzer is enabled or not.
     */
    public void setNodeAuditAnalyzerEnabled(boolean nodeAuditAnalyzerEnabled) {
        this.nodeAuditAnalyzerEnabled = nodeAuditAnalyzerEnabled;
    }

    /**
     * Returns if the RetireJS analyzer is enabled or not.
     */
    public boolean isRetireJsAnalyzerEnabled() {
        return retireJsAnalyzerEnabled;
    }

    /**
     * Sets if the RetireJS analyzer is enabled or not.
     */
    public void setRetireJsAnalyzerEnabled(boolean retireJsAnalyzerEnabled) {
        this.retireJsAnalyzerEnabled = retireJsAnalyzerEnabled;
    }

    /**
     * Returns the URL to the Javascript feed for Retire.js.
     */
    public URL getRetireJsRepoJsUrl() {
        return retireJsRepoJsUrl;
    }

    /**
     * Sets the URL to the Javascript feed for Retire.js.
     */
    public void setRetireJsRepoJsUrl(URL retireJsRepoJsUrl) {
        this.retireJsRepoJsUrl = retireJsRepoJsUrl;
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
     * Returns if the MS Build Project analyzer is enabled or not.
     */
    public boolean isMsBuildProjectAnalyzerEnabled() {
        return msBuildProjectAnalyzerEnabled;
    }

    /**
     * Specifies if the MS Build Project analyzer is enabled or not.
     */
    public void setMsBuildProjectAnalyzerEnabled(boolean msBuildProjectAnalyzerEnabled) {
        this.msBuildProjectAnalyzerEnabled = msBuildProjectAnalyzerEnabled;
    }

    /**
     * Returns if the NuGet Config analyzer is enabled or not.
     */
    public boolean isNuGetConfigAnalyzerEnabled() {
        return nuGetConfigAnalyzerEnabled;
    }

    /**
     * Specifies if the NuGet Config analyzer is enabled or not.
     */
    public void setNuGetConfigAnalyzerEnabled(boolean nuGetConfigAnalyzerEnabled) {
        this.nuGetConfigAnalyzerEnabled = nuGetConfigAnalyzerEnabled;
    }

    /**
     * Returns if the Artifactor analyzer is enabled or not.
     */
    public boolean isArtifactoryAnalyzerEnabled() {
        return artifactoryAnalyzerEnabled;
    }

    /**
     * Specifies if the Artifactor analyzer is enabled or not.
     */
    public void setArtifactoryAnalyzerEnabled(boolean artifactoryAnalyzerEnabled) {
        this.artifactoryAnalyzerEnabled = artifactoryAnalyzerEnabled;
    }

    /**
     * Returns the Artifactory URL.
     */
    public URL getArtifactoryUrl() {
        return artifactoryUrl;
    }

    /**
     * Specifies the Artifactor URL.
     */
    public void setArtifactoryUrl(URL artifactoryUrl) {
        this.artifactoryUrl = artifactoryUrl;
    }

    /**
     * Returns if the Artifactory analyzer should bypass any proxy defined in Jenkins.
     */
    public boolean isArtifactoryProxyBypassed() {
        return artifactoryProxyBypassed;
    }

    /**
     * Specifies if the Artifactory analyzer should bypass any proxy defined in Jenkins.
     */
    public void setArtifactoryProxyBypassed(boolean artifactoryProxyBypassed) {
        this.artifactoryProxyBypassed = artifactoryProxyBypassed;
    }

    /**
     * Returns the Artifactor API token.
     */
    public String getArtifactoryApiToken() {
        return artifactoryApiToken;
    }

    /**
     * Specifies the Artifactor API token.
     */
    public void setArtifactoryApiToken(String artifactoryApiToken) {
        this.artifactoryApiToken = artifactoryApiToken;
    }

    /**
     * Returns the Artifactor API username.
     */
    public String getArtifactoryApiUsername() {
        return artifactoryApiUsername;
    }

    /**
     * Specifies the Artifactor API username.
     */
    public void setArtifactoryApiUsername(String artifactoryApiUsername) {
        this.artifactoryApiUsername = artifactoryApiUsername;
    }

    /**
     * Returns the Artifactor bearer token.
     */
    public String getArtifactoryBearerToken() {
        return artifactoryBearerToken;
    }

    /**
     * Specifies the Artifactor bearer token.
     */
    public void setArtifactoryBearerToken(String artifactoryBearerToken) {
        this.artifactoryBearerToken = artifactoryBearerToken;
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
        if (dataMirroringType == -1) {
            sb.append(" -dataMirroringType = all").append("\n");
        }
        if (dataMirroringType == 0) {
            sb.append(" -dataMirroringType = none").append("\n");
        }
        if (dataMirroringType == 1) {
            sb.append(" -dataMirroringType = NIST CPE/CVE").append("\n");
        }
        if (dataMirroringType == 2) {
            sb.append(" -dataMirroringType = Retire.js").append("\n");
        }
        if (dataMirroringType == -1 || dataMirroringType == 1) {
            if (cveJsonUrlModified == null) {
                sb.append(" -cveJsonUrlModified = ").append("ERROR - CVE JSON MODIFIED URL NOT SPECIFIED OR INVALID.\n");
            } else {
                sb.append(" -cveJsonUrlModified = ").append(cveJsonUrlModified.toExternalForm()).append("\n");
            }
            if (cveJsonUrlBase == null) {
                sb.append(" -cveJsonUrlBase = ").append("ERROR - CVE JSON BASE URL NOT SPECIFIED OR INVALID.\n");
            } else {
                sb.append(" -cveJsonUrlBase = ").append(cveJsonUrlBase.toExternalForm()).append("\n");
            }
        }
        if (dataMirroringType == -1 || dataMirroringType == 2) {
            if (retireJsRepoJsUrl == null) {
                sb.append(" -retireJsRepoJsUrl = ").append("ERROR - Retire.js Javascript URL NOT SPECIFIED OR INVALID.\n");
            } else {
                sb.append(" -retireJsRepoJsUrl = ").append(retireJsRepoJsUrl.toExternalForm()).append("\n");
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
        if (nonProxyHosts != null) {
            sb.append(" -nonProxyHosts = ").append(nonProxyHosts).append("\n");
        }

        sb.append(" -isQuickQueryTimestampEnabled = ").append(isQuickQueryTimestampEnabled).append("\n");

        sb.append(" -jarAnalyzerEnabled = ").append(jarAnalyzerEnabled).append("\n");
        sb.append(" -nodePackageAnalyzerEnabled = ").append(nodePackageAnalyzerEnabled).append("\n");
        sb.append(" -nodeAuditAnalyzerEnabled = ").append(nodeAuditAnalyzerEnabled).append("\n");
        sb.append(" -retireJsAnalyzerEnabled = ").append(retireJsAnalyzerEnabled).append("\n");
        sb.append(" -composerLockAnalyzerEnabled = ").append(composerLockAnalyzerEnabled).append("\n");
        sb.append(" -pythonDistributionAnalyzerEnabled = ").append(pythonDistributionAnalyzerEnabled).append("\n");
        sb.append(" -pythonPackageAnalyzerEnabled = ").append(pythonPackageAnalyzerEnabled).append("\n");
        sb.append(" -rubyBundlerAuditAnalyzerEnabled = ").append(rubyBundlerAuditAnalyzerEnabled).append("\n");
        sb.append(" -rubyGemAnalyzerEnabled = ").append(rubyGemAnalyzerEnabled).append("\n");
        sb.append(" -cocoaPodsAnalyzerEnabled = ").append(cocoaPodsAnalyzerEnabled).append("\n");
        sb.append(" -swiftPackageManagerAnalyzerEnabled = ").append(swiftPackageManagerAnalyzerEnabled).append("\n");
        sb.append(" -archiveAnalyzerEnabled = ").append(archiveAnalyzerEnabled).append("\n");
        sb.append(" -assemblyAnalyzerEnabled = ").append(assemblyAnalyzerEnabled).append("\n");
        sb.append(" -msBuildProjectAnalyzerEnabled = ").append(msBuildProjectAnalyzerEnabled).append("\n");
        sb.append(" -nuGetConfigAnalyzerEnabled = ").append(nuGetConfigAnalyzerEnabled).append("\n");
        sb.append(" -nuspecAnalyzerEnabled = ").append(nuspecAnalyzerEnabled).append("\n");
        sb.append(" -centralAnalyzerEnabled = ").append(centralAnalyzerEnabled).append("\n");
        sb.append(" -nexusAnalyzerEnabled = ").append(nexusAnalyzerEnabled).append("\n");
        sb.append(" -artifactoryAnalyzerEnabled = ").append(artifactoryAnalyzerEnabled).append("\n");
        sb.append(" -autoconfAnalyzerEnabled = ").append(autoconfAnalyzerEnabled).append("\n");
        sb.append(" -cmakeAnalyzerEnabled = ").append(cmakeAnalyzerEnabled).append("\n");
        sb.append(" -opensslAnalyzerEnabled = ").append(opensslAnalyzerEnabled).append("\n");
        if (nexusAnalyzerEnabled && nexusUrl != null) {
            sb.append(" -nexusUrl = ").append(nexusUrl.toExternalForm()).append("\n");
        }
        if (nexusAnalyzerEnabled) {
            sb.append(" -nexusProxyBypassed = ").append(nexusProxyBypass).append("\n");
        }
        if (artifactoryAnalyzerEnabled && artifactoryUrl != null) {
            sb.append(" -artifactoryUrl = ").append(artifactoryUrl.toExternalForm()).append("\n");
        }
        if (artifactoryProxyBypassed) {
            sb.append(" -artifactoryProxyBypassed = ").append(artifactoryProxyBypassed).append("\n");
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
        return sb.toString();
    }
}
