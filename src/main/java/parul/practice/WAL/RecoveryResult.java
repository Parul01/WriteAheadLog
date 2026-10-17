package parul.practice.WAL;

import java.util.List;

public record RecoveryResult(
        int firstValue,
        long secondValue,
        long validBytes,
        List<WALEntry> entries
) {
    @Override
    public long validBytes() {
        return validBytes;
    }
}