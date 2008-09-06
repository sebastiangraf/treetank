#!/bin/sh

while [ true ]
do
/treetank/jre/bin/jamvm -Xms128M -Xmx160M\
    -Djava.library.path=\
/treetank/service\
    -classpath \
/treetank/service/wstx-asl.jar:\
/treetank/service/stax-api.jar:\
/treetank/service/jetty-util.jar:\
/treetank/service/jetty.jar:\
/treetank/service/servlet-api.jar:\
/treetank/service/treetank.jar:\
    org.treetank.service.TreeTankService
done
