package parul.practice.WAL;

import java.io.IOException;

public class KVStoreTester {
    public static void main(String[] args) throws IOException {
        // RUN 1 — write data
        System.out.println("=== RUN 1: Writing data ===");
        SingleNodeKVStore store = new SingleNodeKVStore();
        store.put("name", "Alice");
        store.put("city", "Delhi");
        store.put("age", "28");
        System.out.println("Written: name=Alice, city=Delhi, age=28");
        // crash — no cleanup

        // RUN 2 — restart, verify
        System.out.println("\n=== RUN 2: After restart ===");
        SingleNodeKVStore store2 = new SingleNodeKVStore();
        System.out.println("name: " + store2.get("name"));  // Alice
        System.out.println("city: " + store2.get("city"));  // Delhi
        System.out.println("age:  " + store2.get("age"));   // 28

        // RUN 3 — delete and verify
        store2.delete("city");
        System.out.println("\n=== After deleting city ===");

        SingleNodeKVStore store3 = new SingleNodeKVStore();
        System.out.println("city: " + store3.get("city"));  // null
        System.out.println("name: " + store3.get("name"));  // Alice

    }
}
