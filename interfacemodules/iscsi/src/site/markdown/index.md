#Treetank - ISCSI interface Module


This interface module for treetank adds functionality, making it possible to use it as a storage
device for the jscsi target server.

## How does it work?

The target is called in the same way any iscsi target is called. But there is no physical, nor logical
storage device in the way of a hard disk or virtual image. The blocks a storage device normally uses to
save data on are now being abstracted on to the node representation in treetank.

Following figure presents a small overview of how the data is mapped on to nodes in treetank.

![Mapping of bytes to treetank nodes](images/iscsi-mapping.png "Mapping of bytes to treetank nodes")

## Features currently implemented

* For faster response times, requests are being cached for later submition into the storage.
* Usage of different configurations for treetank, such as
    * Using different backends
    * Compression and encryption of the data
* Supports all configuration possibilities of the common jscsi target.