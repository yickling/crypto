const {Command, flags} = require('@oclif/command')
const bip39 = require('bip39')
const hdkey = require('hdkey')

const generateWallet = async (log) => {
  const mnemonic = bip39.generateMnemonic(); //generates string
  const seed = await bip39.mnemonicToSeed(mnemonic); //creates seed buffer
  const root = hdkey.fromMasterSeed(seed);
  const masterPrivateKey = root.privateKey.toString('hex');
  const masterPublicKey = root.publicKey.toString('hex');
  log('generating new seed')
  log(`mnemonic: ${mnemonic}`)
  log(`seed: ${seed.toString('hex')}`)
  log(`masterPrivKey: ${masterPrivateKey}`)
  log(`masterPubKey: ${masterPublicKey}`)
  return seed
}

class NewSeedCommand extends Command {
  async run() {
    generateWallet(this.log)
  }
}

NewSeedCommand.description = `Generate a wallet seed and master private key
`

NewSeedCommand.flags = {
}

module.exports = NewSeedCommand
