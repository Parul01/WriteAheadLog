# WriteAheadLog
Write Ahead log is popular approach used in various DBs and even operating system for crash recovery. 
### Problem Statement
- To recover from a crash which happened in middle of ongoing transaction or lost data in memory, we might need to redo the transactions logged in DB.
### Design
- Maintaining a log file for all transactions can help system to perform actions that were left pending but were logged
- Creating a key value store
- We are logging all types of transaction - what a real system also does
- Format
```text
Transaction id|Sequence no|Operation|Key|value|checksum
3|5|PUT|age|28|799608703
```
- Incase a log entry itself is corrupted we are computing the entry checksum using CRC and confirm checksum before applying transaction
- WALEntry is a Pojo to store a single wal entry
- all operations in a transaction end with COMMIT wal entry
- On a system startup - we replay WAL logs after a checkpoint and process these transactions in a sequence
- (Note: checkpoint is not implemented in this but real system does that)
- log file generated from our test
```text
1|1|PUT|Name|Anthony|1698605354
1|2|PUT|Age|31|507344988
1|3|PUT|Email|anthony@gmail.com|-56105752
1|4|COMMIT|||311932553
2|5|PUT|Name|Bob|561619137
2|6|PUT|Age|25|518117212

```

- Handling corrupt WAL - output
```text
Reading line: 1|1|PUT|Name|Anthony|1698605354
Fields count: 6
Stored: 1698605354 Computed: 1698605354
Reading line: 1|2|PUT|Age|31|507344988
Fields count: 6
Stored: 507344988 Computed: 507344988
Reading line: 1|3|PUT|Email|anthony@gmail.com|-56105752
Fields count: 6
Stored: -56105752 Computed: -56105752
Reading line: 1|4|COMMIT|||311932553
Fields count: 6
Stored: 311932553 Computed: 311932553
Reading line: 2|5|PUT|Name|Bob|561619137
Fields count: 6
Stored: 561619137 Computed: 561619137
Reading line: 2|6|PUT|Age|25|518117212
Fields count: 6
Stored: 518117212 Computed: 518117212
Reading line: 2|
Fields count: 2
Committed txns: [1]
Pending txns: [1]
On startup !!!!!
Torn/corrupt WAL tail, stopping: 2|
[WALEntry{txnId='1', seqNo='1', operation=PUT, key='Name', value='Anthony', checkSum='1698605354'}, WALEntry{txnId='1', seqNo='2', operation=PUT, key='Age', value='31', checkSum='507344988'}, WALEntry{txnId='1', seqNo='3', operation=PUT, key='Email', value='anthony@gmail.com', checkSum='-56105752'}]
Email anthony@gmail.com
Age 31
Name Hailey
```

