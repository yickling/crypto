const {Command, flags} = require('@oclif/command')
const createHash = require('create-hash');
const hdkey = require('hdkey')
const bs58check = require('bs58check')
const ethUtil = require('ethereumjs-util')

const generateWallet = async (log, seed, index) => {
  log('generating ETH wallet')

  const root = hdkey.fromMasterSeed(Buffer.from(seed, 'hex'));
  const addrNode = root.derive(`m/44'/60'/0'/0/${index}`);
  const pubKey = ethUtil.privateToPublic(addrNode._privateKey);
  const addr = ethUtil.publicToAddress(pubKey).toString('hex');
  const address = ethUtil.toChecksumAddress(`0x${addr}`);
  log(`private key: ${addrNode._privateKey.toString('hex')}`)
  log(`address: ${address}`)
  return address
}

class ETHCommand extends Command {
  async run() {
    const {flags} = this.parse(ETHCommand)
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

ETHCommand.description = `Generate an Ethereum wallet address and master private key
...
Extra documentation goes here
`

ETHCommand.flags = {
  seed: flags.string({char: 's', description: 'master seed'}),
  index: flags.string({char: 'i', description: 'index'}),
}

module.exports = ETHCommand
