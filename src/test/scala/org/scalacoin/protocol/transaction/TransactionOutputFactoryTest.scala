package org.scalacoin.protocol.transaction

import org.scalacoin.currency.CurrencyUnits
import org.scalacoin.util.TestUtil
import org.scalatest.{MustMatchers, FlatSpec}

/**
 * Created by chris on 3/30/16.
 */
class TransactionOutputFactoryTest extends FlatSpec with MustMatchers {

  "TransactionOutputFactory" must "create a transaction output out of it's base components" in {
    val emptyTxOutput = TransactionOutputFactory.factory(EmptyTransactionOutput.value,EmptyTransactionOutput.scriptPubKey)
    emptyTxOutput.value must be (EmptyTransactionOutput.value)
    emptyTxOutput.scriptPubKey must be (EmptyTransactionOutput.scriptPubKey)
  }

  it must "modify the currency unit for a tx output" in {
    val newTxOutput = TransactionOutputFactory.factory(EmptyTransactionOutput,CurrencyUnits.oneSatoshi)
    newTxOutput.value must be (CurrencyUnits.oneSatoshi)
  }

  it must "modify the scriptPubKey for a tx output" in {
    val newTxOutput = TransactionOutputFactory.factory(EmptyTransactionOutput,TestUtil.scriptPubKey)
    newTxOutput.scriptPubKey must be (TestUtil.scriptPubKey)
  }
}
