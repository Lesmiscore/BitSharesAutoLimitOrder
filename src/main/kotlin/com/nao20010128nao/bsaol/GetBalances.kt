package com.nao20010128nao.bsaol

import com.google.common.primitives.UnsignedLong
import com.shopify.promises.Promise
import com.shopify.promises.then
import cy.agorise.graphenej.Asset
import cy.agorise.graphenej.AssetAmount
import cy.agorise.graphenej.UserAccount
import cy.agorise.graphenej.api.GetAccountBalances
import cy.agorise.graphenej.interfaces.WitnessResponseListener
import cy.agorise.graphenej.models.BaseResponse
import cy.agorise.graphenej.models.WitnessResponse

object GetBalances {

    val lesmiAccount = UserAccount("1.2.519685", "nao20010128nao")

    @JvmStatic
    fun main(args: Array<String>) {
        val lock = Any()
        requestAssetsPromise()
                .then { requestUserBalancePromise(it) }
                .then { sortPromise(it) }
                .then { removeZeroPromise(it) }
                .then { displayPromise(it) }
                .whenComplete {
                    synchronized(lock) {
                        lock.notify()
                    }
                }
        synchronized(lock) {
            lock.wait()
        }
    }

    fun requestUserBalancePromise(completeAsset: List<Asset> = emptyList()): Promise<List<AssetAmount>, Any?> {
        return Promise {
            onCancel { }
            val assetMap = completeAsset.map { it.objectId to it }.toMap()
            val ws = newWs()
            ws.addListener(GetAccountBalances(lesmiAccount, emptyList(), true, object : WitnessResponseListener {
                override fun onSuccess(p0: WitnessResponse<*>?) {
                    println("OK")
                    process((p0?.result as? List<AssetAmount>)
                            ?.map { it.also { it.asset = assetMap.getOrDefault(it.asset.objectId, it.asset) } })
                }

                override fun onError(p0: BaseResponse.Error?) {
                    process(null)
                }

                fun process(value: List<AssetAmount>?) {
                    if (value != null) {
                        resolve(value)
                    } else {
                        reject(value)
                    }
                }
            }))
            ws.connect()
        }
    }

    fun sortPromise(list: List<AssetAmount>): Promise<List<AssetAmount>, Any?> {
        return Promise {
            onCancel { }
            try {
                resolve(list.sortedWith(AssetAmountComparatorByAmount and AssetAmountComparatorByIds))
            } catch (e: Throwable) {
                reject(e)
            }
        }
    }

    fun removeZeroPromise(list: List<AssetAmount>): Promise<List<AssetAmount>, Any?> {
        return Promise {
            onCancel { }
            try {
                resolve(list.filter { it.amount != UnsignedLong.ZERO })
            } catch (e: Throwable) {
                reject(e)
            }
        }
    }

    fun displayPromise(list: List<AssetAmount>): Promise<List<AssetAmount>, Any?> {
        return Promise {
            list.forEach {
                println("%2.8f %s".format(it.displayAmount, it.asset.symbol))
            }
            resolve(list)
        }
    }
}