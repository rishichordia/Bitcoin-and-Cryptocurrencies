// import ArrayList, HashMap
import java.util.ArrayList;
import java.util.HashMap;

// Blockchain class
public class BlockChain {
    public static final int CUT_OFF_AGE = 10;
    private TransactionPool transactionPool;
    private HashMap<ByteArrayWrapper, BlockNode> blockchain;
    private BlockNode maxHeightBlockNode;

    // creates a blockchain with genesis block
    public BlockChain(Block genesisBlock) {
        blockchain = new HashMap<>();
        UTXOPool utxoPool = new UTXOPool();

        // add transactions to UTXOPool of this block
        addNewTransactionsToUTXOPool(genesisBlock, utxoPool);

        BlockNode genesisNode = new BlockNode(genesisBlock, null, utxoPool);

        // add genesis node to blockchain
        blockchain.put(new ByteArrayWrapper(genesisBlock.getHash()), genesisNode);
        transactionPool = new TransactionPool();
        maxHeightBlockNode = genesisNode;
    }

    // getter for block of max height node
    public Block getMaxHeightBlock() {
        return this.maxHeightBlockNode.block;
    }

    // getter for UTXOPool of max height node
    public UTXOPool getMaxHeightUTXOPool() {
        return this.maxHeightBlockNode.getUTXOPoolCopy();
    }

    // getter for transaction pool for next block
    public TransactionPool getTransactionPool() {
        return transactionPool;
    }

    public boolean addBlock(Block block) {
        byte[] prevHash = block.getPrevBlockHash();

        // check if the block is a genesis block
        if (prevHash == null) {
            return false;
        }

        BlockNode prevNode = blockchain.get(new ByteArrayWrapper(prevHash));

        // check if previous node is valid
        if (prevNode == null) {
            return false;
        }

        TxHandler handler = new TxHandler(prevNode.getUTXOPoolCopy());
        Transaction[] txs = block.getTransactions().toArray(new Transaction[block.getTransactions().size()]);

        // select valid transactions from all transactions of the block
        Transaction[] validTxs = handler.handleTxs(txs);

        // check if all transactions of the block are valid
        if (txs.length != validTxs.length) {
            return false;
        }

        int proposedHeight = prevNode.height + 1;

        // check if the proposed block is too old
        if (proposedHeight < maxHeightBlockNode.height - CUT_OFF_AGE + 1) {
            return false;
        }

        // construct new Blocknode with this block
        UTXOPool utxoPool = handler.getUTXOPool();
        addNewTransactionsToUTXOPool(block, utxoPool);
        BlockNode newNode = new BlockNode(block, prevNode, utxoPool);
        blockchain.put(new ByteArrayWrapper(block.getHash()), newNode);

        // update max height blocknode if necessary
        if (proposedHeight > maxHeightBlockNode.height) {
            maxHeightBlockNode = newNode;
        }
        return true;
    }

    // add Transaction to transaction pool
    public void addTransaction(Transaction tx) {
        transactionPool.addTransaction(tx);
    }

    // add output transactions of block to utxoPool
    public void addNewTransactionsToUTXOPool(Block block, UTXOPool utxoPool) {
        Transaction tx = block.getCoinbase();

        // iterate over output transactions
        for (int i = 0; i < tx.numOutputs(); i++) {
            UTXO utxo = new UTXO(tx.getHash(), i);
            utxoPool.addUTXO(utxo, tx.getOutput(i));
        }
    }

    // BlockNode class
    public class BlockNode {
        public Block block;
        public BlockNode parent;
        public ArrayList<BlockNode> children;
        public int height;
        private UTXOPool utxoPool;

        // constructor
        public BlockNode(Block block, BlockNode parent, UTXOPool utxoPool) {
            this.block = block;
            this.parent = parent;
            this.children = new ArrayList<BlockNode>();

            // check if the the block is genesis block
            if (parent == null) {
                this.height = 1;
            } else {
                parent.children.add(this);
                this.height = parent.height + 1;
            }
            this.utxoPool = utxoPool;
        }

        // getter for UTXOPool of this block
        public UTXOPool getUTXOPoolCopy() {
            return new UTXOPool(this.utxoPool);
        }
    }

