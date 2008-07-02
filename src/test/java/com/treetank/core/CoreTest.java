package com.treetank.core;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.treetank.api.Configuration;
import com.treetank.api.FragmentReference;
import com.treetank.api.RevisionReference;

public class CoreTest {

  private static final String TANK = "target/CoreTest";

  @Before
  public void setUp() {
    final Core tank = new Core(TANK, 1);
    tank.erase();
  }

  @Test
  public void testCreateLoad() {
    final Core tank = new Core(TANK, 1);
    final Configuration configuration = new Configuration();
    tank.create(configuration);

    final Configuration newConfiguration = tank.load();

    Assert.assertEquals(configuration.getMaxRevision(), newConfiguration
        .getMaxRevision());
  }

  @Test
  public void testWriteRead() {
    final Core tank = new Core(TANK, 1);

    tank.create();

    final byte[] fragment = new byte[666];
    fragment[66] = 66;

    final FragmentReference fragmentReference = tank.writeFragment(fragment);
    tank.writeRevision(fragmentReference);

    final RevisionReference newRevisionReference = tank.readRevision(1, 1);
    Assert.assertEquals(fragmentReference.getOffset(), newRevisionReference
        .getOffset());
    Assert.assertEquals(fragmentReference.getLength(), newRevisionReference
        .getLength());

    final byte[] newFragment =
        tank.readFragment(1, new FragmentReference(newRevisionReference
            .getOffset(), newRevisionReference.getLength()));
    Assert.assertArrayEquals(fragment, newFragment);
  }

}
