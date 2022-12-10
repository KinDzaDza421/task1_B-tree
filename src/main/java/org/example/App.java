package org.example;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        BTree bTree = new BTree();

        bTree.add(1);
        bTree.add(2);
        bTree.add(3);
        bTree.add(4);
        bTree.add(5);
        bTree.add(6);
        bTree.add(7);
        bTree.add(8);
        bTree.add(9);

        bTree.remove(1);
        bTree.remove(2);
        bTree.remove(4);
        bTree.remove(7);
        bTree.remove(5);
        bTree.remove(8);
        bTree.remove(9);

        System.out.println(bTree.size());
    }
}
