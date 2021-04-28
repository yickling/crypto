import java.util.ArrayList;

public class TxHandler {

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    UTXOPool _pool;

    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
        _pool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        boolean condition2 = true;
        boolean condition5 = false;
        double sumInputs = 0.0, sumOutputs = 0.0;
        ArrayList<UTXO> allUtxos = _pool.getAllUTXO();
        ArrayList<Transaction.Input> inputs = tx.getInputs();
        ArrayList<Transaction.Output> outputs = tx.getOutputs();
        ArrayList<UTXO> txnUtxos = new ArrayList<UTXO>();

        try {
            for (int index = 0; index < tx.getInputs().size(); index++) {
                Transaction.Input input = tx.getInput(index);
                UTXO u = new UTXO(input.prevTxHash, input.outputIndex);
    
                // (3)
                for (UTXO v: txnUtxos) {
                    if (u.hashCode() == v.hashCode()) {
                        return false;
                    }
                }
        
                txnUtxos.add(u);
                // (1)
                if (!allUtxos.contains(u) || !_pool.contains(u)) return false;

                // // (2)
                Transaction.Output prevOutput = _pool.getTxOutput(u);
                if (prevOutput == null || prevOutput.address == null) return false;

                if (!Crypto.verifySignature(prevOutput.address, tx.getRawDataToSign(index), input.signature)) {
                    return false;
                }

                sumInputs += prevOutput.value;
            }

            for (Transaction.Output o: outputs) {
                // (4)
                if (o.value < 0.0) return false;
                sumOutputs += o.value;
            }
    
            // (5)
            condition5 = sumInputs >= sumOutputs;
            return condition2 && condition5;                
        } catch (Exception e) {
            //TODO: handle exception
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
        return new Transaction[1];
    }

}
