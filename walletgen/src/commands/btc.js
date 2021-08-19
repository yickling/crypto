const {Command, flags} = require('@oclif/command')
const createHash = require('create-hash');
const hdkey = require('hdkey')
const bs58check = require('bs58check')

const generateWallet = async (log, seed, index) => {
  log('generating BTC wallet')

  const root = hdkey.fromMasterSeed(Buffer.from(seed, 'hex'));
  // const masterPrivateKey = root.privateKey.toString('hex');
  const addrnode = root.derive(`m/44'/0'/0'/0/${index}`);

  const step1 = addrnode._publicKey;
  const step2 = createHash('sha256').update(step1).digest();
  const step3 = createHash('rmd160').update(step2).digest();
  var step4 = Buffer.allocUnsafe(21);
  step4.writeUInt8(0x00, 0);
  step3.copy(step4, 1); //step4 now holds the extended RIPMD-160 result
  const step9 = bs58check.encode(step4);
  log(`address: ${step9}`)
  return step9
}

class BTCCommand extends Command {
  async run() {
    const {flags} = this.parse(BTCCommand)
    const seed = flags.seed

    if (!seed) {
      this.log("seed required.")
      return
    }

    const index = flags.index || 0
    // this.log(`hello ${name} from ./src/commands/hello.js`)
    generateWallet(this.log, seed, index)
  }
}

BTCCommand.description = `Generate a bitcoin wallet address and master private key
...
Extra documentation goes here
`

BTCCommand.flags = {
  seed: flags.string({char: 's', description: 'master seed'}),
  index: flags.string({char: 'i', description: 'index'}),
}

module.exports = BTCCommand
