#!/bin/sh

###SCRIPT FOR CI###
#setting error to false, not beautiful but efficient because of updating/merging
set +e
#getting disy data
hg pull
hg update
hg merge 2>/dev/null
#getting sf data
hg pull ssh://sebastiangraf@treetank.hg.sourceforge.net/hgroot/treetank/treetank
hg update
hg merge 2>/dev/null
hg commit -m "merged disy and sf data"
