#!/bin/sh

iscsiadm --mode node --targetname iqn.2010-04.local-test:disk-1 --portal 134.34.165.140:3260 --logout
