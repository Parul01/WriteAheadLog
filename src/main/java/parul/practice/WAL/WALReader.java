package parul.practice.WAL;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.CRC32;
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

    private long computeCheckSum(String data){
        CRC32 crc = new CRC32();
        crc.update(data.getBytes(StandardCharsets.UTF_8));
        return crc.getValue();
    }

    private boolean isChecksumValid(List<String> writes) {

        String data = Integer.parseInt(writes.get(0)) + "|"
                + Long.parseLong(writes.get(1)) + "|"
                + OperationType.valueOf(writes.get(2)).name() + "|"
                + writes.get(3) + "|"
                + writes.get(4);

        long crc = computeCheckSum(data);

        System.out.println("checksum " +crc);


        return crc == Long.parseLong(writes.get(5));

    }


    public RecoveryResult replay() throws IOException {
        // read from reader
        Map<Integer, List<WALEntry>> validTxn = new HashMap<>();
        Set<Integer> allCommittedTxns = new HashSet<>();

        RecoveryResult result = null;
        int txn = 0;
        long seq = 0;
        int mxTxn = 0;
        long mxSeq = 0;
        long validBytes = 0;

        String line = "";

        while ((line = reader.readLine()) != null){
            List<String> writes = splitFields(line);
            try {
                if(writes.size() != 6) throw new IllegalArgumentException("bad field count");
                txn = Integer.parseInt(writes.get(0));
                seq = Long.parseLong(writes.get(1));
                Long.parseLong(writes.get(5));
                OperationType.valueOf(writes.get(2));


            }catch (RuntimeException ex){
                System.err.println("Torn/corrupt WAL tail, stopping: " + line);
                break;
            }

            if (!isChecksumValid(writes)) {
                System.err.println("Checksum mismatch, stopping replay at: " + line);
                break;
            }

            mxSeq = Math.max(mxSeq, seq);
            mxTxn = Math.max(mxTxn, txn);

            validBytes += line.getBytes(StandardCharsets.UTF_8).length+1;

            WALEntry wal = new WALEntry.Builder()
                    .setTxnId(txn)
                    .setSeqNo(seq)
                    .setOperation(OperationType.valueOf(writes.get(2)))
                    .setKey(writes.get(3))
                    .setValue(writes.get(4))
                    .build();

            if(writes.get(2).equals(OperationType.COMMIT.name())){
                allCommittedTxns.add(txn);
                continue;

            }

            if(validTxn.get(txn) == null){
                validTxn.put(txn, new ArrayList<>());
            }

            validTxn.get(txn).add(wal);
        }

        Iterator<Map.Entry<Integer, List<WALEntry>>> it = validTxn.entrySet().iterator();

        while(it.hasNext()){
            Map.Entry<Integer, List<WALEntry>> t = it.next();
            if(!allCommittedTxns.contains(t.getKey())){
                it.remove();
            }
        }


        List<WALEntry> walList = new ArrayList<>();
        for (List<WALEntry> e : validTxn.values()){
            walList.addAll(e);
        }

        walList.sort(Comparator.comparing(WALEntry::getSeqNo));

        result = new RecoveryResult(mxTxn, mxSeq, validBytes, walList);

        return result;

    }



}
