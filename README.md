[![Build Status](https://jenkins.ci.cloudbees.com/buildStatus/icon?job=plugins/dependency-check-plugin)](https://jenkins.ci.cloudbees.com/job/plugins/job/dependency-check-plugin/)

Dependency-Check Jenkins Plugin
==============================

Dependency-Check is a utility that identifies project dependencies and checks if there are any known, publicly disclosed, vulnerabilities. This tool can be part of the solution to the OWASP Top 10 2013: A9 - Using Components with Known Vulnerabilities. This plug-in can independently execute a Dependency-Check analysis and visualize results.

Dependency-Check is able to identify Java and Python components along with .NET assemblies. Once identified, Dependency-Check will automatically determine if those component have known, publicly disclosed, vulnerabilities.

The Dependency-Check Jenkins Plugin features the ability to perform a dependency analysis build and later view results post build. The plugin is built using [analysis-core] and features many of the same features that Jenkins static analysis plugins offer, including thresholds, charts and the ability to view vulnerability information should a dependency have one identified.

More information can be found on the [wiki].

Mailing List
------------

Subscribe: [dependency-check+subscribe@googlegroups.com] [subscribe]

Post: [dependency-check@googlegroups.com] [post]

Copyright & License
-------------------

Dependency-Check is Copyright (c) 2012-2015 Jeremy Long. All Rights Reserved.

Dependency-Check Jenkins Plugin is Copyright (c) 2013-2015 Steve Springett. All Rights Reserved.

Permission to modify and redistribute is granted under the terms of the Apache 2.0 license. See the [LICENSE.txt] [license] file for the full license.

Dependency-Check makes use of several other open source libraries. Please see the [NOTICE.txt] [notices] file for more information.


  [wiki]: https://wiki.jenkins-ci.org/display/JENKINS/OWASP+Dependency-Check+Plugin
  [analysis-core]: http://wiki.jenkins-ci.org/x/CwDgAQ
  [subscribe]: mailto:dependency-check+subscribe@googlegroups.com
  [post]: mailto:dependency-check@googlegroups.com
  [license]: https://github.com/jenkinsci/dependency-check-plugin/blob/master/LICENSE.txt
  [notices]: https://github.com/jenkinsci/dependency-check-plugin/blob/master/NOTICES.txt
