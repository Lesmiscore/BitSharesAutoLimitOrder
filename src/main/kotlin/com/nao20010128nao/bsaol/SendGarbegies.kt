package com.nao20010128nao.bsaol

import com.shopify.promises.Promise
import com.shopify.promises.then
import cy.agorise.graphenej.Transaction
import cy.agorise.graphenej.UserAccount
import cy.agorise.graphenej.api.TransactionBroadcastSequence
import cy.agorise.graphenej.interfaces.WitnessResponseListener
import cy.agorise.graphenej.models.BaseResponse
import cy.agorise.graphenej.models.WitnessResponse
import cy.agorise.graphenej.operations.TransferOperationBuilder
import org.bitcoinj.core.ECKey
import kotlin.system.exitProcess

object SendGarbegies {
    @JvmStatic
    fun main(args: Array<String>) {
        requestAssetsPromise()
                .then { GetBalances.requestUserBalancePromise(it) }
                .then { GetBalances.sortPromise(it) }
                .then { GetBalances.removeZeroPromise(it) }
                .then { excludePromise(it, IndexedAssets) }
                .then { createTransfersPromise(it) }
                .then { packTxPromise(it) }
                .then { sendTx(it) }
                .whenComplete {
                    when (it) {
                        is Promise.Result.Success -> {
                            println(it.value)
                            exitProcess(0)
                        }
                        is Promise.Result.Error -> {
                            println(it.error)
                            println((it.error as? BaseResponse.Error)?.message)
                            exitProcess(1)
                        }
                        else -> exitProcess(2)
                    }
                }
    }

    fun excludePromise(self: AssetAmounts, target: Assets): AssetAmountsPromise {
        return Promise {
            onCancel { }
            resolve(self - target)
        }
    }

    fun createTransfersPromise(data: AssetAmounts, from: UserAccount = lesmiAccount, dest: UserAccount = lesmi3Account): Promising<Operations> {
        return Promise {
            onCancel { }
            data.map {
                TransferOperationBuilder()
                        .setTransferAmount(it)
                        .setSource(from)
                        .setDestination(dest)
                        .build()
            }.also { resolve(it) }
        }
    }

    fun packTxPromise(ops: Operations, key: ECKey = lesmiKey): Promising<Transaction> {
        return Promise {
            onCancel { }
            resolve(Transaction(key, null, ops))
        }
    }

    fun sendTx(tx: Transaction): Promising<Any?> {
        return Promise {
            val ws = newWs()
            ws.addListener(TransactionBroadcastSequence(tx, bts, object : WitnessResponseListener {
                override fun onSuccess(p0: WitnessResponse<*>?) {
                    resolve(p0?.result)
                    ws.disconnect()
                }

                override fun onError(p0: BaseResponse.Error?) {
                    reject(p0)
                    ws.disconnect()
                }
            }))
            ws.connect()
        }
    }
}