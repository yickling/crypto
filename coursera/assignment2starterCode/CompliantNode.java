import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {
    HashSet<Integer> _followees;
    Set<Transaction> _pendingTxns;

    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        // IMPLEMENT THIS
        _followees = new HashSet<Integer>();
    }

    public void setFollowees(boolean[] followees) {
        _followees.clear();

        // IMPLEMENT THIS
        for (int i = 0; i < followees.length ; ++i) {
            if (followees[i]) _followees.add(i);
        }
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        // IMPLEMENT THIS
        _pendingTxns = pendingTransactions;
    }

    public Set<Transaction> sendToFollowers() {
        // IMPLEMENT THIS
        return _pendingTxns;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        // IMPLEMENT THIS
        for (Candidate c: candidates) {
            if (_followees.contains(c.sender)) _pendingTxns.add(c.tx);
        }
    }
}
