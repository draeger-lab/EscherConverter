# EscherConverter<sup>1.0</sup> [![Build Status](https://travis-ci.org/SBRG/EscherConverter.svg?branch=master)](https://travis-ci.org/SBRG/EscherConverter)

EscherConverter is a standalone program that reads files created with the graphical network editor [Escher](http://escher.github.io) and converts them to files in community standard formats.

## Quick-start

**Try out the live web version [here](http://139.59.18.143/).**

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
* yFiles (only in the first version)
* Jackson
 
##### Author: Andreas Dräger (@draeger)
##### Maintainer: Devesh Khandelwal (@devkhan)
