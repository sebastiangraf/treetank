#!/bin/sh

/treetank/jre/bin/jamvm\
    -Djava.library.path=\
/treetank/service\
    -classpath \
/treetank/service/backport-util-concurrent.jar:\
/treetank/service/treetank.jar:\
    org.treetank.service.TestTreeTankService
