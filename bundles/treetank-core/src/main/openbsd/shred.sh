#!/bin/sh

/treetank/jre/bin/jamvm -Xms80M -Xmx80M\
    -Djava.library.path=\
/treetank/service\
    -classpath \
/treetank/service/wstx-asl.jar:\
/treetank/service/stax-api.jar:\
/treetank/service/treetank.jar:\
    com.treetank.service.xml.XMLShredder $1 $2
