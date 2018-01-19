import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.security.PublicKey;

public class MaxFeeTxHandler {

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    private UTXOPool utxoPool;

    public MaxFeeTxHandler(UTXOPool utxoPool) {
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
        HashSet<UTXO> utxoHashSet = new HashSet<>();
        ArrayList<UTXO> allUTXO = utxoPool.getAllUTXO();
        double totalInput = 0.0;
        double totalOutput = 0.0;
        for (int i = 0; i < tx.numInputs(); i++){
            Transaction.Input inp = tx.getInput(i);
//            Transaction.Output op = tx.getOutput(inp.outputIndex);
            UTXO ut = new UTXO(inp.prevTxHash, inp.outputIndex);
            Transaction.Output op = utxoPool.getTxOutput(ut);
            if (!utxoPool.contains(ut)){
                return false;
            }
            if (!utxoHashSet.add(ut)){
                return false;
            }
            if (!Crypto.verifySignature(op.address, tx.getRawDataToSign(i), inp.signature)){
                return false;
            }
//            Transaction.Output txoutput = utxoPool.getTxOutput(ut);
            totalInput += op.value;
        }
        for (int i = 0; i < tx.numOutputs(); i++){
            if (tx.getOutput(i).value < 0){
                return false;
            }
            totalOutput += tx.getOutput(i).value;
        }
        return totalInput >= totalOutput;
    }

    private double txFee(Transaction tx){
        double totalInput = 0.0;
        double totalOutput = 0.0;
        for (int i = 0; i < tx.numInputs(); i++){
            Transaction.Input inp = tx.getInput(i);
//            Transaction.Output op = tx.getOutput(inp.outputIndex);
            UTXO ut = new UTXO(inp.prevTxHash, inp.outputIndex);
            Transaction.Output txoutput = utxoPool.getTxOutput(ut);
            totalInput += txoutput.value;
        }
        for (int i = 0; i < tx.numOutputs(); i++){
            totalOutput += tx.getOutput(i).value;
        }
        return (totalInput - totalOutput);
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
        for (int i = 0; i < possibleTxs.length-1; i++){
            for (int j = i; j < possibleTxs.length; j++){
                double feei = txFee(possibleTxs[i]);
                double feej = txFee(possibleTxs[j]);
                if (feei < feej){
                    Transaction temp = possibleTxs[i];
                    possibleTxs[i] = possibleTxs[j];
                    possibleTxs[i] = temp;
                }
            }
        }

        Set<Transaction> validTxs = new HashSet<Transaction>();
        int cnt = 0;
        for (Transaction tc : possibleTxs){
            if(isValidTx(tc)){
                validTxs.add(tc);
                cnt++;
                for (Transaction.Input inp : tc.getInputs()){
                    UTXO ut = new UTXO(inp.prevTxHash, inp.outputIndex);
                    utxoPool.removeUTXO(ut);
                }
                int i = 0;
                for (Transaction.Output oup : tc.getOutputs()){
                    UTXO ut = new UTXO(tc.getHash(), i++);
                    utxoPool.addUTXO(ut, oup);
                }
            }
        }
        Transaction[] result = new Transaction[cnt];
//        for (int i = 0; i < cnt ;i++){
//            result[i] = validTxs[i];
//        }
        result = validTxs.toArray(result);
        return result;
    }

}
