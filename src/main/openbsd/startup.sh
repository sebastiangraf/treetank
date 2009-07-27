#!/bin/sh

while [ true ]
do
/treetank/jre/bin/jamvm -Xms80M -Xmx80M\
    -Djava.library.path=\
/treetank/service\
    -classpath \
/treetank/service/wstx-asl.jar:\
/treetank/service/stax-api.jar:\
/treetank/service/jetty-util.jar:\
/treetank/service/jetty.jar:\
/treetank/service/servlet-api.jar:\
/treetank/service/treetank.jar:\
    com.treetank.service.rest.TreeTankService
done
