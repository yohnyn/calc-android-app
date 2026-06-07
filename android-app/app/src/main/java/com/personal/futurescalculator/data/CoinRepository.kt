package com.personal.futurescalculator.data

import android.content.Context
import android.graphics.BitmapFactory
import com.personal.futurescalculator.model.CoinAsset
import java.math.BigDecimal
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONArray
import org.json.JSONObject

class CoinRepository(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
    private val iconDirectory = File(context.filesDir, "coin_icons").apply { mkdirs() }

    fun loadCachedCoins(): List<CoinAsset> = parseCoins(preferences.getString(KEY_COINS, null))
    fun loadCustomCoins(): List<CoinAsset> = parseCoins(preferences.getString(KEY_CUSTOM_COINS, null))
    fun loadSelectedCoinId(): String = preferences.getString(KEY_SELECTED, "bitcoin") ?: "bitcoin"
    fun loadUpdatedAt(): Long? = preferences.getLong(KEY_UPDATED_AT, 0L).takeIf { it > 0L }

    fun saveSelectedCoin(id: String) {
        preferences.edit().putString(KEY_SELECTED, id).apply()
    }

    fun saveCustomCoins(coins: List<CoinAsset>) {
        preferences.edit().putString(KEY_CUSTOM_COINS, serializeCoins(coins)).apply()
    }

    fun fetchTopCoins(): List<CoinAsset> {
        val connection = URL(COINGECKO_MARKETS_URL).openConnection() as HttpURLConnection
        connection.connectTimeout = 10_000
        connection.readTimeout = 15_000
        connection.requestMethod = "GET"
        connection.setRequestProperty("Accept", "application/json")
        connection.setRequestProperty("User-Agent", "FuturesCalculator/0.1")
        return try {
            if (connection.responseCode !in 200..299) error("HTTP ${connection.responseCode}")
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val coins = parseMarketResponse(body).map(::withAvailableLocalIcon)
            preferences.edit()
                .putString(KEY_COINS, serializeCoins(coins))
                .putLong(KEY_UPDATED_AT, System.currentTimeMillis())
                .apply()
            coins
        } finally {
            connection.disconnect()
        }
    }

    private fun parseMarketResponse(body: String): List<CoinAsset> {
        val array = JSONArray(body)
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                val id = item.getString("id")
                val symbol = item.getString("symbol").uppercase()
                val name = item.getString("name")
                if (isStableCoin(id, symbol, name)) continue
                val price = item.optString("current_price").toBigDecimalOrNull() ?: continue
                add(
                    CoinAsset(
                        id = id,
                        symbol = symbol,
                        name = name,
                        priceUsdt = price,
                        marketRank = item.optInt("market_cap_rank").takeIf { it > 0 },
                        iconUrl = item.optString("image").takeIf { it.isNotBlank() },
                        iconResourceName = BUILT_IN_ICON_RESOURCES[symbol]
                    )
                )
                if (size == COIN_LIMIT) break
            }
        }
    }

    private fun parseCoins(value: String?): List<CoinAsset> {
        if (value.isNullOrBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(value)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    val coin = CoinAsset(
                            id = item.getString("id"),
                            symbol = item.getString("symbol"),
                            name = item.getString("name"),
                            priceUsdt = item.getString("price").toBigDecimal(),
                            isCustom = item.optBoolean("custom", false),
                            marketRank = item.optInt("rank").takeIf { it > 0 },
                            iconUrl = item.optString("icon_url").takeIf { it.isNotBlank() },
                            iconPath = item.optString("icon_path").takeIf { it.isNotBlank() && File(it).exists() },
                            iconResourceName = item.optString("icon_resource_name").takeIf { it.isNotBlank() }
                        )
                    if (coin.isCustom || !isStableCoin(coin.id, coin.symbol, coin.name)) add(withAvailableLocalIcon(coin))
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun serializeCoins(coins: List<CoinAsset>): String {
        val array = JSONArray()
        coins.forEach { coin ->
            array.put(
                JSONObject()
                    .put("id", coin.id)
                    .put("symbol", coin.symbol)
                    .put("name", coin.name)
                    .put("price", coin.priceUsdt.toPlainString())
                    .put("custom", coin.isCustom)
                    .put("rank", coin.marketRank)
                    .put("icon_url", coin.iconUrl)
                    .put("icon_path", coin.iconPath)
                    .put("icon_resource_name", coin.iconResourceName)
            )
        }
        return array.toString()
    }

    private fun withAvailableLocalIcon(coin: CoinAsset): CoinAsset {
        val builtInResource = BUILT_IN_ICON_RESOURCES[coin.symbol.uppercase()]
        if (builtInResource != null) {
            return coin.copy(iconResourceName = builtInResource, iconPath = null)
        }
        return coin.withExistingIcon()
    }

    private fun CoinAsset.withExistingIcon(): CoinAsset {
        val file = iconFile(id)
        return if (file.exists()) copy(iconPath = file.absolutePath) else copy(iconPath = null)
    }

    fun loadIconForCoin(coin: CoinAsset): CoinAsset {
        if (coin.isCustom || coin.iconResourceName != null) return coin
        if (coin.id in loadFailedIconIds()) return coin.withExistingIcon()
        return downloadIcon(coin)
    }

    private fun downloadIcon(coin: CoinAsset): CoinAsset {
        val url = coin.iconUrl ?: fetchCoinIconUrl(coin.id) ?: return coin.withExistingIcon()
        val file = iconFile(coin.id)
        if (file.exists() && file.length() > 0L && BitmapFactory.decodeFile(file.absolutePath) != null) {
            return coin.copy(iconUrl = url, iconPath = file.absolutePath)
        }
        file.delete()
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 10_000
        connection.readTimeout = 15_000
        connection.requestMethod = "GET"
        return try {
            if (connection.responseCode !in 200..299) return coin.markIconFailed()
            val bytes = connection.inputStream.use { it.readBytes() }
            if (bytes.isEmpty() || BitmapFactory.decodeByteArray(bytes, 0, bytes.size) == null) {
                return coin.copy(iconUrl = url).markIconFailed()
            }
            file.writeBytes(bytes)
            coin.copy(iconUrl = url, iconPath = file.absolutePath)
        } catch (_: Exception) {
            file.delete()
            coin.markIconFailed()
        } finally {
            connection.disconnect()
        }
    }

    private fun CoinAsset.markIconFailed(): CoinAsset {
        saveFailedIconIds(loadFailedIconIds() + id)
        return copy(iconPath = null)
    }

    private fun loadFailedIconIds(): Set<String> = preferences.getStringSet(KEY_FAILED_ICON_IDS, emptySet()).orEmpty()

    private fun saveFailedIconIds(ids: Set<String>) {
        preferences.edit().putStringSet(KEY_FAILED_ICON_IDS, ids).apply()
    }

    private fun fetchCoinIconUrl(id: String): String? {
        val connection = URL("$COINGECKO_COIN_URL/$id?localization=false&tickers=false&market_data=false&community_data=false&developer_data=false").openConnection() as HttpURLConnection
        connection.connectTimeout = 10_000
        connection.readTimeout = 15_000
        connection.requestMethod = "GET"
        connection.setRequestProperty("Accept", "application/json")
        connection.setRequestProperty("User-Agent", "FuturesCalculator/0.1")
        return try {
            if (connection.responseCode !in 200..299) return null
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            JSONObject(body).optJSONObject("image")?.optString("small")?.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        } finally {
            connection.disconnect()
        }
    }

    private fun iconFile(id: String): File = File(iconDirectory, "${id.replace(Regex("[^A-Za-z0-9_-]"), "_")}.img")

    private fun isStableCoin(id: String, symbol: String, name: String): Boolean {
        val normalizedSymbol = symbol.uppercase()
        val normalizedName = name.lowercase()
        return id in STABLE_COIN_IDS ||
            normalizedSymbol in STABLE_COIN_SYMBOLS ||
            "USD" in normalizedSymbol ||
            "stablecoin" in normalizedName ||
            "stable coin" in normalizedName ||
            "dollar" in normalizedName
    }

    private companion object {
        const val PREFERENCES = "coin_prices"
        const val KEY_COINS = "coins"
        const val KEY_CUSTOM_COINS = "custom_coins"
        const val KEY_SELECTED = "selected_coin"
        const val KEY_UPDATED_AT = "updated_at"
        const val KEY_FAILED_ICON_IDS = "failed_icon_ids"
        const val COIN_LIMIT = 100
        const val COINGECKO_MARKETS_URL =
            "https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&order=market_cap_desc&per_page=150&page=1&sparkline=false&precision=full"
        const val COINGECKO_COIN_URL = "https://api.coingecko.com/api/v3/coins"

        val STABLE_COIN_SYMBOLS = setOf(
            "USDT", "USDC", "DAI", "FDUSD", "USDE", "USDS", "TUSD", "PYUSD", "USDD",
            "FRAX", "GUSD", "LUSD", "USD0", "USDB", "EURC", "EURT", "XAUT", "PAXG"
        )
        val STABLE_COIN_IDS = setOf(
            "tether", "usd-coin", "dai", "first-digital-usd", "ethena-usde", "usds",
            "true-usd", "paypal-usd", "usdd", "frax", "gemini-dollar", "liquity-usd",
            "usual-usd", "usdb", "euro-coin", "tether-eurt", "tether-gold", "pax-gold"
        )
        val BUILT_IN_ICON_RESOURCES = mapOf(
            "BTC" to "coin_btc",
            "ETH" to "coin_eth",
            "BNB" to "coin_bnb",
            "SOL" to "coin_sol",
            "XRP" to "coin_xrp",
            "DOGE" to "coin_doge",
            "ADA" to "coin_ada",
            "TRX" to "coin_trx",
            "LINK" to "coin_link",
            "AVAX" to "coin_avax",
            "TON" to "coin_ton",
            "SUI" to "coin_sui",
            "DOT" to "coin_dot",
            "SHIB" to "coin_shib",
            "BCH" to "coin_bch",
            "LTC" to "coin_ltc",
            "NEAR" to "coin_near",
            "APT" to "coin_apt",
            "HBAR" to "coin_hbar",
            "PEPE" to "coin_pepe"
        )
    }
}
