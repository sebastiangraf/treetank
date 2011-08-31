#!/bin/sh

###SCRIPT FOR CI###
#setting error to false, not beautiful but efficient because of updating/merging
set +e
#getting disy data
hg pull
#getting sf data at the moment is to complicated, only push to sf supported
hg pull ssh://sebastiangraf@treetank.hg.sourceforge.net/hgroot/treetank/treetank
hg update 2>/dev/null
hg merge 2>/dev/null
hg commit -m "merged disy and sf data"
exit 0
