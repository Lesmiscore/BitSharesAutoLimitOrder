package com.nao20010128nao.bsaol

import com.google.common.primitives.UnsignedLong
import cy.agorise.graphenej.AssetAmount
import cy.agorise.graphenej.Transaction
import cy.agorise.graphenej.api.TransactionBroadcastSequence
import cy.agorise.graphenej.interfaces.WitnessResponseListener
import cy.agorise.graphenej.models.BaseResponse
import cy.agorise.graphenej.models.WitnessResponse
import cy.agorise.graphenej.operations.LimitOrderCreateOperation
import java.math.MathContext

object Main {
    const val oneMona = 1000000
    const val quoteMonaBase = 1300000
    const val oneMonaUnit = 10000

    val twoHundredZeny: AssetAmount
        get() = AssetAmount(UnsignedLong.valueOf(200_000000), zeny)

    const val tenYearsInSecond = 10 * 365 * 86400
    val afterTenYrs: Long
        get() = System.currentTimeMillis() / 1000 + tenYearsInSecond

    @JvmStatic
    fun main(args: Array<String>) {
        val seq = run {
            val start = quoteMonaBase
            val step = oneMonaUnit
            generateSequence { 0 }
                    .take(11)
                    .mapIndexed { index, _ -> start + step * index }
                    .drop(1)
        }
        println("Creating operations")
        val ops = seq
                .map { UnsignedLong.valueOf(it.toLong()) }
                .map {
                    LimitOrderCreateOperation(
                            lesmiAccount,
                            twoHundredZeny,
                            AssetAmount(it, mona),
                            afterTenYrs.toInt(),
                            false
                    )
                }
                .toList()
        println("Creating transaction")
        val tx = Transaction(lesmiKey, null, ops)
        println("Num of ops: " + ops.size)
        ops.forEach {
            println("%.10f".format(it.minToReceive.amount.toBigDecimal().divide(it.amountToSell.amount.toBigDecimal(), MathContext.UNLIMITED)))
        }
        //Scanner(System.`in`).nextLine()
        //return
        println("Uploading transaction")
        val obj = Any()
        val ws = newWs()
        ws.addListener(TransactionBroadcastSequence(tx, bts, object : WitnessResponseListener {
            override fun onSuccess(p0: WitnessResponse<*>?) {
                synchronized(obj) {
                    obj.notify()
                }
                println("OK")
                println(p0?.result)
                System.exit(0)
            }

            override fun onError(p0: BaseResponse.Error?) {
                synchronized(obj) {
                    obj.notify()
                }
                println("NG")
                println(p0)
                System.exit(1)
            }
        }))
        ws.connect()
        synchronized(obj) {
            obj.wait()
        }
    }
}