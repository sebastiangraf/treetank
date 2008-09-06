#!/bin/sh

/treetank/jre/bin/jamvm -Xms128M -Xmx160M\
    -Djava.library.path=\
/treetank/service\
    -classpath \
/treetank/service/treetank.jar:\
    org.treetank.service.TestTreeTankService
