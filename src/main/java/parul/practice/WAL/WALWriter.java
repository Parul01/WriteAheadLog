package parul.practice.WAL;

import java.io.*;
import java.nio.charset.StandardCharsets;

/*
WAL writer write WAL write ahead log  to the text file

 */
public class WALWriter {
    private final FileOutputStream fos;
    private BufferedWriter bw;

    public WALWriter(String path) throws IOException {
        this.fos = new FileOutputStream(path, true);
        this.bw  = new BufferedWriter(new OutputStreamWriter(fos, StandardCharsets.UTF_8));
    }

    private String escape(String value){
        if(value == null) return "";
        return value.replace("\\", "\\\\").replace("|", "\\|");
    }


    public void append(WALEntry entry) throws IOException {
        bw.write(entry.getTxnId() + "|" + entry.getSeqNo() + "|");

        bw.write(entry.getOperation()+"");

        bw.write("|");
        bw.write(escape(entry.getKey()));
        bw.write("|");
        bw.write(escape(entry.getValue()));
        bw.write("|" + entry.getCheckSum());

        bw.newLine();
        bw.flush();
        if (entry.getOperation() == OperationType.COMMIT) {
            fos.getChannel().force(false);            // page cache -> disk (durable)
        }

    }

    public void close() throws IOException {
        bw.close();
    }
}
