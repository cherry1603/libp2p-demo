# libp2p-demo

## Usage

```text
Start one node: com.dvbug.p2pchat.Chatter
<node1 log>: Node listened on /ip4/172.17.0.1/tcp/35833/ipfs/QmZPzjyL2eaHgUCJjRyUs57pPbP1hTFFmjC24vwdsjBN8z

Then start another node: com.dvbug.p2pchat.Chatter
<node2 log>: Node listened on /ip4/172.17.0.1/tcp/46135/ipfs/QmfCyVsVF8RHtJjQLTdb63vP9xf3UWnphKGtNiG1ZXQ55E

Then input "conn <node1 address>" command to connect node1 peer on console, like:

conn /ip4/172.17.0.1/tcp/35833/ipfs/QmZPzjyL2eaHgUCJjRyUs57pPbP1hTFFmjC24vwdsjBN8z

<node1 log>: Connected to new peer QmZPzjyL2eaHgUCJjRyUs57pPbP1hTFFmjC24vwdsjBN8z

Then input some text to send...
```