package org.example;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest
        extends TestCase {
    public void testRemove() {
        BTree<Integer> bTree = new BTree<>();
        bTree.add(1);
        bTree.add(2);

        Assert.assertEquals(((BTree<Integer>) bTree).getNode(2), bTree.remove(2));
    }


}
