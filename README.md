[![Build Status](https://ci.jenkins.io/buildStatus/icon?job=Plugins/dependency-check-plugin/master)](https://ci.jenkins.io/job/Plugins/job/dependency-check-plugin)
[![License][license-image]][license-url]
[![Plugin Version](https://img.shields.io/jenkins/plugin/v/dependency-check-jenkins-plugin.svg)](https://plugins.jenkins.io/dependency-check-jenkins-plugin)
[![Jenkins Plugin Installs](https://img.shields.io/jenkins/plugin/i/dependency-check-jenkins-plugin.svg?color=blue)](https://plugins.jenkins.io/dependency-check-jenkins-plugin)
[![JIRA](https://img.shields.io/badge/issue_tracker-JIRA-red.svg)](https://issues.jenkins-ci.org/issues/?jql=component%20%3D%20dependency-check-jenkins-plugin)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/d8d32c08cb1f401ba0f950daca885901)](https://www.codacy.com/app/stevespringett/dependency-check-plugin?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=jenkinsci/dependency-check-plugin&amp;utm_campaign=Badge_Grade)


# Dependency-Check Jenkins Plugin
Dependency-Check is a utility that identifies project dependencies and checks if there are any known, publicly 
disclosed, vulnerabilities. This tool can be part of the solution to the OWASP Top 10 2017: A9 - Using 
Components with Known Vulnerabilities. This plug-in can independently execute a Dependency-Check analysis and 
visualize results.

## Usage
The plugin has three main components: a globally defined tool configuration, a builder, and a publisher.

#### Global Tool Configuration
One or more Dependency-Check versions can be installed via the Jenkins Global Tool Configuration. The installation of Dependency-Check can be performed automatically, which will download and extract the official Command-Line Interface (CLI) from Bintray, or an official distribution can be installed manually and the path to the installation referenced in the configuration.

![global tool configuration](https://raw.githubusercontent.com/jenkinsci/dependency-check-plugin/master/docs/images/global-tool-configuration.png)

#### Builder
The builder performs an analysis using one of the pre-defined Dependency-Check CLI installations. Configuration specific to Jenkins is minimal, with important aspects of the job configuration being the 'Arguments' field, which is sent directly to the CLI installation defined. 

![builder configuration](https://raw.githubusercontent.com/jenkinsci/dependency-check-plugin/master/docs/images/builder-config.png)

#### Publisher
The publisher works independently of the tool configuration or builder and is responsible for reading dependency-check-report.xml and generating metrics, trends, findings, and optionally failing the build or putting it into a warning state based on configurable thresholds. 

![publisher configuration](https://raw.githubusercontent.com/jenkinsci/dependency-check-plugin/master/docs/images/publisher-config.png)

<p><br></p>

When a job has the publisher configured, a trending chart will display the total number of findings grouped by severity.

<p><br></p>

![publisher trend](https://raw.githubusercontent.com/jenkinsci/dependency-check-plugin/master/docs/images/publisher-trend.png)

<p><br></p>

The chart is interactive. Hovering over a build will display high-level severity information.

<p><br></p>

![publisher trend hover](https://raw.githubusercontent.com/jenkinsci/dependency-check-plugin/master/docs/images/publisher-trend-hover.png)

<p><br></p>

Per-build results may be viewed. Findings are displayed in an interactive table which can be sorted, searched on, and paginated through. Each findings can be expanded to reveal additional details.

<p><br></p>

![publisher results](https://raw.githubusercontent.com/jenkinsci/dependency-check-plugin/master/docs/images/publisher-results-expanded.png)


## Mailing List
Subscribe: [dependency-check+subscribe@googlegroups.com] [subscribe]

Post: [dependency-check@googlegroups.com] [post]

## Copyright & License
Dependency-Check is Copyright (c) Jeremy Long. All Rights Reserved.

Dependency-Check Jenkins Plugin is Copyright (c) Steve Springett. All Rights Reserved.

Permission to modify and redistribute is granted under the terms of the Apache 2.0 license. See the [LICENSE.txt] [license] file for the full license.

  [subscribe]: mailto:dependency-check+subscribe@googlegroups.com
  [post]: mailto:dependency-check@googlegroups.com
  [license]: https://github.com/jenkinsci/dependency-check-plugin/blob/master/LICENSE.txt
  [notices]: https://github.com/jenkinsci/dependency-check-plugin/blob/master/NOTICES.txt
  [license-image]: https://img.shields.io/badge/license-apache%20v2-brightgreen.svg
  [license-url]: https://github.com/jenkinsci/dependency-check-plugin/blob/master/LICENSE.txt
