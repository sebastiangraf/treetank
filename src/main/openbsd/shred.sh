#!/bin/sh

/treetank/jre/bin/jamvm -Xms128M -Xmx160M\
    -Djava.library.path=\
/treetank/service\
    -classpath \
/treetank/service/wstx-asl.jar:\
/treetank/service/stax-api.jar:\
/treetank/service/treetank.jar:\
    org.treetank.xmllayer.XMLShredder $1 $2
