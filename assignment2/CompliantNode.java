// import ArrayList, HashSet, Set
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class CompliantNode implements Node {

    double p_graph, p_malicious, p_txDistribution;
    int numRounds;
    boolean[] followees;
    Set<Transaction> pendingTransactions;

    // constructor
    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        this.p_graph = p_graph;
        this.p_malicious = p_malicious;
        this.p_txDistribution = p_txDistribution;
        this.numRounds = numRounds;
    }

    // setter for followees
    public void setFollowees(boolean[] followees) {
        this.followees = followees;
    }

    // setter for pendingTransactions
    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        this.pendingTransactions = pendingTransactions;
    }

    // send pendingTransactions to followers
    public Set<Transaction> sendToFollowers() {
        Set<Transaction> toSend = new HashSet<>(pendingTransactions);
        pendingTransactions.clear();
        return toSend;
    }

    // collect transactions from Followers
    public void receiveFromFollowees(Set<Candidate> candidates) {
        for (Candidate candidate : candidates) {
            pendingTransactions.add(candidate.tx);
        }
    }
}
