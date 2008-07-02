package com.treetank.api;

public interface ICore {

  public void create();

  public void create(final Configuration configuration);

  public Configuration load();

  public void erase();

  public FragmentReference writeFragment(final byte[] fragment);

  public RevisionReference writeRevision(
      final FragmentReference fragmentReference);

  public RevisionReference readRevision(final int core, final long revision);

  public byte[] readFragment(
      final int core,
      final FragmentReference fragmentReference);

}
