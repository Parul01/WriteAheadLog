package parul.practice.WAL;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class WALReader {

    BufferedReader reader;

    public WALReader(String path) throws FileNotFoundException {
        reader = new BufferedReader(new FileReader(path));
    }

    private List<String> splitFields(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        boolean escaped = false;

        for (char c : line.toCharArray()) {

            if (escaped) {
                current.append(c);
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '|') {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        if (escaped) {
            throw new IllegalArgumentException("Invalid WAL entry: trailing escape");
        }

        fields.add(current.toString());

        return fields;
    }

    private boolean isChecksumValid(List<String> writes){
        int stored = Integer.parseInt(writes.get(5));
        int computed = Objects.hash(
                writes.get(0) + "|" + writes.get(1) + "|" +
                        writes.get(2) + "|" + writes.get(3) + "|" + writes.get(4)
        );
        System.out.println("Stored: " + stored + " Computed: " + computed);  // ← add
        return stored == computed;
    }

    public RecoveryResult replay() throws IOException {
        // read from reader
        Map<Integer, List<WALEntry>> pendingTxns = new HashMap<>();
        Set<Integer> allCommittedTxns = new HashSet<>();
        Set<Integer> invalidTransactions = new HashSet<>();

        int mxTxn = 0;
        long mxSeq = 0;
        int txn = 0;
        long validBytes = 0;

        String line = "";
        while((line = reader.readLine())!= null){
            System.out.println("Reading line: " + line);  // ← add this
            List<String> writes = splitFields(line);
            System.out.println("Fields count: " + writes.size());  // ← add this

            try {
                writes = splitFields(line);
                if (writes.size() != 6) throw new IllegalArgumentException("bad field count");
                txn = Integer.parseInt(writes.get(0));
                long seq = Long.parseLong(writes.get(1));
                Integer.parseInt(writes.get(5));
                OperationType.valueOf(writes.get(2));
                mxTxn = Math.max(mxTxn, txn);
                mxSeq = Math.max(mxSeq, seq);
            } catch (RuntimeException ex) {
                System.err.println("Torn/corrupt WAL tail, stopping: " + line);
                break;
            }
            if (!isChecksumValid(writes)) {
                System.err.println("Checksum mismatch, stopping replay at: " + line);
                break;
            }
            validBytes += line.getBytes(StandardCharsets.UTF_8).length + 1;
            if (writes.get(2).equals(OperationType.COMMIT.name())) { allCommittedTxns.add(txn); continue; }

            WALEntry walEntry = new WALEntry.Builder().setTxnId(Integer.parseInt(writes.get(0))).setSeqNo(Long.parseLong(writes.get(1))).setOperation(OperationType.valueOf(writes.get(2))).setKey(writes.get(3)).setValue(writes.get(4)).setCheckSum(Integer.parseInt(writes.get(5))).build();
            if(!pendingTxns.containsKey(txn)){
                pendingTxns.put(txn, new ArrayList<>());
            }
            pendingTxns.get(txn).add(walEntry);


        }

        for (Integer t: invalidTransactions){
            if(pendingTxns.containsKey(t)){
                pendingTxns.remove(t);
            }
        }

        Iterator<Map.Entry<Integer, List<WALEntry>>> it = pendingTxns.entrySet().iterator();

        while(it.hasNext()){
            Map.Entry<Integer, List<WALEntry>> entry = it.next();
            if(!allCommittedTxns.contains(entry.getKey())){
                it.remove();
            }
        }

        List<WALEntry> result = new ArrayList<>();
        for (List<WALEntry> e : pendingTxns.values()){
            result.addAll(e);
        }

        result.sort(Comparator.comparing(WALEntry::getSeqNo));

        System.out.println("Committed txns: " + allCommittedTxns);  // ← add this
        System.out.println("Pending txns: " + pendingTxns.keySet());
        RecoveryResult record = new RecoveryResult(mxTxn, mxSeq, validBytes, result);
        return record;
    }



}
