const bitsharesjs = require('bitsharesjs');
const TransactionBuilder = bitsharesjs.TransactionBuilder;
const Apis = bitsharesjs.Apis;
const AccountLogin = require('bitsharesjs/dist/chain/src/AccountLogin');
const data = require('./data');
const tx = data.params[2][0];

const builder = new TransactionBuilder();
Object.assign(builder, tx, {
    signatures: []
});
const login = AccountLogin;

(async () => {
    await Apis.instance("wss://japan.bitshares.apasia.tech/ws", true).init_promise;
    const {
        privKeys,
        pubKeys
    } = login.generateKeys("nao20010128nao", "rRhpLKm3qnVRPCjiKMcfkzYPX3fsHmwr9fvcigNunxvvT4NFCnJNCksRfveY");

})().then(console.log, console.log);