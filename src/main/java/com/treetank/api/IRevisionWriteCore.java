package com.treetank.api;


public interface IRevisionWriteCore {

  public RevisionReference writeRevision(
      final long revision,
      final FragmentReference fragmentReference);

}
