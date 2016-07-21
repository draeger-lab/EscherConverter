# EscherConverter

EscherConverter is a standalone program that reads files created with the graphical network editor [Escher](http://escher.github.io) and converts them to files in community standard formats.

## Using EscherConverter

Escher uses a JSON file format to represent its networks. This format has been developed because of its very small file size and its compatibility to online programs that are written in JavaScript. In particular, JSON is a JavaScript Object Notation, or in other words, a JSON file directly represents components of JavaScript programs. This makes parsing very simple and allows direct use of those files in web-based programs.

However, in systems biology, specific file formats have been developed with the aim to be easily exchangeable between software implemented in diverse programming languages.

To this end, these formats support semantically clear annotations and are maintained by a large community of scientists. EscherConverter supports export to two particularly important XML-based community file formats SBML with layout extension and SBGN-ML.

While SBML has been mainly developed for dynamic simulation of biological networks, it is nowadays also usable for diverse other purposes. Its layout extension facilitates the encoding the display of biological networks.

SBGN-ML has been directly developed as a language for the display of biological pathway maps of diverse kinds. It stores the position and connection of entities, similar to what is shown in Escher networks.

EscherConverter takes Escher’s JSON files as input and generates equivalent SBML Level 3 Version 1 files with layout extension or SBGN-ML files.

In order to ensure that the conversion is correct, EscherConverter provides its own display that gives users a preview of how the export data format will be rendered by other tools. In this preview display, you can zoom in and out, move arcs and node positions. However, it is important to know that none of the changes made in this preview are stored in the export file.

## Download and Installation

You can obtain local copy of EscherConverter by clicking [here](https://github.com/SBRG/EscherConverter/releases/latest/).

As a Java™ application, no specific installation is required for EscherConverter.

However, make sure you have a recent Java™ Runtime Environment (JRE) installed on your computer (at least JRE 8). You can obtain Java™ from the Oracle website. There you can also find installation instructions for your respective operating system.

Once Java™ has been installed, you can simply place the EscherConverter JAR file somewhere on your local computer, for instance in the folder
 * `/Applications/` if you are working under Mac OS
 * `/opt/` for Linux computers
 * `C:\Program Files\` if you are using Windows
 
## Launching the program

You can launch EscherConverter simply by double-clicking on the application JAR file. This will open a following graphical user interface.

## Included third-party software

EscherConverter includes several third-party libraries, which we here list and acknowledge:
 * ArgParser
 * JSBML
 * libSBGN
 * Pixel-Mixer icons
 * yFiles (only in the first version)
 
## Command-line interface API

You can launch EscherConverter from the command-line. On a Unix system (such as Linux, MacOS, or Solaris, etc.), use a command like:
```
bash$ java -jar -Xms8G -Xmx8G -Duser.language=en ./EscherConverter-0.5.jar [options]
```
Under Window, use a command like:
```
C:\> javaw -jar -Xms8G -Xmx8G -Duser.language=en EscherConverter-0.5.jar [options]
```

Escher has a large collection of command-line options, which can be useful if you want to launch the program with specific settings or if multiple files are to be converted in a batch mode. It is even possible to completely disable the graphical user interface. When launching EscherConverter with the option `--help` or `-?` you will receive more information about possible options. In the graphical user interface, you can find the full list of command-line options in the online Help menu.
