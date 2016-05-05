package org.bitcoins.script.locktime


import org.bitcoins.policy.Policy
import org.bitcoins.protocol.transaction.{Transaction, TransactionConstants, TransactionInput, UpdateTransactionInputs}
import org.bitcoins.script.result._
import org.bitcoins.script.{ExecutedScriptProgram, ExecutionInProgressScriptProgram, PreExecutionScriptProgram, ScriptProgram}
import org.bitcoins.script.constant.{OP_0, ScriptNumber}
import org.bitcoins.util.{ScriptProgramTestUtil, TestUtil}
import org.scalatest.{FlatSpec, MustMatchers}

/**
 * Created by chris on 3/30/16.
 */
class LockTimeInterpreterTest extends FlatSpec with MustMatchers with LockTimeInterpreter {

  "LockTimeInterpreter" must "mark the transaction invalid if the stack is empty" in {
    val stack = Seq()
    val script = Seq(OP_CHECKLOCKTIMEVERIFY)
    val program = ScriptProgram(TestUtil.testProgramExecutionInProgress,stack,script)
    val newProgram = ScriptProgramTestUtil.toExecutedScriptProgram(opCheckLockTimeVerify(program))
    newProgram.error must be (Some(ScriptErrorInvalidStackOperation))
  }

  it must "mark the transaction invalid if the transaction's sequence number is set to the max" in {
    val stack = Seq(OP_0)
    val script = Seq(OP_CHECKLOCKTIMEVERIFY)
    val program = ScriptProgram(TestUtil.testProgramExecutionInProgress,stack,script)
    val newProgram = ScriptProgramTestUtil.toExecutedScriptProgram(opCheckLockTimeVerify(program))
    newProgram.error must be (Some(ScriptErrorUnsatisfiedLocktime))
  }

  it must "mark the transaction as invalid if the stack top is negative" in {
    val stack = Seq(ScriptNumber(-1))
    val script = Seq(OP_CHECKLOCKTIMEVERIFY)
    val txInputAdjustedSequenceNumber = TransactionInput(TestUtil.transaction.inputs(0),0)
    val txAdjustedSequenceNumber = Transaction(TestUtil.transaction,UpdateTransactionInputs(Seq(txInputAdjustedSequenceNumber)))
    val adjustedLockTimeTx = Transaction(txAdjustedSequenceNumber,0)
    val baseProgram = ScriptProgram(adjustedLockTimeTx,TestUtil.testProgram.txSignatureComponent.scriptPubKey,
      TestUtil.testProgram.txSignatureComponent.inputIndex,TestUtil.testProgram.flags)
    val program = ScriptProgramTestUtil.toPreExecutionScriptProgram(ScriptProgram(baseProgram,stack,script))
    val newProgram = ScriptProgramTestUtil.toExecutedScriptProgram(opCheckLockTimeVerify(ScriptProgram.toExecutionInProgress(program)))
    newProgram.error must be (Some(ScriptErrorNegativeLockTime))
  }

  it must "mark the transaction as invalid if the locktime on the tx is < 500000000 && stack top is >= 500000000" in {
    val stack = Seq(ScriptNumber(500000000))
    val script = Seq(OP_CHECKLOCKTIMEVERIFY)
    val txInputAdjustedSequenceNumber = TransactionInput(TestUtil.transaction.inputs(0),0)
    val txAdjustedSequenceNumber = Transaction(TestUtil.transaction,UpdateTransactionInputs(Seq(txInputAdjustedSequenceNumber)))
    val adjustedLockTimeTx = Transaction(txAdjustedSequenceNumber,0)
    val baseProgram = ScriptProgram(adjustedLockTimeTx,TestUtil.testProgram.txSignatureComponent.scriptPubKey,
      TestUtil.testProgram.txSignatureComponent.inputIndex,TestUtil.testProgram.flags)
    val program = ScriptProgramTestUtil.toPreExecutionScriptProgram(ScriptProgram(baseProgram,stack,script))
    val newProgram = ScriptProgramTestUtil.toExecutedScriptProgram(opCheckLockTimeVerify(ScriptProgram.toExecutionInProgress(program)))
    newProgram.error must be (Some(ScriptErrorUnsatisfiedLocktime))
  }

  it must "mark the transaction as invalid if the locktime on the tx is >= 500000000 && stack top is < 500000000" in {
    val stack = Seq(ScriptNumber(499999999))
    val script = Seq(OP_CHECKLOCKTIMEVERIFY)
    val txInputAdjustedSequenceNumber = TransactionInput(TestUtil.transaction.inputs(0),0)
    val txAdjustedSequenceNumber = Transaction(TestUtil.transaction,UpdateTransactionInputs(Seq(txInputAdjustedSequenceNumber)))
    val adjustedLockTimeTx = Transaction(txAdjustedSequenceNumber,500000000)
    val baseProgram = ScriptProgram(adjustedLockTimeTx,TestUtil.testProgram.txSignatureComponent.scriptPubKey,
      TestUtil.testProgram.txSignatureComponent.inputIndex,TestUtil.testProgram.flags)
    val program = ScriptProgramTestUtil.toPreExecutionScriptProgram(ScriptProgram(baseProgram,stack,script))
    val newProgram = ScriptProgramTestUtil.toExecutedScriptProgram(opCheckLockTimeVerify(ScriptProgram.toExecutionInProgress(program)))
    newProgram.error must be (Some(ScriptErrorUnsatisfiedLocktime))
  }

