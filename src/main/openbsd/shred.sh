#!/bin/sh

/treetank/jre/bin/jamvm\
    -Djava.library.path=\
/treetank/service\
    -classpath \
/treetank/service/wstx-asl.jar:\
/treetank/service/stax-api.jar:\
/treetank/service/backport-util-concurrent.jar:\
/treetank/service/treetank.jar:\
    org.treetank.xmllayer.XMLShredder $1 $2
