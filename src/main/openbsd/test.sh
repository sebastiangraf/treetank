#!/bin/sh

/treetank/jre/bin/jamvm -Xms80M -Xmx80M\
    -Djava.library.path=\
/treetank/service\
    -classpath \
/treetank/service/treetank.jar:\
    org.treetank.service.TestTreeTankService
