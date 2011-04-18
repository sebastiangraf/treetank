#!/bin/sh

###SCRIPT FOR CI###
#setting error to false, not beautiful but efficient because of updating/merging
set +e
#submitting to sourceforge
hg push
exit 0
