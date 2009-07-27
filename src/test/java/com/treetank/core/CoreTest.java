/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
 * 
 * Patent Pending.
 * 
 * NO permission to use, copy, modify, and/or distribute this software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 * $Id: CoreTest.java 4367 2008-08-24 12:24:09Z kramis $
 */

package com.treetank.core;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.treetank.shared.Configuration;
import com.treetank.shared.DeletedNode;
import com.treetank.shared.Fragment;
import com.treetank.shared.FragmentReference;
import com.treetank.shared.RevisionReference;
import com.treetank.shared.RootNode;

public class CoreTest {

	private static final String TANK = "target/tnk/CoreTest";

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

		final Fragment fragment = new Fragment();
		fragment.addNode(new RootNode(1, 2, "Marc", "Foo."));
		fragment.addNode(new DeletedNode(2, 2));

		final FragmentReference fragmentReference = tank
				.writeFragment(fragment);
		tank.writeRevision(fragmentReference);

		final RevisionReference newRevisionReference = tank.readRevision(1, 1);
		Assert.assertEquals(fragmentReference.getOffset(), newRevisionReference
				.getOffset());
		Assert.assertEquals(fragmentReference.getLength(), newRevisionReference
				.getLength());

		final Fragment newFragment = tank.readFragment(1,
				new FragmentReference(newRevisionReference.getOffset(),
						newRevisionReference.getLength()));
		Assert
				.assertEquals(fragment.getNodeCount(), newFragment
						.getNodeCount());
		Assert.assertEquals(fragment.getNode(1).getRevision(), newFragment
				.getNode(1).getRevision());
		Assert.assertEquals("Marc", ((RootNode) newFragment.getNode(1))
				.getAuthor());
		Assert.assertEquals(0, newFragment.getNode(2).getType());
	}

}
