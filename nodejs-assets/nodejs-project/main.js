var rn_bridge = require('rn-bridge');
var fs = require('fs')

rn_bridge.channel.on('fd', ({fd}) => {
  const buf = fs.readFileSync(fd, { flag: 'r' })
  const str = buf.toString('utf8')
  rn_bridge.channel.post("read", str)
} );

// Inform react-native node is initialized.
rn_bridge.channel.send("Node was initialized.");
