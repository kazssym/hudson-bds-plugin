# READ ME

This directory contains the source code for [RAD Studio Plugin][] for
[Jenkins][] which allows users to build [RAD Studio][] projects or project
groups.

Although RAD Studio projects can be built with more famous [MSBuild Plugin][],
users must set version-specific environment variables by hand or by calling
a batch file in each Jenkins project.
This plugin will help users build (and test) their RAD Studio projects or
project groups without such tricks.

[RAD Studio Plugin]: <https://wiki.jenkins-ci.org/display/JENKINS/RAD+Studio+Plugin>
[Jenkins]: <http://jenkins-ci.org/>
[RAD Studio]: <http://www.embarcadero.com/products/rad-studio>
[MSBuild Plugin]: <https://wiki.jenkins-ci.org/display/JENKINS/MSBuild+Plugin>

## License

This program is *[free software][]*: you can redistribute it and/or modify it
under the terms of the [GNU Affero General Public License][] as published by
the [Free Software Foundation][], either version 3 of the License, or (at your
option) any later version.

You should be able to receive a copy of the GNU Affero General Public License
along with this program.

[free software]: <http://www.gnu.org/philosophy/free-sw.html>
                 "What is free software?"
[GNU Affero General Public License]: <http://www.gnu.org/licenses/agpl.html>
[Free Software Foundation]: <http://www.fsf.org/>

## Installation

You should be able to generate a HPI file from the source code as a [Maven][]
project if you would like to do it yourself.

[Maven]: <http://maven.apache.org/>
