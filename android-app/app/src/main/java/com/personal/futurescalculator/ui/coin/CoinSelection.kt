package com.personal.futurescalculator.ui.coin

import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.personal.futurescalculator.data.CoinRepository
import com.personal.futurescalculator.model.CoinAsset
import com.personal.futurescalculator.ui.CompactTextInput
import com.personal.futurescalculator.ui.DropdownChevronIcon
import com.personal.futurescalculator.ui.NumberInput
import java.math.BigDecimal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun CoinMarketHeader(
    coin: CoinAsset?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(52.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CoinIcon(coin = coin, size = 34)
            Column(
                modifier = Modifier.weight(1f).padding(start = 10.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(
                    text = "币种",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = coin?.symbol ?: "选择币种",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            HeaderChevron()
        }
    }
}

@Composable
private fun HeaderChevron() {
    Box(modifier = Modifier.size(20.dp), contentAlignment = Alignment.Center) {
        DropdownChevronIcon(iconSize = 18.dp)
    }
}

@Composable
fun CoinIcon(coin: CoinAsset?, size: Int) {
    val context = LocalContext.current
    val repository = remember(context) { CoinRepository(context) }
    var lazyIconPath by remember(coin?.id, coin?.iconPath) { mutableStateOf(coin?.iconPath) }
    val resourceId = remember(coin?.iconResourceName) {
        coin?.iconResourceName?.let { name ->
            context.resources.getIdentifier(name, "drawable", context.packageName)
        } ?: 0
    }
    val bitmap = remember(lazyIconPath) {
        lazyIconPath?.let { path -> BitmapFactory.decodeFile(path)?.asImageBitmap() }
    }

    LaunchedEffect(coin?.id, coin?.iconResourceName, lazyIconPath) {
        val current = coin ?: return@LaunchedEffect
        if (current.iconResourceName == null && lazyIconPath == null && !current.isCustom) {
            val loaded = withContext(Dispatchers.IO) { repository.loadIconForCoin(current) }
            lazyIconPath = loaded.iconPath
        }
    }

    if (resourceId != 0) {
        Box(
            modifier = Modifier.requiredSize(size.dp).clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = androidx.compose.ui.res.painterResource(id = resourceId),
                contentDescription = coin?.symbol,
                modifier = Modifier.requiredSize(size.dp),
                contentScale = ContentScale.Fit
            )
        }
    } else if (bitmap != null) {
        Box(
            modifier = Modifier.requiredSize(size.dp).clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = bitmap,
                contentDescription = coin?.symbol,
                modifier = Modifier.requiredSize(size.dp),
                contentScale = ContentScale.Fit
            )
        }
    } else {
        Surface(
            modifier = Modifier.requiredSize(size.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = coin?.symbol?.take(1) ?: "?",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CoinSelectorDialog(
    coins: List<CoinAsset>,
    selectedCoinId: String,
    onSelect: (String) -> Unit,
    onAddCustom: (String, BigDecimal) -> Unit,
    onDeleteCustom: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var search by remember { mutableStateOf("") }
    var showCustomForm by remember { mutableStateOf(false) }
    var customSymbol by remember { mutableStateOf("") }
    var customPrice by remember { mutableStateOf<BigDecimal?>(null) }
    val filtered = coins.filter {
        search.isBlank() ||
            it.symbol.contains(search, ignoreCase = true) ||
            it.name.contains(search, ignoreCase = true)
    }

    if (showCustomForm) {
        CustomCoinDialog(
            symbol = customSymbol,
            price = customPrice,
            onSymbolChange = { customSymbol = it },
            onPriceChange = { customPrice = it },
            onSave = {
                onAddCustom(customSymbol, customPrice!!)
                customSymbol = ""
                customPrice = null
                showCustomForm = false
            },
            onDismiss = {
                customSymbol = ""
                customPrice = null
                showCustomForm = false
            }
        )
        return
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().widthIn(max = 440.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("选择币种", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("搜索币种") },
                    singleLine = true
                )
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filtered, key = { it.id }) { coin ->
                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable { onSelect(coin.id) },
                            shape = MaterialTheme.shapes.small,
                            color = if (coin.id == selectedCoinId) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.50f)
                            }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CoinIcon(coin = coin, size = 30)
                                    Text(coin.symbol, fontWeight = FontWeight.SemiBold)
                                }
                                if (coin.isCustom) {
                                    TextButton(
                                        onClick = { onDeleteCustom(coin.id) },
                                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                    ) { Text("删除", fontWeight = FontWeight.SemiBold) }
                                }
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    TextButton(onClick = { showCustomForm = true }) {
                        Text("添加自定义币种", fontWeight = FontWeight.SemiBold)
                    }
                    TextButton(onClick = onDismiss) { Text("关闭", fontWeight = FontWeight.SemiBold) }
                }
            }
        }
    }
}

@Composable
private fun CustomCoinDialog(
    symbol: String,
    price: BigDecimal?,
    onSymbolChange: (String) -> Unit,
    onPriceChange: (BigDecimal?) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val canSave = symbol.isNotBlank() && price != null && price > BigDecimal.ZERO

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 400.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.22f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "添加自定义币种",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "自定义币种与价格仅保存在本地设备。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                CompactTextInput(
                    value = symbol,
                    onValueChange = onSymbolChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = "币种名称，例如 ABC"
                )
                NumberInput(
                    value = price,
                    onValueChange = onPriceChange,
                    label = "币种价格 USDT"
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消", fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = onSave,
                        enabled = canSave,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text("保存并使用")
                    }
                }
            }
        }
    }
}
