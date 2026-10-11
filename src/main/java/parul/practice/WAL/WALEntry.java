package parul.practice.WAL;

public class WALEntry {

    private int txnId;
    private long seqNo;
    private OperationType operation;
    private String key;
    private String value;
    private int checkSum;

    public WALEntry(Builder builder) {

        txnId = builder.txnId;
        seqNo = builder.seqNo;
        operation = builder.operation;
        key = builder.key;
        value = builder.value;
        checkSum = builder.checkSum;

    }

    public int getTxnId() {
        return txnId;
    }

    public long getSeqNo() {
        return seqNo;
    }

    public OperationType getOperation() {
        return operation;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public int getCheckSum() {
        return checkSum;
    }

    @Override
    public String toString() {
        return "WALEntry{" +
                "txnId='" + txnId + '\'' +
                ", seqNo='" + seqNo + '\'' +
                ", operation=" + operation +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", checkSum='" + checkSum + '\'' +
                '}';
    }

    public static class Builder{

        private int txnId;
        private long seqNo;
        private OperationType operation;
        private String key;
        private String value;
        private int checkSum;

        public Builder setTxnId(int txnId) {
            this.txnId = txnId;
            return this;
        }

        public Builder setSeqNo(long seqNo) {
            this.seqNo = seqNo;
            return this;

        }

        public Builder setOperation(OperationType operation) {
            this.operation = operation;
            return this;

        }

        public Builder setKey(String key) {
            this.key = key;
            return this;
        }

        public Builder setValue(String value) {
            this.value = value;
            return this;
        }

        public Builder setCheckSum(int checkSum) {
            this.checkSum = checkSum;
            return this;
        }

        public WALEntry build(){
            return new WALEntry(this);
        }

    }
}
