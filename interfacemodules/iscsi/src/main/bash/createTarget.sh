#! /bin/bash

#Simple creating a target and copying it to this project.
##-b: forces build of server
##-s: storagepath
##-s 

##1. note the path
build=$1
bashPath=`pwd`
iscsiPath=$bashPath/../../..

targetConf=$iscsiPath/src/main/resources/jscsi-target.xml
backend=org.treetank.io.berkeley.BerkeleyStorage
revisioning=org.treetank.revisioning.SlidingSnapshot
jarname=iscsi-6.0.1-SNAPSHOT-jar-with-dependencies.jar

#Check if build is triggered
if [ "build" == "$build" ]
	then
	##2. install current project
	cd $iscsiPath/../..
	echo "Building entire project at `pwd`"
	echo "++++++++++++++++++++++++++++++++++++++++++++++++++++++"
	mvn clean install

	##3. build target server
	cd $iscsiPath
	echo "Building iscsi target at `pwd` ++++++++++++++++++++"
	echo "++++++++++++++++++++++++++++++++++++++++++++++++++++++"
	mvn assembly:assembly
fi

##5. start server
cd $iscsiPath
echo "Start target to `pwd` ++++++++++++++++++++"
echo "++++++++++++++++++++++++++++++++++++++++++++++++++++++"
storagePath=`pwd`/target/iscsiStor
rm -rvf $storagePath
mkdir $storagePath
cp target/$jarname .
java -jar $jarname storagePath=$storagePath targetConfiguration=$targetConf backendImplementation=$backend revisioningImplementation=$revisioning
