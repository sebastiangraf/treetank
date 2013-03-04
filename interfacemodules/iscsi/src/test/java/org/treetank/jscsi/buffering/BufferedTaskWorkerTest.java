package org.treetank.jscsi.buffering;

import java.util.List;
import java.util.Random;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests functionallity of a buffered task worker.
 * 
 * @author Andreas Rain
 * 
 */
public class BufferedTaskWorkerTest {

    private BufferedTaskWorker worker;

    @BeforeClass
    public void setUp() {
        byte[] testBytes = new byte[4096];

        Random rand = new Random(42);

        worker = new BufferedTaskWorker(null, 0);

        // Create some dummy tasks
        rand.nextBytes(testBytes);
        worker.newTask(testBytes, 0, 2048, 20);
        rand.nextBytes(testBytes);
        worker.newTask(testBytes, 0, 1024, 0);
        rand.nextBytes(testBytes);
        worker.newTask(testBytes, 0, 512, 128);
        rand.nextBytes(testBytes);
        worker.newTask(testBytes, 0, 2048, 128);
        rand.nextBytes(testBytes);
        worker.newTask(testBytes, 0, 2048, 2048);

    }

    /**
     * Test functionallity of collision detection.
     */
    @Test
    public void testCheckForCollisons() {
        List<Collision> collisions = worker.checkForCollisions(1024, 50);

        // There are 5 buffered tasks of which only 4 collide
        // with the given argument.
        Assert.assertTrue(collisions.size() == 4);

        // The size of the bytes in each collision.
        Assert.assertTrue(collisions.get(0).getBytes().length == 1024);
        Assert.assertTrue(collisions.get(1).getBytes().length == 1024 - 50);
        Assert.assertTrue(collisions.get(2).getBytes().length == 512);
        Assert.assertTrue(collisions.get(3).getBytes().length == 1024 - 78);

    }

}
