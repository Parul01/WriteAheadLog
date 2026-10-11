package parul.practice.WAL;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/*
WAL writer write WAL write ahead log  to the text file

 */
public class WALWriter {

    BufferedWriter bw;

    public WALWriter(String path) throws IOException {
      this.bw = new BufferedWriter(new FileWriter(path, true));
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

    }

    public void close() throws IOException {
        bw.close();
    }
}
