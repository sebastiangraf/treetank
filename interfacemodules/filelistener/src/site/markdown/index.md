# Treetank - File interface Module


This interface module for treetank adds functionality, making it possible to use it in a dropbox like manner, watching
different folders and synchronizing their data with treetank.

## How does it work?

A UI created using SWT is provided to easily add new folders to watch over. You can then choose different
backends like jclouds.

Following figure presents a small overview of how the data is mapped on to nodes in treetank.

![Mapping of files to treetank nodes](images/filelistener-mapping.png "Mapping of files to treetank nodes")

## Features currently implemented

* Multiple resources can be created with different backends
* A resource can be used for exactly one watched folder
* Restoring of the latest revision from treetank into an empty folder

## Upcoming features

* Restoring over different revisions
* Choose which files to restore
* Synchronize differences when the application was offline