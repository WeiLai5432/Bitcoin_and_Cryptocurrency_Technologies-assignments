import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.security.PublicKey;

public class TxHandler {

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */

    private UTXOPool utxoPool;

    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
        this.utxoPool = new UTXOPool(utxoPool);
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
        double totalInputs = 0;
        double totalOutputs = 0;
        Set<UTXO> used = new HashSet<UTXO>();
        for (int i = 0; i < tx.numInputs(); i++) {
            Transaction.Input inp = tx.getInput(i);
            UTXO ut = new UTXO(inp.prevTxHash, inp.outputIndex);
            if(!utxoPool.contains(ut)) {
                return false;
            }  // check(1)
            Transaction.Output out = utxoPool.getTxOutput(ut);
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

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
        Set<Transaction> validTxs = new HashSet<Transaction>();
        boolean updated = true;
        for(Transaction tx: possibleTxs) {
            if(validTxs.contains(tx)) continue;
            if(isValidTx(tx)) {
                validTxs.add(tx);
                updated = true;
                for(Transaction.Input inp: tx.getInputs()) {
                    UTXO ut = new UTXO(inp.prevTxHash, inp.outputIndex);
                    utxoPool.removeUTXO(ut);
                }
                for(int i = 0 ; i < tx.numOutputs(); i++) {
                    UTXO ut = new UTXO(tx.getHash(), i);
                    utxoPool.addUTXO(ut, tx.getOutput(i));
                }
            }
        }
        Transaction[] valid = new Transaction[validTxs.size()];
        return validTxs.toArray(valid);
    }

}
