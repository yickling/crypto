// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.
import java.util.ArrayList;
import java.util.Iterator;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.DatatypeConverter;

public class BlockChain {

    private class TreeNode<T> {

        public T data;
        public TreeNode<T> parent;
        public List<TreeNode<T>> children;
    
        public boolean isRoot() {
            return parent == null;
        }
    
        public boolean isLeaf() {
            return children.size() == 0;
        }
    
        private List<TreeNode<T>> elementsIndex;
    
        public TreeNode(T data) {
            this.data = data;
            this.children = new LinkedList<TreeNode<T>>();
            this.elementsIndex = new LinkedList<TreeNode<T>>();
            this.elementsIndex.add(this);
        }
    
        public TreeNode<T> addChild(T child) {
            TreeNode<T> childNode = new TreeNode<T>(child);
            childNode.parent = this;
            this.children.add(childNode);
            this.registerChildForSearch(childNode);
            return childNode;
        }
    
        public int getLevel() {
            if (this.isRoot())
                return 0;
            else
                return parent.getLevel() + 1;
        }
    
        private void registerChildForSearch(TreeNode<T> node) {
            elementsIndex.add(node);
            if (parent != null)
                parent.registerChildForSearch(node);
        }
    
        public TreeNode<T> findTreeNode(Comparable<T> cmp) {
            for (TreeNode<T> element : this.elementsIndex) {
                T elData = element.data;
                if (cmp.compareTo(elData) == 0)
                    return element;
            }
    
            return null;
        }
    
        @Override
        public String toString() {
            return data != null ? data.toString() : "[data null]";
        }    
    }
    
    public class Snapshot {
        public Block _block;
        public UTXOPool _utxoPool;

        public Snapshot(Block block, UTXOPool utxoPool) {
            _block = block;
            _utxoPool = utxoPool;
        }
    }

    public static final int CUT_OFF_AGE = 10;
    TreeNode<Snapshot> _chain;
    TransactionPool _txnPool;

    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    public BlockChain(Block genesisBlock) {
        UTXOPool utxoPool = new UTXOPool();
        
        Transaction mined = genesisBlock.getCoinbase();
        Transaction.Output output = mined.getOutput(0);
        UTXO u = new UTXO(mined.getHash(), 0);
        utxoPool.addUTXO(u, output);

        // IMPLEMENT THIS
        Snapshot s = new Snapshot(genesisBlock, utxoPool);
        _chain = new TreeNode<Snapshot>(s);
        _txnPool = new TransactionPool();
    }

    private TreeNode<Snapshot> traverseDFS(TreeNode<Snapshot> node) {        
        TreeNode<Snapshot> longestChild = null;

        // System.out.println(">> dfsdrill... " + DatatypeConverter.printHexBinary(node.data._block.getHash()));
        // IMPLEMENT THIS
        for (TreeNode<Snapshot> n : node.children) {
            TreeNode<Snapshot> b = traverseDFS(n);
            if (longestChild == null) longestChild = b;
            if (b.getLevel() > longestChild.getLevel()) longestChild = b;
		}

        if (longestChild == null) {
            // System.out.println(">> longestchild NULL");
            return node;
        }
        // System.out.println(">> longestchild... " + DatatypeConverter.printHexBinary(longestChild.data._block.getHash()));
        return longestChild;
    }

    private TreeNode<Snapshot> findNodeMatchingHash(TreeNode<Snapshot> node, byte[] hash) {
        // System.out.println("matching: " +  DatatypeConverter.printHexBinary(node.data._block.getHash()) + " - " +  DatatypeConverter.printHexBinary(hash));
        if (Arrays.equals(node.data._block.getHash(), hash)) return node;

        // IMPLEMENT THIS
        for (TreeNode<Snapshot> n : node.children) {
            // System.out.println(">> drilling... " + n.children.size());
            return findNodeMatchingHash(n, hash);
		}

        return null;
    }

    public int getMaxHeight() {
        TreeNode<Snapshot> s = traverseDFS(_chain);
        return s.getLevel();        
    }

    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
        TreeNode<Snapshot> s = traverseDFS(_chain);
        return s.data._block;
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        TreeNode<Snapshot> s = traverseDFS(_chain);
        return s.data._utxoPool;
    }

    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
        // IMPLEMENT THIS
        return _txnPool;
    }

    /**
     * Add {@code block} to the block chain if it is valid. For validity, all transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)}.
     * 
     * <p>
     * For example, you can try creating a new block over the genesis block (block height 2) if the
     * block chain height is {@code <=
     * CUT_OFF_AGE + 1}. As soon as {@code height > CUT_OFF_AGE + 1}, you cannot create a new block
     * at height 2.
     * 
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {
        byte[] prevBlockHash = block.getPrevBlockHash();
        if (prevBlockHash == null) return false;

        int maxHeight = this.getMaxHeight();

        TreeNode<Snapshot> prevBlockSnapshot = findNodeMatchingHash(_chain, prevBlockHash);

        if (prevBlockSnapshot == null) {
            System.out.println("prevBlock == null");
            return false;
        }

        int level = prevBlockSnapshot.getLevel();

        if (level < maxHeight - CUT_OFF_AGE) {
            return false;
        }

        ArrayList<Transaction> listTxns = block.getTransactions();
        Transaction[] txnArray = listTxns.toArray(new Transaction[0]);
        TxHandler handler = new TxHandler(prevBlockSnapshot.data._utxoPool);
        Transaction[] validTxns = handler.handleTxs(txnArray);

        UTXOPool utxoPool = handler.getUTXOPool();

        Transaction mined = block.getCoinbase();
        Transaction.Output minedOutput = mined.getOutput(0);
        UTXO minedU = new UTXO(mined.getHash(), 0);
        utxoPool.addUTXO(minedU, minedOutput);

        // System.out.println("ADD]] PARENT: " +  DatatypeConverter.printHexBinary(prevBlockSnapshot.data._block.getHash()) + " - " +  DatatypeConverter.printHexBinary(block.getHash()));
        Snapshot s = new Snapshot(block, utxoPool);
        prevBlockSnapshot.addChild(s);
        _txnPool = new TransactionPool();
        return true;
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        UTXOPool utxoPool = getMaxHeightUTXOPool();

        TxHandler handler = new TxHandler(utxoPool);
        if (!handler.isValidTx(tx)) return;

        // IMPLEMENT THIS
        _txnPool.addTransaction(tx);
    }
}