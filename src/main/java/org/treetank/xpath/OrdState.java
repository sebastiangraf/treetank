package org.treetank.xpath;

/**
 * Enum represents different states of the result order that help to specify, if
 * the result query will be in document order or not.
 */
public enum OrdState {

  /** State of the HiddersMichiels - automaton. */
  LIN {

    /**
     * {@inheritDoc}
     */
    @Override
    public OrdState updateOrdChild() {

      mOrdRank++;
      return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrdState updateOrdParent() {

      if (mOrdRank > 0) {
        mOrdRank--;
      }
      return this;
    }
  },

  /** State of the HiddersMichiels - automaton. */
  MAX1 {

    /**
     * {@inheritDoc}
     */
    @Override
    public OrdState updateOrdChild() {

      return OrdState.GENSIB;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrdState updateOrdDesc() {

      return OrdState.SIB;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrdState updateOrdFollPre() {

      return OrdState.SIB;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrdState updateOrdFollPreSib() {

      return OrdState.GENSIB;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrdState updateOrdAncestor() {

      return OrdState.LIN;
    }
  },

  /** State of the HiddersMichiels - automaton. */
  GENSIB {
    
    /**
     * {@inheritDoc}
     */
    @Override
    public OrdState updateOrdChild() {

      return this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public OrdState updateOrdParent() {

      return OrdState.GEN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrdState updateOrdDesc() {

      return OrdState.SIB;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrdState updateOrdFollPreSib() {

      mOrdRank++;
      return OrdState.GEN;

    }
  },

  /** State of the HiddersMichiels - automaton. */
  GEN {

    /**
     * {@inheritDoc}
     */
    @Override
    public OrdState updateOrdChild() {

      mOrdRank++;
      return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrdState updateOrdParent() {

      if (mOrdRank > 0) {
        mOrdRank--;
      }
      return this;
    }
  },

  /** State of the HiddersMichiels - automaton. */
  SIB {

    /**
     * {@inheritDoc}
     */
    @Override
    public OrdState updateOrdChild() {

      mOrdRank++;
      return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrdState updateOrdParent() {

      if (mOrdRank > 0) {
        mOrdRank--;
        return this;
      } else {
        return OrdState.UNORD;
      }
    }
  },

  /** State of the HiddersMichiels - automaton. */
  UNORD;

  
  /**
   * If mOrderRank is 0, the result sequence will be in document order. If it is
   * greater than 0 it is not any more, but it can retain the ordered property,
   * if a certain sequence of axis follows. For more details see [Hidders, J.,
   * Michiels, P., "Avoiding Unnecessary Ordering Operations in XPath", 2003]
   */
  static int mOrdRank;

  /**
   * Changes the state according to a child step.
   * 
   * @return the updated order state
   */
  public OrdState updateOrdChild() {
    mOrdRank++;
    return this;
  }

  /**
   * Changes the state according to a union step.
   * 
   * @return the updated order state
   */
  public OrdState updateOrdUnion() {

    return OrdState.UNORD;
  }

  /**
   * Changes the state according to a parent step.
   * 
   * @return the updated order state
   */
  public OrdState updateOrdParent() {

    return this;
  }

  /**
   * Changes the state according to a descendant/ descendant-or.self step.
   * 
   * @return the updated order state
   */
  public OrdState updateOrdDesc() {

    return OrdState.UNORD;
  }

  /**
   * Changes the state according to a following/preceding step.
   * 
   * @return the updated order state
   */
  public OrdState updateOrdFollPre() {

    return OrdState.UNORD;

  }

  /**
   * Changes the state according to a following-sibling/preceding-sibling step.
   * 
   * @return the updated order state
   */
  public OrdState updateOrdFollPreSib() {

    if (mOrdRank == 0) {
      mOrdRank++;
    }
    return this;
  }

  /**
   * Changes the state according to a ancestor step.
   * 
   * @return the updated order state
   */
  public OrdState updateOrdAncestor() {

    return OrdState.UNORD;
  }

  /**
   * Initializes the order state.
   */
  public void init() {

    mOrdRank = 0;
    
  }
}


