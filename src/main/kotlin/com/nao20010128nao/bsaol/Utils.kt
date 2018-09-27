package com.nao20010128nao.bsaol

import com.google.common.primitives.UnsignedLong
import com.neovisionaries.ws.client.WebSocketFactory
import com.shopify.promises.Promise
import cy.agorise.graphenej.Asset
import cy.agorise.graphenej.AssetAmount
import cy.agorise.graphenej.BaseOperation
import cy.agorise.graphenej.UserAccount
import cy.agorise.graphenej.api.ListAssets
import cy.agorise.graphenej.interfaces.WitnessResponseListener
import cy.agorise.graphenej.models.BaseResponse
import cy.agorise.graphenej.models.WitnessResponse
import org.bitcoinj.core.DumpedPrivateKey
import java.math.BigDecimal
import kotlin.math.max

fun Any.wait() {
    (this as java.lang.Object).wait()
}

fun Any.notify() {
    (this as java.lang.Object).notifyAll()
}

fun UnsignedLong.toBigDecimal() = bigIntegerValue().toBigDecimal()

fun newWs() = WebSocketFactory().createSocket("wss://japan.bitshares.apasia.tech/ws")

object AssetComparatorByIds : Comparator<Asset> {
    override fun compare(p0: Asset?, p1: Asset?): Int {
        val firstNumber = p0?.objectId?.split(".")?.last()?.toIntOrNull()
        val secondNumber = p1?.objectId?.split(".")?.last()?.toIntOrNull()
        return when {
            firstNumber != null && secondNumber == null -> 1
            firstNumber == null && secondNumber != null -> -1
            firstNumber == null && secondNumber == null -> 0
            else -> firstNumber!! - secondNumber!!
        }
    }
}

object AssetAmountComparatorByIds : Comparator<AssetAmount> {
    override fun compare(first: AssetAmount?, second: AssetAmount?): Int {
        return when {
            first != null && second == null -> 1
            first == null && second != null -> -1
            first == null && second == null -> 0
            else -> AssetComparatorByIds.compare(first?.asset, second?.asset)
        }
    }
}

object AssetAmountComparatorByAmount : Comparator<AssetAmount> {
    override fun compare(first: AssetAmount?, second: AssetAmount?): Int {
        return when {
            first != null && second == null -> 1
            first == null && second != null -> -1
            first == null && second == null -> 0
            else -> -first!!.displayAmount.compareTo(second!!.displayAmount)
        }
    }
}

class ChainedComparator<T>(private val comparators: List<Comparator<T>>) : Comparator<T> {
    override fun compare(p0: T, p1: T): Int = comparators.map { it.compare(p0, p1) }.firstOrNull { it != 0 } ?: 0
}

infix fun <T> Comparator<T>.and(other: Comparator<T>): Comparator<T> = ChainedComparator(listOf(this, other))

val ten: BigDecimal = BigDecimal.TEN

val AssetAmount.displayAmount: BigDecimal
    get() = amount.toBigDecimal().movePointLeft(max(0, asset.precision))

fun requestAssets(f: (List<Asset>?) -> Unit) {
    val ws = newWs()
    ws.addListener(ListAssets("", -1, true, object : WitnessResponseListener {
        override fun onSuccess(p0: WitnessResponse<*>?) {
            f(p0?.result as? List<Asset>)
        }

        override fun onError(p0: BaseResponse.Error?) {
            f(null)
        }
    }))
    ws.connect()
}

fun requestAssetsPromise(): Promise<List<Asset>, Any?> {
    return Promise {
        onCancel { }
        requestAssets {
            if (it != null) {
                resolve(it)
            } else {
                reject(it)
            }
        }
    }
}

typealias Assets = List<Asset>
typealias AssetAmounts = List<AssetAmount>
typealias Operations = List<BaseOperation>
typealias Promising<T> = Promise<T, Any?>
typealias AssetsPromise = Promising<Assets>
typealias AssetAmountsPromise = Promising<AssetAmounts>
typealias TerminatorPromise = Promising<Nothing?>

inline operator fun Assets.minus(lst: List<String>): Assets = filter { it.objectId !in lst }
@JvmName("removeAssetFromList")
inline operator fun Assets.minus(lst: Assets): Assets = this - lst.map { it.objectId }

@JvmName("removeAssetFromList2")
inline operator fun AssetAmounts.minus(lst: List<String>): AssetAmounts = filter { it.asset?.objectId !in lst }

@JvmName("removeAssetFromList3")
inline operator fun AssetAmounts.minus(lst: Assets): AssetAmounts = this - lst.map { it.objectId }

val bts = Asset("1.3.0")
val zeny = Asset("1.3.2481")
val mona = Asset("1.3.2570")
val btc = Asset("1.3.1570")
val bco = Asset("1.3.1564")
val vips = Asset("1.3.3842")
val neet = Asset("1.3.3401")

object IndexedAssets : Assets by listOf(btc, bts, bco, mona, zeny, vips, neet)

val lesmiKey = DumpedPrivateKey.fromBase58(null, "5JtvqYDt8pggwtsrgm8mAbtP7VUcuH4sX1BQ3sVjkFVKDh3TZuD").key

val lesmiAccount = UserAccount("1.2.519685", "nao20010128nao")
val lesmi3Account = UserAccount("1.2.1073871", "nao20010128nao3rd")
