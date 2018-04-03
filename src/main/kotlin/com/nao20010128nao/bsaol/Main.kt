package com.nao20010128nao.bsaol

import com.google.common.primitives.UnsignedLong
import com.neovisionaries.ws.client.WebSocketFactory
import cy.agorise.graphenej.Asset
import cy.agorise.graphenej.AssetAmount
import cy.agorise.graphenej.Transaction
import cy.agorise.graphenej.UserAccount
import cy.agorise.graphenej.api.TransactionBroadcastSequence
import cy.agorise.graphenej.interfaces.WitnessResponseListener
import cy.agorise.graphenej.models.BaseResponse
import cy.agorise.graphenej.models.WitnessResponse
import cy.agorise.graphenej.operations.LimitOrderCreateOperation
import org.bitcoinj.core.DumpedPrivateKey

object Main {
    const val oneMona = 1000000
    const val quoteMonaBase = 860000
    const val oneMonaUnit = 10000

    val bts = Asset("1.3.0")
    val zeny = Asset("1.3.2481")
    val mona = Asset("1.3.2570")

    val oneHundredZeny = AssetAmount(UnsignedLong.valueOf(100_000000), zeny)

    val lesmiAccount = UserAccount("1.2.519685", "nao20010128nao")

    const val tenYearsInSecond = 10 * 365 * 86400
    val afterTenYrs: Long
        get() =
            System.currentTimeMillis() / 1000 + tenYearsInSecond

    val lesmiKey = DumpedPrivateKey.fromBase58(null, "").key

    @JvmStatic
    fun main(args: Array<String>) {
        val seq = run {
            val start = quoteMonaBase
            val step = oneMonaUnit
            generateSequence { 0 }
                    .take(10)
                    .mapIndexed { index, _ -> start + step * index }
        }
        println("Creating operations")
        val ops = seq
                .map { UnsignedLong.valueOf(it.toLong()) }
                .map {
                    LimitOrderCreateOperation(
                            lesmiAccount,
                            oneHundredZeny,
                            AssetAmount(it, mona),
                            afterTenYrs.toInt(),
                            false
                    )
                }
                .toList()
        println("Creating transaction")
        val tx = Transaction(lesmiKey, null, ops)
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