import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TxHandler {

    // curent UTXOPool
    private UTXOPool utxoPool;

    // makes a copy of utxoPool and stores it in current UTXOPool
    public TxHandler(UTXOPool utxoPool) {
        this.utxoPool = new UTXOPool(utxoPool);
    }

    public boolean isValidTx(Transaction tx) {
        UTXOPool uniqueUTXOs = new UTXOPool();
        double previousTxOutSum = 0;
        double currentTxOutSum = 0;

        // iterate over all inputs of transaction tx
        for (int i = 0; i < tx.numInputs(); i++) {
            Transaction.Input in = tx.getInput(i);
            UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);

            // previous output of input transaction
            Transaction.Output output = utxoPool.getTxOutput(utxo);

            // check if the transaction is in current UTXOPool
            if (!utxoPool.contains(utxo))
                return false;

            // check if the signatures on each input is valid
            if (!Crypto.verifySignature(output.address, tx.getRawDataToSign(i), in.signature))
                return false;

            // check if the transaction is claimed only once
            if (uniqueUTXOs.contains(utxo))
                return false;

            // add transaction to uniqueUTXOs
            uniqueUTXOs.addUTXO(utxo, output);

            // add previous output value i.e. input value
            previousTxOutSum += output.value;
        }

        // iterate over all transactions output
        for (Transaction.Output out : tx.getOutputs()) {
            // check if the output value are non-negative
            if (out.value < 0)
                return false;

            // add current output value
            currentTxOutSum += out.value;
        }

        // check if the sum of the input value is less than sum of output value
        return previousTxOutSum >= currentTxOutSum;
    }

    public Transaction[] handleTxs(Transaction[] possibleTxs) {

        // set to store all the valid transactions
        Set<Transaction> validTxs = new HashSet<>();

        // iterate over all possible transactions
        for (Transaction tx : possibleTxs) {
            // check if the transactions are valid
            if (isValidTx(tx)) {
                // add transaction to set of valid transactions
                validTxs.add(tx);

                // iterate over inputs of transactions to remove them from current UTXOPool
                for (Transaction.Input in : tx.getInputs()) {
                    UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
                    utxoPool.removeUTXO(utxo);
                }

                // iterate over outputs of transactions to add them to current UTXOPool
                for (int i = 0; i < tx.numOutputs(); i++) {
                    Transaction.Output out = tx.getOutput(i);
                    UTXO utxo = new UTXO(tx.getHash(), i);
                    utxoPool.addUTXO(utxo, out);
                }
            }
        }

        // convert set of transactions to array of transactions
        Transaction[] validTxArray = new Transaction[validTxs.size()];
        return validTxs.toArray(validTxArray);
    }
}
