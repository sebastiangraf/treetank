#!/bin/sh

/treetank/jre/bin/jamvm -Xms128M -Xmx160M\
    -Djava.library.path=\
/treetank/service\
    -classpath \
/treetank/service/backport-util-concurrent.jar:\
/treetank/service/treetank.jar:\
    org.treetank.service.TestTreeTankService
