/**
 * Copyright (c) 2010, Distributed Systems Group, University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED AS IS AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 */

package org.treetank.service.xml.xpath;

/**
 * <h1>DupState</h1> Enum represents different states of the current result
 * sequence that helps to specify, whether the result query will contain
 * duplicates.
 */
public enum DupState {

    /** State of the HiddersMichiels - automaton. */
    MAX1 {

        /**
         * {@inheritDoc}
         */
        @Override
        public DupState updateDupFollPreSib() {

            return DupState.GENSIB;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DupState updateDupAncestor() {

            return DupState.LIN;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DupState updateDupChild() {

            return DupState.GENSIB;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DupState updateDupFollPre() {

            DupState.nodup = true;
            return DupState.NO;
        }

    },

    /** State of the HiddersMichiels - automaton. */
    LIN {

        /**
         * {@inheritDoc}
         */
        @Override
        public DupState updateDupFollPreSib() {

            return DupState.SIB;

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DupState updateDupDesc() {

            DupState.nodup = false;
            return DupState.NO;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DupState updateDupChild() {

            return DupState.NO;
        }

    },

    /** State of the HiddersMichiels - automaton. */
    GENSIB {

        /**
         * {@inheritDoc}
         */
        @Override
        public DupState updateDupParent() {

            DupState.nodup = false;
            return DupState.NO;
        }
    },

    /** State of the HiddersMichiels - automaton. */
    SIB {

        /**
         * {@inheritDoc}
         */
        @Override
        public DupState updateDupParent() {

            DupState.nodup = false;
            return DupState.NO;
        }

    },

    /** State of the HiddersMichiels - automaton. */
    NO {

        /**
         * {@inheritDoc}
         */
        @Override
        public DupState updateDupDesc() {

            DupState.nodup = false;
            return DupState.NO;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DupState updateDupParent() {

            DupState.nodup = false;
            return DupState.NO;
        }
    };

    /** Is true, if the expression is still duplicate free. */
    static boolean nodup = true;

    /**
     * Changes the state according to a child step.
     * 
     * @return the updated duplicate state
     */
    public DupState updateDupChild() {

        return this;
    }

    /**
     * Changes the state according to a parent step.
     * 
     * @return the updated duplicate state
     */
    public DupState updateDupParent() {

        return this;
    }

    /**
     * Changes the state according to a descendant, descendant-or-self step.
     * 
     * @return the updated duplicate state
     */
    public DupState updateDupDesc() {

        return DupState.NO;
    }

    /**
     * Changes the state according to a following /preceding step.
     * 
     * @return the updated duplicate state
     */
    public DupState updateDupFollPre() {

        DupState.nodup = false;
        return DupState.NO;
    }

    /**
     * Changes the state according to a following-sibling/preceding-sibling
     * step.
     * 
     * @return the updated duplicate state
     */
    public DupState updateDupFollPreSib() {

        DupState.nodup = false;
        return DupState.NO;
    }

    /**
     * Changes the state according to a ancestor step.
     * 
     * @return the updated duplicate state
     */
    public DupState updateDupAncestor() {

        DupState.nodup = false;
        return DupState.NO;
    }

    /**
     * Changes the state according to a union step.
     * 
     * @return the updated duplicate state
     */
    public DupState updateUnion() {
        DupState.nodup = false;
        return this;
    }
}
