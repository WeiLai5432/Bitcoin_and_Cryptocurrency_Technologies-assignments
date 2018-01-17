import java.util.HashSet;
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
        this.utxoPool = utxoPool;
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
        HashMap<Transaction.Output, UTXO> rH;
        HashSet<UXTO> uxtoHashSet = new HashSet<>();
//        HashMap<UTXO, Transaction.Output> H
        ArrayList<UTXO> allUTXO = utxoPool.getAllUTXO();
        for (UTXO ut : allUTXO){
            Transaction.Output toutput = utxoPool.getTxOutput(ut);
            rH.put(ut, toutput);
        }
        ArrayList<Transaction.Output> Outputs = tx.getOutputs();
        ArrayList<Transaction.Input> Inputs = tx.getInputs();
        if (Inputs.size() < Outputs.size()){
            return false;
        }
        for (int i = 0; i < Inputs.size(); i++){
            Transaction.Input inp = Inputs[i];
            Transaction.Output op = Outputs[inp.outputIndex];
            if (!rh.get(op) && op.value < 0 ){
                return false;
            }
            UTXO ut = rh.get(op);
            if (!utxoHashSet.add(ut)){
                return false;
            }
            if (!Crypto.verifySignature(op.address, inp.prevTxHash, inp.signature)){
                return false;
            }
        }
        return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
    }

}
