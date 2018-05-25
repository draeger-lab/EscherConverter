# EscherConverter<sup>1.2</sup> 
<img align="right" src="src/main/resources/edu/ucsd/sbrg/escher/gui/escher-logo_64.png"/>

**A standalone program that reads files created with the graphical network editor [Escher](http://escher.github.io) and converts them to files in community standard formats.**

[![License (MIT)](https://img.shields.io/badge/license-MIT-blue.svg?style=plastic)](http://opensource.org/licenses/MIT)
[![Latest version](https://img.shields.io/badge/Latest_version-1.2-brightgreen.svg?style=plastic)](https://github.com/draeger-lab/EscherConverter/releases/)
[![DOI](http://img.shields.io/badge/DOI-10.1371%20%2F%20journal.pcbi.1004321-blue.svg?style=plastic)](http://dx.doi.org/10.1371/journal.pcbi.1004321)
[![Build Status](https://travis-ci.org/draeger-lab/EscherConverter.svg?branch=master&style=plastic)](https://travis-ci.org/draeger-lab/EscherConverter/)

*Authors:* [Andreas Dräger](https://github.com/draeger), [Devesh Khandelwal](https://github.com/devkhan)
___________________________________________________________________________________________________________

## Quick-start

#### OR

Get a local copy of EscherConverter from [here](https://github.com/SBRG/EscherConverter/releases/latest). As a Java™ application, the only thing needed is a valid Java(JRE) 8+ installation present. After that you can either open the JAr by double-clicking it or use it from the command line as below.

On Unix-like system (Ubuntu, Fedora, macOS, etc.):
```
bash$ java -jar -Xms8G -Xmx8G -Duser.language=en ./EscherConverter.jar --help
```
On Windows:
```
C:\> javaw -jar -Xms8G -Xmx8G -Duser.language=en EscherConverter-0.5 --help
```

### Need to know more?

Head over to the wiki.

## Included third-party software

EscherConverter includes several third-party libraries, which we here list and acknowledge:

* ArgParser
* JSBML
* libSBGN
* Pixel-Mixer icons
* yFiles (obfuscated)
* Randelshofer's macOS filechooser
* Jackson
