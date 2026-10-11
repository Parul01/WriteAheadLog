package parul.practice.WAL;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import java.util.concurrent.atomic.AtomicLong;

// send to WALWriter to write to WAL
//replay from Reader
// do actual transactions
public class WALService {

    private int currentTxnId;          // active transaction
    private AtomicLong globalSeqNo;    // global sequence counter
    private WALWriter walWriter;       // writes to file
    private KVStore kvStore;           // in-memory DB
    private WALReader walReader;       // for recovery on startup
    private static final String WAL_PATH = "wal.log";

    public WALService() throws IOException {

        this.walWriter = new WALWriter(WAL_PATH);
        this.walReader = new WALReader(WAL_PATH);

        this.kvStore = new KVStore();

        globalSeqNo = new AtomicLong(0);
        currentTxnId = 0;
    }

    public void put(String key, String value) throws IOException {

        long seqNo = globalSeqNo.incrementAndGet();
        int checksum = Objects.hash(currentTxnId + "|" + seqNo + "|" +
                OperationType.PUT.name() + "|" + key + "|" + value);
        WALEntry entry = new WALEntry.Builder()
                .setSeqNo(seqNo)
                .setTxnId(currentTxnId)
                .setOperation(OperationType.PUT)
                .setKey(key)
                .setValue(value)
                .setCheckSum(checksum).build();

        walWriter.append(entry);

        // write to db
        kvStore.put(key, value);
    }

    // creates new txnId
    public void beginTransaction() {
       currentTxnId++;
    }

    public void delete(String key) throws IOException {
        long seqNo = globalSeqNo.incrementAndGet();
        int checksum = Objects.hash(currentTxnId + "|" + seqNo + "|" +
                OperationType.DELETE.name() + "|" + key + "|" + "");
        WALEntry entry = new WALEntry.Builder()
                .setSeqNo(seqNo)
                .setTxnId(currentTxnId)
                .setOperation(OperationType.DELETE)
                .setKey(key)
                .setValue("")
                .setCheckSum(checksum).build();

        walWriter.append(entry);

        // write to db
        kvStore.remove(key);
    } // writes to WAL + KVStore

    public void commit() throws IOException {
        long seqNo = globalSeqNo.incrementAndGet();
        int checksum = Objects.hash(currentTxnId + "|" + seqNo + "|" +
                OperationType.COMMIT.name() + "|" + "" + "|" + "");
        WALEntry entry = new WALEntry.Builder()
                .setSeqNo(seqNo)
                .setTxnId(currentTxnId)
                .setOperation(OperationType.COMMIT)
                .setCheckSum(checksum).build();

        walWriter.append(entry);
        Map<String, String> db = kvStore.getAll();
        for (Map.Entry<String, String> entry1: db.entrySet()){
            System.out.println(entry1.getKey() +" "+entry1.getValue());
        }
    }// writes COMMIT to WAL

    public void startup() throws IOException {
        List<WALEntry> pendingTxns = walReader.replay();
        System.out.println("On startup !!!!!");
        System.out.println(pendingTxns);

        int maxTxnId = 0;
        long seqno = 0;
        for(WALEntry e: pendingTxns){
            if(e.getTxnId() > maxTxnId){
                maxTxnId = e.getTxnId();

            }

            if(seqno < e.getSeqNo()){
                seqno = e.getSeqNo();
            }
        }

        currentTxnId = maxTxnId;
        globalSeqNo.set(seqno);
        for(WALEntry w: pendingTxns){
            OperationType type = w.getOperation();
            if(type.equals(OperationType.DELETE)){
                kvStore.remove(w.getKey());
            }else if(w.getOperation().equals(OperationType.PUT)){
                kvStore.put(w.getKey(), w.getValue());
            }else{
                System.out.println("Not a valid operation");
            }
        }

    } // called on restart — replays WAL into KVStore


    public KVStore getKVStore() {
        return kvStore;
    }

    public String get(String key){
        return kvStore.get(key);
    }
}