  it must "mark the transaction as valid if the locktime on the tx is < 500000000 && stack top is < 500000000" in {
    val stack = Seq(ScriptNumber(499999999))
    val script = Seq(OP_CHECKLOCKTIMEVERIFY)
    val txInputAdjustedSequenceNumber = TransactionInput(TestUtil.transaction.inputs(0),0)
    val txAdjustedSequenceNumber = Transaction(TestUtil.transaction,UpdateTransactionInputs(Seq(txInputAdjustedSequenceNumber)))
    val adjustedLockTimeTx = Transaction(txAdjustedSequenceNumber,0)
    val baseProgram = ScriptProgram(adjustedLockTimeTx,TestUtil.testProgram.txSignatureComponent.scriptPubKey,
      TestUtil.testProgram.txSignatureComponent.inputIndex,TestUtil.testProgram.flags)
    val program = ScriptProgram(baseProgram,stack,script)
    val newProgram = opCheckLockTimeVerify(program)
    //if an error is hit, the newProgram will be an instance of ExecutedScriptProgram
    //if an error is not hit it will still be a ExecutionInProgressScriptProgram
    newProgram.isInstanceOf[ExecutedScriptProgram] must be (false)
  }

  it must "mark the transaction as valid if the locktime on the tx is >= 500000000 && stack top is >= 500000000" in {
    val stack = Seq(ScriptNumber(500000000))
    val script = Seq(OP_CHECKLOCKTIMEVERIFY)
    val txInputAdjustedSequenceNumber = TransactionInput(TestUtil.transaction.inputs(0),0)
    val txAdjustedSequenceNumber = Transaction(TestUtil.transaction,UpdateTransactionInputs(Seq(txInputAdjustedSequenceNumber)))
    val adjustedLockTimeTx = Transaction(txAdjustedSequenceNumber,500000000)
    val baseProgram : PreExecutionScriptProgram = ScriptProgram(adjustedLockTimeTx,TestUtil.testProgram.txSignatureComponent.scriptPubKey,
      TestUtil.testProgram.txSignatureComponent.inputIndex,TestUtil.testProgram.flags)
    val program = ScriptProgram(baseProgram,stack,script)
    val newProgram = opCheckLockTimeVerify(program)
    //if an error is hit, the newProgram will be an instance of ExecutedScriptProgram
    //if an error is not hit it will still be a ExecutionInProgressScriptProgram
    newProgram.isInstanceOf[ExecutedScriptProgram] must be (false)
  }

  it must "mark the script as invalid for OP_CHECKSEQUENCEVERIFY if there are no tokens on the stack" in {
    val stack = List()
    val script = List(OP_CHECKSEQUENCEVERIFY)
    val program = ScriptProgram(TestUtil.testProgramExecutionInProgress,stack,script)
    val newProgram = opCheckSequenceVerify(program)
    newProgram.isInstanceOf[ExecutedScriptProgram] must be (true)
    newProgram.asInstanceOf[ExecutedScriptProgram].error must be (Some(ScriptErrorInvalidStackOperation))
  }

  it must "mark the script as invalid for OP_CHECKSEQUENCEVERIFY if the stack top is negative" in {
    val stack = List(ScriptNumber.negativeOne)
    val script = List(OP_CHECKSEQUENCEVERIFY)
    val program = ScriptProgram(TestUtil.testProgramExecutionInProgress,stack,script)
    val newProgram = opCheckSequenceVerify(program)
    newProgram.isInstanceOf[ExecutedScriptProgram] must be (true)
    newProgram.asInstanceOf[ExecutedScriptProgram].error must be (Some(ScriptErrorNegativeLockTime))
  }

  it must "mark the script as invalid if we are requiring minimal encoding of numbers and the stack top is not minimal" in {
    val stack = List(ScriptNumber("0100"))
    val script = List(OP_CHECKSEQUENCEVERIFY)
    val program = ScriptProgram(TestUtil.testProgramExecutionInProgress,stack,script)
    val newProgram = opCheckSequenceVerify(program)
    newProgram.isInstanceOf[ExecutedScriptProgram] must be (true)
    newProgram.asInstanceOf[ExecutedScriptProgram].error must be (Some(ScriptErrorUnknownError))
  }

  it must "treat OP_CHECKSEQUENCEVERIFY as a NOP if the locktime disabled flag is set in the sequence number" in {
    val stack = List(ScriptNumber(TransactionConstants.locktimeDisabledFlag))
    val script = List(OP_CHECKSEQUENCEVERIFY)
    val program = ScriptProgram(TestUtil.testProgramExecutionInProgress,stack,script)
    val newProgram = opCheckSequenceVerify(program)
    newProgram.stack must be (stack)
    newProgram.script.isEmpty must be (true)
  }
}

