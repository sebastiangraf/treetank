#!/bin/sh

while [ true ]
do
  /treetank/jre/bin/jamvm -Djava.library.path=/treetank/service -jar /treetank/service/TreeTankService.jar
done
