import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;

public class MaxFeeTxHandler {

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */

    private UTXOPool utxoPool;

    public MaxFeeTxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
        this.utxoPool = new UTXOPool(utxoPool);
    }

    public boolean isValidTxImpl(Transaction tx, UTXOPool currentutxoPool) {
        // IMPLEMENT THIS
        double totalInputs = 0;
        double totalOutputs = 0;
        Set<UTXO> used = new HashSet<UTXO>();
        for (int i = 0; i < tx.numInputs(); i++) {
            Transaction.Input inp = tx.getInput(i);
            UTXO ut = new UTXO(inp.prevTxHash, inp.outputIndex);
            if(!currentutxoPool.contains(ut)) {
                return false;
            }  // check(1)
            Transaction.Output out = currentutxoPool.getTxOutput(ut);
            totalInputs += out.value;
            if(!Crypto.verifySignature(out.address, tx.getRawDataToSign(i), inp.signature)) {
                return false;
            }  // check(2)
            if(!used.add(ut)) {
                return false;
            }  // check(3)
        }
        for(Transaction.Output output: tx.getOutputs()) {
            if(output.value < 0) {
                return false;
            }  // check(4)
            totalOutputs += output.value;
        }
        return totalInputs >= totalOutputs;
    }


    double calcTxFees(Transaction tx, UTXOPool currentutxoPool) {
        double totalInputs = 0;
        double totalOutputs = 0;
        Set<UTXO> used = new HashSet<UTXO>();
        for (int i = 0; i < tx.numInputs(); i++) {
            Transaction.Input inp = tx.getInput(i);
            UTXO ut = new UTXO(inp.prevTxHash, inp.outputIndex);
            if(!currentutxoPool.contains(ut)) {
                return -1;
            }  // check(1)
            Transaction.Output out = currentutxoPool.getTxOutput(ut);
            totalInputs += out.value;
            if(!Crypto.verifySignature(out.address, tx.getRawDataToSign(i), inp.signature)) {
                return -1;
            }  // check(2)
            if(!used.add(ut)) {
                return -1;
            }  // check(3)
        }
        for(Transaction.Output output: tx.getOutputs()) {
            if(output.value < 0) {
                return -1;
            }  // check(4)
            totalOutputs += output.value;
        }
        return totalInputs - totalOutputs;
    }

    private static void updatePool(Transaction tx, UTXOPool pool) {
        for(Transaction.Input inp: tx.getInputs()) {
            UTXO ut = new UTXO(inp.prevTxHash, inp.outputIndex);
            pool.removeUTXO(ut);
        }
        for(int i = 0 ; i < tx.numOutputs(); i++) {
            UTXO ut = new UTXO(tx.getHash(), i);
            pool.addUTXO(ut, tx.getOutput(i));
        }
    }

    private int[] currentList = new int[0];

    private static int[] addIntoList(int[] list, int index) {
        list  = Arrays.copyOf(list, list.length + 1);
        list[list.length - 1] = index;
        return list;
    }

    private double maxFees = 0.0;

    private void dfsForTxs(UTXOPool pool, Transaction[] possibleTxs, int startIndex, int[] nowList, double fees) {
        if(fees > maxFees) {
            maxFees = fees;
            currentList = nowList;
        }

        for(int i = startIndex; i < possibleTxs.length; i++) {
            Transaction tx = possibleTxs[i];
            double fee = calcTxFees(tx, pool);
            if(fee >= 0) {
                UTXOPool newPool = new UTXOPool(pool);
                updatePool(tx, newPool);
                dfsForTxs(newPool, possibleTxs, i+1, addIntoList(nowList, i), fees + fee);
            }
        }
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
        // IMPLEMENT THIS
        return isValidTxImpl(tx, utxoPool);
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
        dfsForTxs(utxoPool, possibleTxs, 0, currentList, 0);
        Transaction[] valid = new Transaction[currentList.length];
        for(int i = 0; i < currentList.length; i++) {
            valid[i] = possibleTxs[currentList[i]];
        }
        return valid;
    }

}

