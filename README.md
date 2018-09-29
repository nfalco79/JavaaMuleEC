# JavaaMuleEC

[![Build Status](https://travis-ci.org/nfalco79/JavaaMuleEC.svg?branch=master)](https://travis-ci.org/nfalco79/JavaaMuleEC) [![Coverage Status](https://coveralls.io/repos/github/nfalco79/JavaaMuleEC/badge.svg?branch=master)](https://coveralls.io/github/nfalco79/JavaaMuleEC?branch=master)

Java library implementing aMule EC protocol.

This library was basically created for the development of the [Amule Remote](https://play.google.com/store/apps/details?id=com.iukonline.amule.android.amuleremote) Android app. However, it can be used within any Java application.

Project is poorly documented (if documented at all), but I have no time at the moment to set up a proper documentation. Feel free to reach out to iuk@iukonline.com for any clarification.

## Maven

Released versions are available in The Central Repository.
Just add this artifact to your project:

```xml
<dependency>
    <groupId>com.github.nfalco79</groupId>
    <artifactId>amule-ec</artifactId>
    <version>{version}</version>
</dependency>
```

However if you want to use the last snapshot version, you have to add the Nexus OSS repository:

```xml
<repository>
    <id>osshr</id>
    <name>Nexus OSS repository for snapshots</name>
    <url>https://oss.sonatype.org/content/repositories/snapshots</url>
    <snapshots>
        <enabled>true</enabled>
    </snapshots>
</repository>
```

## License ##

This project is licensed under [GPLv3 license](https://spdx.org/licenses/GPL-3.0-or-later).