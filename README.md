DependencyCheck Jenkins Plugin
==============================

DependencyCheck is a utility that attempts to detect publicly disclosed vulnerabilities contained within project dependencies. It does this by determining if there is a Common Platform Enumeration (CPE) identifier for a given dependency. If found, it will generate a report linking to the associated CVE entries.

The DependencyCheck Jenkins Plugin features the ability to perform a dependency analysis build and later view results post build. The plugin is built using [analysis-core] and features many of the same features that Jenkins static analysis plugins offer, including thresholds, charts and the ability to view vulnerability information should a dependency have one identified.

More information can be found on the [wiki].

Mailing List
------------

Subscribe: [dependency-check+subscribe@googlegroups.com] [subscribe]

Post: [dependency-check@googlegroups.com] [post]

Copyright & License
-------------------

Dependency-Check is Copyright (c) 2012-2013 Jeremy Long. All Rights Reserved.

Dependency-Check Jenkins Plugin is Copyright (c) 2013 Steve Springett. All Rights Reserved.

Permission to modify and redistribute is granted under the terms of the GPLv3 license. See the [LICENSE.txt] [GPLv3] file for the full license.

Dependency-Check makes use of several other open source libraries. Please see the [NOTICE.txt] [notices] file for more information.


  [wiki]: https://github.com/jeremylong/dependency-check-jenkins/wiki
  [analysis-core]: http://wiki.jenkins-ci.org/x/CwDgAQ
  [subscribe]: mailto:dependency-check+subscribe@googlegroups.com
  [post]: mailto:dependency-check@googlegroups.com
  [GPLv3]: https://github.com/jeremylong/dependency-check-jenkins/blob/master/LICENSE.txt
  [notices]: https://github.com/jeremylong/dependency-check-jenkins/blob/master/NOTICES.txt
