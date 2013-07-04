#!/bin/sh

iscsiadm -m node -T iqn.2010-04.local-test:disk-1 -p 134.34.165.140:3260 --login
mkfs.ext3 /dev/sda
mount /dev/sda /iscsi
