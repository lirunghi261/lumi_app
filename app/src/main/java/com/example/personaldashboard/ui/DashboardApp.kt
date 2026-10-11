package com.example.personaldashboard.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FileOpen
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.SaveAlt
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.example.personaldashboard.DashboardController
import com.example.personaldashboard.R
import com.example.personaldashboard.model.DashboardState
import com.example.personaldashboard.model.ExpenseCategory
import com.example.personaldashboard.model.ExpenseTransaction
import com.example.personaldashboard.model.MoneyFund
import com.example.personaldashboard.model.ThemePreset
import com.example.personaldashboard.ui.components.AnimatedLumiMascot
import com.example.personaldashboard.ui.theme.MochiTheme
import com.example.personaldashboard.ui.theme.AppFontFamily
import com.example.personaldashboard.ui.theme.ThemePack
import com.example.personaldashboard.ui.theme.packFor
import com.example.personaldashboard.ui.theme.themePacks
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.rememberLazyListState

private enum class Destination(val label: String) {
    HOME("Tổng quan"), ACTIVITY("Giao dịch"), BUDGETS("Ngân sách"), THEMES("Tôi")
}

@Composable
fun DashboardApp(controller: DashboardController) {
    val state = controller.state
    val pack = packFor(state.activeSpace.theme)
    var destination by remember { mutableStateOf(Destination.HOME) }
    var showAddExpense by remember { mutableStateOf(false) }
    var newExpenseDate by remember { mutableStateOf<Long?>(null) }
    var editingTransaction by remember { mutableStateOf<ExpenseTransaction?>(null) }

    MochiTheme(pack) {
        if (!state.hasCompletedOnboarding) {
            ThemeBackground(pack) {
                OnboardingScreen(pack, controller::completeOnboarding)
            }
            return@MochiTheme
        }
        ThemeBackground(
            pack = pack,
            showWallpaper = false,
            simple = destination != Destination.HOME,
        ) {
            Scaffold(
                containerColor = if (destination == Destination.HOME) pack.surface else Color.Transparent,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                bottomBar = {
                    WalletNavigation(
                        pack = pack,
                        selected = destination,
                        onQuickAdd = {
                            newExpenseDate = null
                            showAddExpense = true
                        },
                        onSelect = { destination = it },
                    )
                },
            ) { padding ->
                when (destination) {
                    Destination.HOME -> WalletHome(
                        state = state,
                        pack = pack,
                        modifier = Modifier.padding(padding),
                        onDetailedAdd = {
                            newExpenseDate = null
                            showAddExpense = true
                        },
                        onQuickAdd = controller::addExpense,
                    )
                    Destination.ACTIVITY -> ActivityScreen(
                        state,
                        pack,
                        Modifier.padding(padding),
                        onEdit = { editingTransaction = it },
                        onRemove = controller::removeExpense,
                        onAddForDate = { date ->
                            newExpenseDate = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                            showAddExpense = true
                        },
                    )
                    Destination.BUDGETS -> BudgetScreen(
                        state,
                        pack,
                        Modifier.padding(padding),
                        onUpdatePlan = controller::updateFinancialPlan,
                        onUpdateCategory = controller::updateCategoryBudget,
                        onAddFund = controller::addFund,
                        onUpdateFund = controller::updateFund,
                    )
                    Destination.THEMES -> ThemesScreen(controller, pack, Modifier.padding(padding))
                }
            }
        }
    }

    if (showAddExpense) {
        AddExpenseDialog(
            pack = pack,
            funds = state.funds,
            initialCreatedAt = newExpenseDate,
            onDismiss = {
                showAddExpense = false
                newExpenseDate = null
            },
            onSave = { title, amount, category, createdAt, fundId ->
                controller.addExpense(title, amount, category, createdAt, fundId)
                showAddExpense = false
                newExpenseDate = null
            },
        )
    }
    editingTransaction?.let { transaction ->
        AddExpenseDialog(
            pack = pack,
            funds = state.funds,
            initial = transaction,
            onDismiss = { editingTransaction = null },
            onSave = { title, amount, category, createdAt, fundId ->
                controller.updateExpense(transaction.id, title, amount, category, createdAt, fundId)
                editingTransaction = null
            },
        )
    }
}

@Composable
private fun WalletNavigation(
    pack: ThemePack,
    selected: Destination,
    onQuickAdd: () -> Unit,
    onSelect: (Destination) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(126.dp)
            .background(pack.surface),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxSize()
                .padding(start = 8.dp, top = 26.dp, end = 8.dp, bottom = 20.dp),
            color = pack.surface.copy(alpha = 0.98f),
            shape = RoundedCornerShape(30.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, pack.border.copy(alpha = 0.28f)),
            shadowElevation = 12.dp,
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 5.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                WalletNavigationItem(
                    label = Destination.HOME.label,
                    icon = Icons.Rounded.Home,
                    selected = selected == Destination.HOME,
                    pack = pack,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelect(Destination.HOME) },
                )
                WalletNavigationItem(
                    label = Destination.ACTIVITY.label,
                    icon = Icons.AutoMirrored.Rounded.ReceiptLong,
                    selected = selected == Destination.ACTIVITY,
                    pack = pack,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelect(Destination.ACTIVITY) },
                )
                Box(Modifier.weight(1f).fillMaxSize()) {
                    Text(
                        "Ghi nhanh",
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp),
                        color = pack.primary,
                        fontFamily = AppFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                    )
                }
                WalletNavigationItem(
                    label = Destination.BUDGETS.label,
                    icon = Icons.Rounded.AccountBalanceWallet,
                    selected = selected == Destination.BUDGETS,
                    pack = pack,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelect(Destination.BUDGETS) },
                )
                WalletNavigationItem(
                    label = Destination.THEMES.label,
                    icon = Icons.Rounded.Person,
                    selected = selected == Destination.THEMES,
                    pack = pack,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelect(Destination.THEMES) },
                )
            }
        }

        Surface(
            onClick = onQuickAdd,
            modifier = Modifier.align(Alignment.TopCenter).size(64.dp),
            shape = CircleShape,
            color = pack.primary,
            contentColor = Color.White,
            border = androidx.compose.foundation.BorderStroke(4.dp, pack.surface),
            shadowElevation = 12.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Add, "Ghi nhanh", Modifier.size(30.dp))
            }
        }
    }
}

@Composable
private fun WalletNavigationItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    pack: ThemePack,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxSize(),
        color = Color.Transparent,
        contentColor = if (selected) pack.primary else pack.mutedInk,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(top = 9.dp, bottom = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Surface(
                color = if (selected) pack.primary.copy(alpha = 0.14f) else Color.Transparent,
                shape = CircleShape,
            ) {
                Box(Modifier.width(46.dp).height(34.dp), contentAlignment = Alignment.Center) {
                    Icon(icon, label, Modifier.size(22.dp))
                }
            }
            Text(
                label,
                color = if (selected) pack.secondary else pack.mutedInk,
                fontFamily = AppFontFamily,
                fontSize = 10.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun ThemeBackground(
    pack: ThemePack,
    showWallpaper: Boolean = true,
    simple: Boolean = false,
    content: @Composable () -> Unit,
) {
    val background = if (simple) {
        Brush.verticalGradient(listOf(pack.surface, pack.surfaceAlt.copy(alpha = 0.72f)))
    } else {
        Brush.verticalGradient(listOf(pack.top, pack.bottom))
    }
    Box(Modifier.fillMaxSize().background(background)) {
        if (showWallpaper && pack.preset == ThemePreset.COZY) {
            val wallpaper = painterResource(R.drawable.cozy_village_wallpaper)
            Image(wallpaper, null, Modifier.fillMaxSize().blur(22.dp), contentScale = ContentScale.Crop, alpha = 0.94f)
            Image(
                wallpaper,
                null,
                Modifier
                    .fillMaxSize()
                    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            brush = Brush.verticalGradient(
                                0f to Color.Black,
                                0.36f to Color.Black,
                                1f to Color.Transparent,
                                endY = size.height * 0.42f,
                            ),
                            blendMode = BlendMode.DstIn,
                        )
                    },
                contentScale = ContentScale.Crop,
                alpha = 0.98f,
            )
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.28f to Color.Transparent,
                        0.52f to pack.top.copy(alpha = 0.30f),
                        1f to pack.bottom.copy(alpha = 0.68f),
                    ),
                ),
            )
        }
        if (!simple) {
            Canvas(Modifier.fillMaxSize()) {
                repeat(30) { index ->
                    val x = ((index * 83) % 997) / 997f * size.width
                    val y = ((index * 149 + 47) % 991) / 991f * size.height
                    drawCircle(pack.sparkle.copy(alpha = 0.24f), if (index % 5 == 0) 4f else 2f, Offset(x, y))
                }
            }
        }
        content()
    }
}

@Composable
private fun OnboardingScreen(
    pack: ThemePack,
    onComplete: (String, Long, Long) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var income by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }
    val parsedIncome = income.filter(Char::isDigit).toLongOrNull() ?: 0L
    val parsedBudget = budget.filter(Char::isDigit).toLongOrNull() ?: 0L

    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(20.dp, 24.dp, 20.dp, 36.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Chào bạn, mình là Lumi!", color = pack.ink, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Mình cùng chăm sóc chiếc ví thật nhẹ nhàng nhé ✦", color = pack.mutedInk, fontSize = 13.sp)
                }
                AnimatedLumiMascot(Modifier.size(118.dp))
            }
        }
        item {
            Surface(
                color = pack.surface.copy(alpha = 0.96f),
                shape = RoundedCornerShape(28.dp),
                shadowElevation = 12.dp,
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
                    Text("Thiết lập ví của bạn", color = pack.ink, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                    Text("Các con số này chỉ lưu trên thiết bị của bạn.", color = pack.mutedInk, fontSize = 11.sp)
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it.take(30) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Lumi nên gọi bạn là gì?") },
                        placeholder = { Text("Ví dụ: Thiện") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                    )
                    MoneyField("Thu nhập dự kiến mỗi tháng", income, { income = it }, "12000000")
                    MoneyField("Ngân sách chi tiêu mỗi tháng", budget, { budget = it }, "7500000")
                    if (parsedBudget > parsedIncome && parsedIncome > 0) {
                        Text("Ngân sách đang cao hơn thu nhập dự kiến đó nha.", color = pack.secondary, fontSize = 11.sp)
                    }
                    Button(
                        onClick = { onComplete(name, parsedIncome, parsedBudget) },
                        enabled = parsedBudget > 0,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = pack.primary),
                        shape = RoundedCornerShape(17.dp),
                    ) { Text("Bắt đầu cùng Lumi") }
                }
            }
        }
        item {
            Text(
                "Bạn có thể đổi thu nhập, ngân sách, chủ đề và sao lưu dữ liệu bất cứ lúc nào trong Cài đặt.",
                color = pack.mutedInk,
                fontSize = 11.sp,
            )
        }
    }
}

@Composable
private fun MoneyField(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.filter(Char::isDigit).take(12)) },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        supportingText = {
            val amount = value.toLongOrNull() ?: 0L
            if (amount > 0) Text(formatMoney(amount))
        },
        suffix = { Text("₫") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(16.dp),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WalletHome(
    state: DashboardState,
    pack: ThemePack,
    modifier: Modifier,
    onDetailedAdd: () -> Unit,
    onQuickAdd: (String, Long, ExpenseCategory) -> Unit,
) {
    val listState = rememberLazyListState()
    val pullState = rememberPullToRefreshState()
    val coroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val expandedHeight = 440.dp
    val pinnedHeight = 96.dp
    val expandedHeightPx = with(density) { expandedHeight.toPx() }
    val maxCollapsePx = with(density) { (expandedHeight - pinnedHeight).toPx() }
    val scrollOffsetPx by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex == 0) {
                listState.firstVisibleItemScrollOffset.toFloat()
            } else {
                expandedHeightPx
            }
        }
    }
    val collapseOffsetPx = scrollOffsetPx.coerceIn(0f, maxCollapsePx)
    val collapseProgress = (collapseOffsetPx / maxCollapsePx).coerceIn(0f, 1f)
    val compactHeaderProgress = ((collapseProgress - 0.88f) / 0.12f).coerceIn(0f, 1f)
    val pullFraction = pullState.distanceFraction.coerceIn(0f, 1.6f)
    val pullOffset = 56.dp * pullFraction
    val pullOffsetPx = with(density) { pullOffset.toPx() }
    val refresh = {
        if (!isRefreshing) {
            coroutineScope.launch {
                isRefreshing = true
                delay(1_050)
                isRefreshing = false
            }
        }
    }

    BoxWithConstraints(modifier.fillMaxSize()) {
        val minimumPanelHeight = if (maxHeight > pinnedHeight) maxHeight - pinnedHeight else 0.dp

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = refresh,
            state = pullState,
            modifier = Modifier.fillMaxSize(),
        ) {
            WalletBackdrop(
                state = state,
                pack = pack,
                collapseProgress = collapseProgress,
                collapseOffsetPx = collapseOffsetPx,
                pullFraction = pullFraction,
                pullOffsetPx = pullOffsetPx,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(expandedHeight + pullOffset),
            )

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationY = pullOffsetPx
                    },
            ) {
                item(key = "wallet-sliver-space") {
                    Box(Modifier.fillMaxWidth().height(expandedHeight - 72.dp))
                }
                item(key = "wallet-content") {
                    WalletContentPanel(
                        state = state,
                        pack = pack,
                        minimumHeight = minimumPanelHeight,
                        onDetailedAdd = onDetailedAdd,
                        onQuickAdd = onQuickAdd,
                    )
                }
            }

            CompactWalletHeader(
                progress = compactHeaderProgress,
                isRefreshing = isRefreshing,
                pack = pack,
                name = state.profileName,
                reduceMotion = state.reduceMotion,
            )
        }
    }
}

@Composable
private fun WalletContentPanel(
    state: DashboardState,
    pack: ThemePack,
    minimumHeight: Dp,
    modifier: Modifier = Modifier,
    onDetailedAdd: () -> Unit,
    onQuickAdd: (String, Long, ExpenseCategory) -> Unit,
) {
    Column(modifier.fillMaxWidth()) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(72.dp)
                .background(
                    Brush.verticalGradient(
                        0f to pack.surface.copy(alpha = 0f),
                        0.56f to pack.surface.copy(alpha = 0.72f),
                        1f to pack.surface,
                    ),
                ),
        )
        Surface(
            modifier = Modifier.fillMaxWidth().heightIn(min = minimumHeight),
            color = pack.surface,
            contentColor = pack.ink,
            shape = RoundedCornerShape(0.dp),
            shadowElevation = 0.dp,
            tonalElevation = 0.dp,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, top = 4.dp, bottom = 260.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                QuickEntryCard(pack, onDetailedAdd, onQuickAdd)
                SectionTitle("Quỹ của bạn", "${state.funds.size} quỹ", pack)
                FundPreview(state, pack)
                SectionTitle("Lumi nhắc bạn", "", pack)
                FinanceAlerts(state, pack)
                SectionTitle("Hũ ngân sách", "Vuốt để xem", pack)
                CategoryBudgetPreview(state, pack)
                SectionTitle("Chi tiêu gần đây", if (state.transactions.isEmpty()) "" else "5 giao dịch mới nhất", pack)
                state.transactions.take(5).forEach { transaction ->
                    TransactionRow(transaction, pack, fundName = state.fundFor(transaction.fundId).name)
                }
                if (state.transactions.isEmpty()) EmptyCard("Chưa có khoản chi nào. Lumi đang rất tự hào!", pack)
            }
        }
    }
}

@Composable
private fun FundPreview(state: DashboardState, pack: ThemePack) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(state.funds, key = { it.id }) { fund ->
            Surface(
                modifier = Modifier.width(168.dp),
                color = if (fund.isPrimary) pack.primary.copy(alpha = 0.12f) else pack.surfaceAlt,
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, pack.border.copy(alpha = 0.25f)),
            ) {
                Column(Modifier.padding(13.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(fund.emoji, fontSize = 21.sp)
                        Text(fund.name, Modifier.weight(1f).padding(start = 7.dp), color = pack.ink, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                    Text(
                        formatMoney(fund.balance),
                        Modifier.padding(top = 9.dp),
                        color = if (fund.balance >= 0) pack.ink else pack.secondary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(if (fund.isPrimary) "Quỹ chính · ghi nhanh" else "Quỹ phụ", color = pack.mutedInk, fontSize = 9.sp)
                }
            }
        }
    }
}

private data class FinanceAlert(val emoji: String, val title: String, val detail: String, val urgent: Boolean = false)

@Composable
private fun FinanceAlerts(state: DashboardState, pack: ThemePack) {
    val alerts = buildList {
        state.funds.filter { it.balance < 0 }.forEach { fund ->
            add(FinanceAlert("⚠️", "${fund.name} đang âm", "Thiếu ${formatMoney(-fund.balance)} · nên cân đối hoặc nạp thêm", true))
        }
        if (state.monthlyBudget > 0 && state.spentThisMonth >= state.monthlyBudget * 85 / 100) {
            add(FinanceAlert("📊", "Ngân sách tháng sắp chạm hạn mức", "Đã dùng ${formatMoney(state.spentThisMonth)} / ${formatMoney(state.monthlyBudget)}", true))
        }
        ExpenseCategory.entries.forEach { category ->
            val limit = state.categoryBudgets[category.name] ?: 0
            val spent = state.transactionsThisMonth.filter { it.category == category }.sumOf { it.amount }
            if (limit > 0 && spent >= limit * 85 / 100) {
                add(FinanceAlert(category.emoji, "Hũ ${category.title} sắp đầy", "Đã dùng ${formatCompact(spent)} / ${formatCompact(limit)}"))
            }
        }
        if (isEmpty()) {
            add(FinanceAlert("✨", "Mọi thứ đang trong tầm kiểm soát", "Lumi sẽ báo khi quỹ hoặc ngân sách cần chú ý"))
        }
    }.take(3)

    Surface(
        color = pack.surface.copy(alpha = 0.96f),
        shape = RoundedCornerShape(19.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, pack.border.copy(alpha = 0.22f)),
    ) {
        Column(Modifier.fillMaxWidth()) {
            alerts.forEachIndexed { index, alert ->
                Row(Modifier.fillMaxWidth().padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = pack.surfaceAlt, shape = CircleShape) {
                        Text(alert.emoji, Modifier.padding(8.dp), fontSize = 17.sp)
                    }
                    Column(Modifier.weight(1f).padding(start = 10.dp)) {
                        Text(alert.title, color = if (alert.urgent) pack.secondary else pack.ink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(alert.detail, color = pack.mutedInk, fontSize = 9.sp)
                    }
                }
                if (index < alerts.lastIndex) {
                    Box(Modifier.fillMaxWidth().padding(horizontal = 13.dp).height(1.dp).background(pack.border.copy(alpha = 0.13f)))
                }
            }
        }
    }
}

@Composable
private fun CompactWalletHeader(progress: Float, isRefreshing: Boolean, pack: ThemePack, name: String, reduceMotion: Boolean) {
    Surface(
        color = pack.surface.copy(alpha = (progress * 3f).coerceIn(0f, 1f)),
        shadowElevation = if (progress > 0.15f) 10.dp else 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationY = -(1f - progress) * 42.dp.toPx()
            },
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .height(72.dp)
                .padding(horizontal = 16.dp)
                .graphicsLayer { alpha = progress },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedLumiMascot(Modifier.size(47.dp), reduceMotion)
            Column(Modifier.weight(1f).padding(start = 9.dp)) {
                Text("Tổng quan", color = pack.ink, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                Text(if (name.isBlank()) "Lumi bên bạn" else "$name · Lumi bên bạn", color = pack.mutedInk, fontSize = 10.sp)
            }
            Surface(color = pack.surfaceAlt, shape = RoundedCornerShape(14.dp)) {
                Text("Tháng này", Modifier.padding(horizontal = 10.dp, vertical = 7.dp), color = pack.primary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            if (isRefreshing) {
                CircularProgressIndicator(Modifier.padding(start = 10.dp).size(22.dp), color = pack.primary, strokeWidth = 2.5.dp)
            } else {
                Icon(Icons.Rounded.Palette, "Tùy chỉnh", Modifier.padding(start = 10.dp).size(22.dp), tint = pack.primary)
            }
        }
    }
}

@Composable
private fun WalletBackdrop(
    state: DashboardState,
    pack: ThemePack,
    collapseProgress: Float = 0f,
    collapseOffsetPx: Float = 0f,
    pullFraction: Float = 0f,
    pullOffsetPx: Float = 0f,
    modifier: Modifier = Modifier,
) {
    val darkGreen = Color(0xFF092C28)
    val spent = state.spentThisMonth
    val totalBalance = state.totalFundBalance
    val targetProgress = (spent.toFloat() / state.monthlyBudget.coerceAtLeast(1)).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(targetProgress, tween(900, easing = FastOutSlowInEasing), label = "Budget progress")
    val animatedBalance by animateFloatAsState(totalBalance.toFloat(), tween(850, easing = FastOutSlowInEasing), label = "Fund balance")
    val motion = rememberInfiniteTransition(label = "Modern hero motion")
    val cardTilt by motion.animateFloat(
        initialValue = -0.45f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(tween(3_400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "Balance card float",
    )
    val shimmer by motion.animateFloat(
        initialValue = -350f,
        targetValue = 1_100f,
        animationSpec = infiniteRepeatable(tween(4_200, easing = LinearEasing), RepeatMode.Restart),
        label = "Balance shimmer",
    )

    Box(
        modifier
            .fillMaxWidth(),
    ) {
        if (pack.preset == ThemePreset.COZY) {
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationY = -collapseOffsetPx * 0.16f
                        transformOrigin = TransformOrigin(0.5f, 0f)
                        scaleY = 1f + pullFraction * 0.11f
                    },
            ) {
                Image(
                    painter = painterResource(R.drawable.cozy_village_wallpaper),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.98f,
                )
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            0f to Color.White.copy(alpha = 0.04f),
                            0.62f to pack.top.copy(alpha = 0.10f),
                            1f to pack.bottom.copy(alpha = 0.42f),
                        ),
                    ),
                )
            }
        }
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 10.dp)
                .graphicsLayer {
                    translationY = pullOffsetPx * 0.55f - collapseOffsetPx * 0.92f
                    alpha = (1f - collapseProgress / 0.68f).coerceIn(0f, 1f)
                },
        ) {
            Row(Modifier.fillMaxWidth().height(92.dp), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text("LUMI WALLET", color = pack.primary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                    Text(if (state.profileName.isBlank()) "Hôm nay" else "Chào, ${state.profileName}", color = pack.ink, fontSize = 27.sp, fontWeight = FontWeight.Bold)
                    Text(
                        LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd/MM", Locale.forLanguageTag("vi-VN"))),
                        color = pack.mutedInk,
                        fontSize = 11.sp,
                    )
                }
                AnimatedLumiMascot(Modifier.size(105.dp).graphicsLayer { translationY = -8f }, state.reduceMotion)
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(142.dp)
                    .graphicsLayer { rotationZ = if (state.reduceMotion) 0f else cardTilt }
                    .shadow(10.dp, RoundedCornerShape(23.dp))
                    .clip(RoundedCornerShape(23.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(pack.surface.copy(alpha = 0.90f), Color.White.copy(alpha = 0.94f), pack.surfaceAlt.copy(alpha = 0.90f)),
                            start = Offset(if (state.reduceMotion) 0f else shimmer, 0f),
                            end = Offset((if (state.reduceMotion) 0f else shimmer) + 560f, 260f),
                        ),
                    ),
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    drawCircle(darkGreen.copy(alpha = 0.055f), size.width * 0.48f, Offset(size.width * 0.88f, size.height * 0.15f))
                    drawCircle(darkGreen.copy(alpha = 0.035f), size.width * 0.30f, Offset(size.width * 0.76f, size.height * 0.80f))
                }
                Column(Modifier.padding(17.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = darkGreen.copy(alpha = 0.08f), shape = CircleShape) {
                            Icon(Icons.Rounded.AccountBalanceWallet, null, Modifier.padding(7.dp).size(17.dp), tint = darkGreen)
                        }
                        Text("Tổng số dư các quỹ", Modifier.padding(start = 8.dp), color = darkGreen.copy(alpha = 0.66f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text("VND", Modifier.weight(1f), color = darkGreen.copy(alpha = 0.50f), fontSize = 9.sp, textAlign = androidx.compose.ui.text.style.TextAlign.End)
                    }
                    Text(
                        formatMoney(animatedBalance.toLong()),
                        color = darkGreen,
                        fontSize = 31.sp,
                        lineHeight = 38.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.fillMaxWidth().padding(top = 7.dp).height(5.dp).clip(CircleShape),
                        color = if (targetProgress > 0.85f) pack.secondary else Color(0xFF2C775F),
                        trackColor = darkGreen.copy(alpha = 0.10f),
                    )
                    Text(
                        "Ngân sách tháng đã dùng ${(targetProgress * 100).toInt()}%",
                        color = darkGreen.copy(alpha = 0.58f),
                        fontSize = 8.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }

            Row(Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HeroStat("Thu dự kiến", formatCompact(state.monthlyIncome), "↗", pack, Modifier.weight(1f))
                HeroStat("Chi tháng này", formatCompact(spent), "↘", pack, Modifier.weight(1f))
            }
            Surface(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                color = pack.surface.copy(alpha = 0.76f),
                shape = RoundedCornerShape(15.dp),
            ) {
                Row(Modifier.padding(horizontal = 12.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("✦", color = pack.primary, fontSize = 13.sp)
                    Text(lumiReminder(state), Modifier.weight(1f).padding(start = 7.dp), color = pack.ink, fontSize = 10.sp, maxLines = 2)
                    Text("›", color = pack.primary, fontSize = 22.sp)
                }
            }
        }
    }
}

@Composable
private fun HeroStat(label: String, value: String, arrow: String, pack: ThemePack, modifier: Modifier = Modifier) {
    Surface(modifier, color = pack.surface.copy(alpha = 0.78f), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 9.dp)) {
            Text("$arrow  $label", color = pack.mutedInk, fontSize = 9.sp)
            Text(value, color = pack.ink, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun StaggeredReveal(delayMillis: Long, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delayMillis)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(520)) + slideInVertically(tween(520, easing = FastOutSlowInEasing)) { it / 5 },
    ) { content() }
}

@Composable
private fun BalanceCard(state: DashboardState, pack: ThemePack) {
    val spent = state.spentThisMonth
    val left = state.monthlyBudget - spent
    val progress = (spent.toFloat() / state.monthlyBudget.coerceAtLeast(1)).coerceIn(0f, 1f)
    Card(
        colors = CardDefaults.cardColors(containerColor = pack.surface.copy(alpha = 0.97f)),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(7.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, pack.border.copy(alpha = 0.55f)),
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("CÒN LẠI THÁNG NÀY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = pack.mutedInk)
                    Text(
                        formatMoney(left),
                        fontFamily = AppFontFamily,
                        fontSize = 34.sp,
                        lineHeight = 39.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (left >= 0) pack.primary else pack.secondary,
                    )
                }
                Surface(color = pack.surfaceAlt, shape = CircleShape) { Text("💰", Modifier.padding(13.dp), fontSize = 26.sp) }
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp).height(9.dp).clip(CircleShape),
                color = if (progress > 0.85f) pack.secondary else pack.primary,
                trackColor = pack.border.copy(alpha = 0.16f),
            )
            Row(Modifier.fillMaxWidth().padding(top = 9.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Đã chi ${formatMoney(spent)}", color = pack.mutedInk, fontSize = 11.sp)
                Text("Hạn mức ${formatMoney(state.monthlyBudget)}", color = pack.mutedInk, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun QuickEntryCard(
    pack: ThemePack,
    onDetailedAdd: () -> Unit,
    onQuickAdd: (String, Long, ExpenseCategory) -> Unit,
) {
    var draft by remember { mutableStateOf("") }
    val submit = {
        parseQuickExpense(draft)?.let { (title, amount, category) ->
            onQuickAdd(title, amount, category)
            draft = ""
        }
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = pack.surfaceAlt.copy(alpha = 0.96f)),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, pack.secondary.copy(alpha = 0.42f)),
    ) {
        Column(Modifier.fillMaxWidth().padding(15.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Ghi nhanh", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = pack.ink)
                    Text("Ví dụ: Cà phê 35k", color = pack.mutedInk, fontSize = 11.sp)
                }
                TextButton(onClick = onDetailedAdd) { Text("Nhập chi tiết", color = pack.primary, fontSize = 11.sp) }
            }
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                placeholder = { Text("Ăn trưa 65k") },
                singleLine = true,
                shape = RoundedCornerShape(15.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submit() }),
                trailingIcon = {
                    Surface(
                        onClick = { submit() },
                        enabled = parseQuickExpense(draft) != null,
                        color = if (parseQuickExpense(draft) != null) pack.secondary else pack.border.copy(alpha = 0.18f),
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.size(38.dp),
                    ) { Icon(Icons.Rounded.Add, "Lưu nhanh", Modifier.padding(8.dp)) }
                },
            )
            if (draft.isNotBlank() && parseQuickExpense(draft) == null) {
                Text("Thêm số tiền ở cuối, ví dụ 35k hoặc 120000", color = pack.secondary, fontSize = 10.sp, modifier = Modifier.padding(top = 6.dp))
            }
        }
    }
}

@Composable
private fun CategoryBudgetPreview(state: DashboardState, pack: ThemePack) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(end = 4.dp),
    ) {
        items(ExpenseCategory.entries, key = { it.name }) { category ->
            BudgetMiniCard(category, state, pack)
        }
    }
}

@Composable
private fun BudgetMiniCard(category: ExpenseCategory, state: DashboardState, pack: ThemePack) {
    val spent = state.transactionsThisMonth.filter { it.category == category }.sumOf { it.amount }
    val budget = state.categoryBudgets[category.name] ?: 1L
    val targetProgress = (spent.toFloat() / budget.coerceAtLeast(1)).coerceIn(0f, 1f)
    val progress by animateFloatAsState(targetProgress, tween(780, easing = FastOutSlowInEasing), label = "${category.name} budget")
    val shape = RoundedCornerShape(23.dp)
    Box(
        Modifier
            .width(178.dp)
            .height(148.dp)
            .shadow(6.dp, shape)
            .clip(shape)
            .background(Brush.linearGradient(listOf(pack.surface.copy(alpha = 0.98f), pack.surfaceAlt.copy(alpha = 0.98f))))
            .border(1.dp, pack.border.copy(alpha = 0.28f), shape)
            .padding(15.dp),
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = pack.primary.copy(alpha = 0.10f), shape = RoundedCornerShape(12.dp)) {
                    Text(category.emoji, Modifier.padding(8.dp), fontSize = 20.sp)
                }
                Text(category.title, Modifier.padding(start = 9.dp), color = pack.ink, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Text(formatCompact(budget - spent), color = pack.ink, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(top = 11.dp))
            Text("còn lại", color = pack.mutedInk, fontSize = 9.sp)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().padding(top = 9.dp).height(6.dp).clip(CircleShape),
                color = if (targetProgress > 0.85f) pack.secondary else pack.primary,
                trackColor = pack.border.copy(alpha = 0.12f),
            )
        }
    }
}

@Composable
private fun BudgetLine(category: ExpenseCategory, state: DashboardState, pack: ThemePack) {
    val spent = state.transactionsThisMonth.filter { it.category == category }.sumOf { it.amount }
    val budget = state.categoryBudgets[category.name] ?: 1L
    val progress = (spent.toFloat() / budget.coerceAtLeast(1)).coerceIn(0f, 1f)
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(category.emoji, fontSize = 20.sp)
            Text(category.title, Modifier.weight(1f).padding(start = 9.dp), color = pack.ink, fontWeight = FontWeight.SemiBold)
            Text("${formatCompact(spent)} / ${formatCompact(budget)}", color = pack.mutedInk, fontSize = 11.sp)
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp).height(7.dp).clip(CircleShape),
            color = if (progress > 0.85f) pack.secondary else pack.primary,
            trackColor = pack.border.copy(alpha = 0.14f),
        )
    }
}

@Composable
private fun ActivityScreen(
    state: DashboardState,
    pack: ThemePack,
    modifier: Modifier,
    onEdit: (ExpenseTransaction) -> Unit,
    onRemove: (String) -> Unit,
    onAddForDate: (LocalDate) -> Unit,
) {
    var pendingDelete by remember { mutableStateOf<ExpenseTransaction?>(null) }
    var visibleMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showMonthPicker by remember { mutableStateOf(false) }
    val zone = ZoneId.systemDefault()
    val datedTransactions = state.transactions.map { transaction ->
        transaction to Instant.ofEpochMilli(transaction.createdAt).atZone(zone).toLocalDate()
    }
    val monthTransactions = datedTransactions.filter { (_, date) -> YearMonth.from(date) == visibleMonth }
    val totalsByDate = monthTransactions.groupBy({ it.second }, { it.first }).mapValues { (_, items) -> items.sumOf { it.amount } }
    val selectedTransactions = datedTransactions
        .filter { (_, date) -> date == selectedDate }
        .map { it.first }
        .sortedByDescending { it.createdAt }
    val monthTotal = monthTransactions.sumOf { it.first.amount }
    val budgetProgress = if (state.monthlyBudget > 0L) {
        (monthTotal.toFloat() / state.monthlyBudget).coerceIn(0f, 1f)
    } else 0f

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 28.dp, 16.dp, 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { PageTitle("Lịch chi tiêu", "Mỗi ngày một ghi chú nhỏ, cuối tháng mình hiểu ví hơn.", pack) }
        item {
            Surface(
                color = pack.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 8.dp,
            ) {
                Column(Modifier.fillMaxWidth().padding(18.dp)) {
                    Text("ĐÃ DÙNG TRONG THÁNG", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.76f))
                    Text(formatMoney(monthTotal), fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(top = 4.dp))
                    Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${monthTransactions.size} khoản chi", fontSize = 12.sp, color = Color.White.copy(alpha = 0.82f))
                        if (state.monthlyBudget > 0L) {
                            Text("Còn ${formatCompact(state.monthlyBudget - monthTotal)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    if (state.monthlyBudget > 0L) {
                        LinearProgressIndicator(
                            progress = { budgetProgress },
                            modifier = Modifier.fillMaxWidth().padding(top = 10.dp).height(7.dp).clip(CircleShape),
                            color = if (budgetProgress > 0.85f) pack.secondary else Color.White,
                            trackColor = Color.White.copy(alpha = 0.22f),
                        )
                    }
                }
            }
        }
        item {
            SpendingCalendar(
                month = visibleMonth,
                selectedDate = selectedDate,
                totalsByDate = totalsByDate,
                pack = pack,
                onPreviousMonth = {
                    visibleMonth = visibleMonth.minusMonths(1)
                    selectedDate = visibleMonth.atDay(1)
                },
                onNextMonth = {
                    visibleMonth = visibleMonth.plusMonths(1)
                    selectedDate = visibleMonth.atDay(1)
                },
                onChooseMonth = { showMonthPicker = true },
                onSelectDate = { selectedDate = it },
            )
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        selectedDate.format(DateTimeFormatter.ofPattern("EEEE, dd/MM", Locale.forLanguageTag("vi-VN"))),
                        color = pack.ink,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "${selectedTransactions.size} khoản · ${formatMoney(selectedTransactions.sumOf { it.amount })}",
                        color = pack.mutedInk,
                        fontSize = 11.sp,
                    )
                }
                Button(
                    onClick = { onAddForDate(selectedDate) },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = pack.secondary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 9.dp),
                ) {
                    Icon(Icons.Rounded.Add, null, Modifier.size(18.dp))
                    Text("Ghi ngày này", Modifier.padding(start = 5.dp), fontSize = 11.sp)
                }
            }
        }
        items(selectedTransactions, key = { it.id }) { transaction ->
            TransactionRow(
                transaction = transaction,
                pack = pack,
                fundName = state.fundFor(transaction.fundId).name,
                onEdit = { onEdit(transaction) },
                onRemove = { pendingDelete = transaction },
            )
        }
        if (selectedTransactions.isEmpty()) item {
            EmptyCard("Ngày này chưa có khoản chi nào. Ví đang được nghỉ ngơi ✨", pack)
        }
    }

    if (showMonthPicker) {
        MonthYearPickerDialog(
            currentMonth = visibleMonth,
            pack = pack,
            onDismiss = { showMonthPicker = false },
            onSelect = { chosenMonth ->
                visibleMonth = chosenMonth
                selectedDate = if (chosenMonth == YearMonth.now()) LocalDate.now() else chosenMonth.atDay(1)
                showMonthPicker = false
            },
        )
    }

    pendingDelete?.let { transaction ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            containerColor = pack.surface,
            title = { Text("Xóa giao dịch này?") },
            text = { Text("${transaction.title} · ${formatMoney(transaction.amount)}") },
            confirmButton = {
                Button(
                    onClick = { onRemove(transaction.id); pendingDelete = null },
                    colors = ButtonDefaults.buttonColors(containerColor = pack.secondary),
                ) { Text("Xóa") }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Giữ lại") } },
        )
    }
}

@Composable
private fun SpendingCalendar(
    month: YearMonth,
    selectedDate: LocalDate,
    totalsByDate: Map<LocalDate, Long>,
    pack: ThemePack,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onChooseMonth: () -> Unit,
    onSelectDate: (LocalDate) -> Unit,
) {
    val firstDayOffset = month.atDay(1).dayOfWeek.value - 1
    val slotCount = ((firstDayOffset + month.lengthOfMonth() + 6) / 7) * 7
    val monthLabel = month.format(DateTimeFormatter.ofPattern("'Tháng' M 'năm' yyyy", Locale.forLanguageTag("vi-VN")))

    Surface(
        color = pack.surface.copy(alpha = 0.98f),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, pack.border.copy(alpha = 0.22f)),
        shadowElevation = 5.dp,
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onPreviousMonth) { Text("‹", fontSize = 25.sp, color = pack.primary) }
                TextButton(onClick = onChooseMonth, modifier = Modifier.weight(1f)) {
                    Text(
                        "$monthLabel  ▾",
                        color = pack.ink,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
                TextButton(onClick = onNextMonth) { Text("›", fontSize = 25.sp, color = pack.primary) }
            }
            Row(Modifier.fillMaxWidth().padding(bottom = 5.dp)) {
                listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN").forEach { label ->
                    Text(
                        label,
                        Modifier.weight(1f),
                        color = pack.mutedInk,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }
            repeat(slotCount / 7) { week ->
                Row(Modifier.fillMaxWidth()) {
                    repeat(7) { weekday ->
                        val slot = week * 7 + weekday
                        val dayNumber = slot - firstDayOffset + 1
                        if (dayNumber in 1..month.lengthOfMonth()) {
                            val date = month.atDay(dayNumber)
                            CalendarDay(
                                date = date,
                                total = totalsByDate[date] ?: 0L,
                                selected = date == selectedDate,
                                isToday = date == LocalDate.now(),
                                pack = pack,
                                modifier = Modifier.weight(1f),
                                onClick = { onSelectDate(date) },
                            )
                        } else {
                            Box(Modifier.weight(1f).height(58.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthYearPickerDialog(
    currentMonth: YearMonth,
    pack: ThemePack,
    onDismiss: () -> Unit,
    onSelect: (YearMonth) -> Unit,
) {
    var yearText by remember(currentMonth) { mutableStateOf(currentMonth.year.toString()) }
    val year = yearText.toIntOrNull()
    val monthNames = listOf(
        "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
        "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12",
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = pack.surface,
        shape = RoundedCornerShape(26.dp),
        title = {
            Column {
                Text("Chọn tháng", color = pack.ink, fontWeight = FontWeight.Bold)
                Text("Nhập năm cũ như 2025 hoặc 2024", color = pack.mutedInk, fontSize = 11.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                        onClick = { yearText = ((year ?: currentMonth.year) - 1).toString() },
                    ) { Text("‹", fontSize = 26.sp) }
                    OutlinedTextField(
                        value = yearText,
                        onValueChange = { input -> yearText = input.filter(Char::isDigit).take(4) },
                        modifier = Modifier.weight(1f),
                        label = { Text("Năm") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(16.dp),
                    )
                    TextButton(
                        onClick = { yearText = ((year ?: currentMonth.year) + 1).toString() },
                    ) { Text("›", fontSize = 26.sp) }
                }
                repeat(4) { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(3) { column ->
                            val monthNumber = row * 3 + column + 1
                            val isSelected = year == currentMonth.year && monthNumber == currentMonth.monthValue
                            Surface(
                                onClick = {
                                    if (year != null && year in 1900..2100) onSelect(YearMonth.of(year, monthNumber))
                                },
                                modifier = Modifier.weight(1f).height(44.dp),
                                color = if (isSelected) pack.primary else pack.surfaceAlt,
                                contentColor = if (isSelected) Color.White else pack.ink,
                                shape = RoundedCornerShape(14.dp),
                                enabled = year != null && year in 1900..2100,
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(monthNames[monthNumber - 1], fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
                TextButton(
                    onClick = { onSelect(YearMonth.now()) },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                ) {
                    Text("Về tháng hiện tại", color = pack.primary)
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Đóng") } },
    )
}

@Composable
private fun CalendarDay(
    date: LocalDate,
    total: Long,
    selected: Boolean,
    isToday: Boolean,
    pack: ThemePack,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val background = when {
        selected -> pack.primary.copy(alpha = 0.18f)
        total > 0L -> pack.secondary.copy(alpha = 0.13f)
        else -> Color.Transparent
    }
    Surface(
        onClick = onClick,
        modifier = modifier.height(58.dp).padding(2.dp),
        color = background,
        shape = RoundedCornerShape(13.dp),
        border = if (selected) androidx.compose.foundation.BorderStroke(1.5.dp, pack.primary) else null,
    ) {
        Column(
            Modifier.fillMaxSize().padding(vertical = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                date.dayOfMonth.toString(),
                color = if (isToday || selected) pack.primary else pack.ink,
                fontSize = 11.sp,
                fontWeight = if (isToday || selected) FontWeight.Bold else FontWeight.Normal,
            )
            if (total > 0L) {
                Text(
                    formatCompact(total),
                    color = pack.secondary,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
                Box(Modifier.padding(top = 2.dp).size(4.dp).background(pack.secondary, CircleShape))
            }
        }
    }
}

@Composable
private fun TransactionRow(
    transaction: ExpenseTransaction,
    pack: ThemePack,
    fundName: String? = null,
    onEdit: (() -> Unit)? = null,
    onRemove: (() -> Unit)? = null,
) {
    Surface(
        color = pack.surface.copy(alpha = 0.94f),
        shape = RoundedCornerShape(17.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, pack.border.copy(alpha = 0.28f)),
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = pack.surfaceAlt, shape = RoundedCornerShape(13.dp)) {
                Text(transaction.category.emoji, Modifier.padding(10.dp), fontSize = 21.sp)
            }
            Column(Modifier.weight(1f).padding(horizontal = 11.dp)) {
                Text(transaction.title, fontWeight = FontWeight.SemiBold, color = pack.ink)
                Text(
                    listOfNotNull(transaction.category.title, fundName, formatDate(transaction.createdAt)).joinToString(" · "),
                    color = pack.mutedInk,
                    fontSize = 10.sp,
                )
            }
            Text("-${formatMoney(transaction.amount)}", fontWeight = FontWeight.Bold, color = pack.secondary, fontSize = 12.sp)
            if (onEdit != null) {
                IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Rounded.Edit, "Sửa giao dịch", tint = pack.primary, modifier = Modifier.size(18.dp))
                }
            }
            if (onRemove != null) {
                IconButton(onClick = onRemove, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Rounded.DeleteOutline, "Xóa giao dịch", tint = pack.mutedInk, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun BudgetScreen(
    state: DashboardState,
    pack: ThemePack,
    modifier: Modifier,
    onUpdatePlan: (Long, Long) -> Unit,
    onUpdateCategory: (ExpenseCategory, Long) -> Unit,
    onAddFund: (String, String, Long, Boolean) -> Unit,
    onUpdateFund: (String, String, String, Long, Boolean) -> Unit,
) {
    var editingPlan by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<ExpenseCategory?>(null) }
    var addingFund by remember { mutableStateOf(false) }
    var editingFund by remember { mutableStateOf<MoneyFund?>(null) }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 28.dp, 16.dp, 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { PageTitle("Ngân sách & Quỹ", "Biết tiền đang ở đâu và được dành cho điều gì", pack) }
        item {
            Surface(
                color = pack.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 7.dp,
            ) {
                Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("TỔNG SỐ DƯ CÁC QUỸ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.76f))
                        Text(formatMoney(state.totalFundBalance), fontSize = 29.sp, fontWeight = FontWeight.ExtraBold)
                        Text("${state.funds.size} quỹ đang hoạt động", fontSize = 11.sp, color = Color.White.copy(alpha = 0.78f))
                    }
                    Text("👛", fontSize = 36.sp)
                }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Quỹ của bạn", Modifier.weight(1f), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = pack.ink)
                TextButton(onClick = { addingFund = true }) {
                    Icon(Icons.Rounded.Add, null, Modifier.size(18.dp))
                    Text("Thêm quỹ", Modifier.padding(start = 4.dp))
                }
            }
        }
        items(state.funds, key = { it.id }) { fund ->
            FundManagementCard(
                fund = fund,
                transactionCount = state.transactions.count { it.fundId == fund.id },
                pack = pack,
                onClick = { editingFund = fund },
            )
        }
        item { SectionTitle("Kế hoạch tháng", "", pack) }
        item { BalanceCard(state, pack) }
        item {
            Surface(color = pack.surface.copy(alpha = 0.95f), shape = RoundedCornerShape(18.dp)) {
                Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Thu nhập & hạn mức", color = pack.ink, fontWeight = FontWeight.Bold)
                        Text("Thu nhập ${formatMoney(state.monthlyIncome)} · Chi tối đa ${formatMoney(state.monthlyBudget)}", color = pack.mutedInk, fontSize = 10.sp)
                    }
                    TextButton(onClick = { editingPlan = true }) { Text("Chỉnh sửa") }
                }
            }
        }
        items(ExpenseCategory.entries, key = { it.name }) { category ->
            Card(
                onClick = { editingCategory = category },
                colors = CardDefaults.cardColors(containerColor = pack.surface.copy(alpha = 0.95f)),
                shape = RoundedCornerShape(19.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, pack.border.copy(alpha = 0.38f)),
            ) { Column(Modifier.padding(15.dp)) { BudgetLine(category, state, pack) } }
        }
        item { EmptyCard("Chạm vào từng hũ để đổi hạn mức. Lumi sẽ nhắc khi hũ dùng quá 85% nhé!", pack) }
    }

    if (editingPlan) {
        FinancialPlanDialog(state, pack, { editingPlan = false }) { income, budget ->
            onUpdatePlan(income, budget)
            editingPlan = false
        }
    }
    editingCategory?.let { category ->
        CategoryBudgetDialog(
            category = category,
            current = state.categoryBudgets[category.name] ?: 0,
            pack = pack,
            onDismiss = { editingCategory = null },
            onSave = { amount -> onUpdateCategory(category, amount); editingCategory = null },
        )
    }
    if (addingFund) {
        FundDialog(
            initial = null,
            pack = pack,
            onDismiss = { addingFund = false },
            onSave = { name, emoji, balance, primary ->
                onAddFund(name, emoji, balance, primary)
                addingFund = false
            },
        )
    }
    editingFund?.let { fund ->
        FundDialog(
            initial = fund,
            pack = pack,
            onDismiss = { editingFund = null },
            onSave = { name, emoji, balance, primary ->
                onUpdateFund(fund.id, name, emoji, balance, primary)
                editingFund = null
            },
        )
    }
}

@Composable
private fun FundManagementCard(
    fund: MoneyFund,
    transactionCount: Int,
    pack: ThemePack,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = pack.surface.copy(alpha = 0.97f)),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, pack.border.copy(alpha = 0.28f)),
    ) {
        Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = pack.surfaceAlt, shape = RoundedCornerShape(15.dp)) {
                Text(fund.emoji, Modifier.padding(11.dp), fontSize = 24.sp)
            }
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(fund.name, color = pack.ink, fontWeight = FontWeight.Bold)
                    if (fund.isPrimary) {
                        Surface(
                            modifier = Modifier.padding(start = 7.dp),
                            color = pack.primary.copy(alpha = 0.12f),
                            shape = CircleShape,
                        ) {
                            Text("Quỹ chính", Modifier.padding(horizontal = 7.dp, vertical = 3.dp), color = pack.primary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Text("$transactionCount khoản chi · Chạm để điều chỉnh", color = pack.mutedInk, fontSize = 10.sp)
            }
            Text(
                formatCompact(fund.balance),
                color = if (fund.balance >= 0) pack.ink else pack.secondary,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
            )
        }
    }
}

@Composable
private fun FundDialog(
    initial: MoneyFund?,
    pack: ThemePack,
    onDismiss: () -> Unit,
    onSave: (String, String, Long, Boolean) -> Unit,
) {
    var name by remember(initial?.id) { mutableStateOf(initial?.name.orEmpty()) }
    var emoji by remember(initial?.id) { mutableStateOf(initial?.emoji ?: "👛") }
    var balance by remember(initial?.id) { mutableStateOf(initial?.balance?.toString().orEmpty()) }
    var makePrimary by remember(initial?.id) { mutableStateOf(initial?.isPrimary ?: false) }
    val parsedBalance = balance.toLongOrNull()
    val emojis = listOf("💳", "👛", "🏦", "💵", "🪙", "✈️", "🏠", "🎓", "🛟")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = pack.surface,
        shape = RoundedCornerShape(26.dp),
        title = { Text(if (initial == null) "Tạo quỹ mới" else "Chỉnh sửa quỹ", color = pack.ink, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(30) },
                    label = { Text("Tên quỹ") },
                    placeholder = { Text("Ví dụ: Du lịch") },
                    singleLine = true,
                    shape = RoundedCornerShape(15.dp),
                )
                Text("Biểu tượng", color = pack.ink, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(emojis) { option ->
                        FilterChip(selected = emoji == option, onClick = { emoji = option }, label = { Text(option) })
                    }
                }
                OutlinedTextField(
                    value = balance,
                    onValueChange = { input ->
                        balance = input.filterIndexed { index, char -> char.isDigit() || (char == '-' && index == 0) }.take(13)
                    },
                    label = { Text("Số dư hiện tại (₫)") },
                    placeholder = { Text("2000000") },
                    supportingText = { Text("Đây là số tiền thực tế đang có trong quỹ") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(15.dp),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Đặt làm quỹ chính", color = pack.ink, fontWeight = FontWeight.SemiBold)
                        Text("Ghi nhanh sẽ tự chọn quỹ này", color = pack.mutedInk, fontSize = 10.sp)
                    }
                    Switch(checked = makePrimary, onCheckedChange = { if (initial?.isPrimary != true) makePrimary = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, emoji, parsedBalance ?: 0L, makePrimary) },
                enabled = name.isNotBlank() && parsedBalance != null && (initial != null || parsedBalance >= 0),
            ) { Text(if (initial == null) "Tạo quỹ" else "Lưu thay đổi") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } },
    )
}

@Composable
private fun FinancialPlanDialog(
    state: DashboardState,
    pack: ThemePack,
    onDismiss: () -> Unit,
    onSave: (Long, Long) -> Unit,
) {
    var income by remember { mutableStateOf(state.monthlyIncome.toString()) }
    var budget by remember { mutableStateOf(state.monthlyBudget.toString()) }
    val parsedIncome = income.toLongOrNull() ?: 0
    val parsedBudget = budget.toLongOrNull() ?: 0
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = pack.surface,
        title = { Text("Kế hoạch tháng") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MoneyField("Thu nhập dự kiến", income, { income = it }, "12000000")
                MoneyField("Ngân sách chi tiêu", budget, { budget = it }, "7500000")
            }
        },
        confirmButton = { Button(onClick = { onSave(parsedIncome, parsedBudget) }, enabled = parsedBudget > 0) { Text("Lưu") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } },
    )
}

@Composable
private fun CategoryBudgetDialog(
    category: ExpenseCategory,
    current: Long,
    pack: ThemePack,
    onDismiss: () -> Unit,
    onSave: (Long) -> Unit,
) {
    var amount by remember { mutableStateOf(current.toString()) }
    val parsed = amount.toLongOrNull() ?: 0
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = pack.surface,
        title = { Text("${category.emoji} Hũ ${category.title}") },
        text = { MoneyField("Hạn mức mỗi tháng", amount, { amount = it }, "1000000") },
        confirmButton = { Button(onClick = { onSave(parsed) }) { Text("Lưu hạn mức") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } },
    )
}

@Composable
private fun ThemesScreen(controller: DashboardController, pack: ThemePack, modifier: Modifier) {
    val context = LocalContext.current
    var editProfile by remember { mutableStateOf(false) }
    var confirmReset by remember { mutableStateOf(false) }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) runCatching {
            context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(controller.exportData()) }
        }.onSuccess { Toast.makeText(context, "Đã sao lưu ví của bạn", Toast.LENGTH_SHORT).show() }
            .onFailure { Toast.makeText(context, "Không thể sao lưu dữ liệu", Toast.LENGTH_SHORT).show() }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val success = runCatching {
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            }.getOrNull()?.let(controller::importData) == true
            Toast.makeText(context, if (success) "Đã khôi phục dữ liệu" else "Tệp sao lưu không hợp lệ", Toast.LENGTH_SHORT).show()
        }
    }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 28.dp, 16.dp, 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { PageTitle("Cài đặt", "Chiếc ví của bạn, theo cách bạn thích", pack) }
        item {
            Card(
                onClick = { editProfile = true },
                colors = CardDefaults.cardColors(containerColor = pack.surface.copy(alpha = 0.96f)),
                shape = RoundedCornerShape(20.dp),
            ) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = pack.primary.copy(alpha = 0.12f), shape = CircleShape) {
                        Icon(Icons.Rounded.Person, null, Modifier.padding(11.dp), tint = pack.primary)
                    }
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(controller.state.profileName.ifBlank { "Bạn của Lumi" }, color = pack.ink, fontWeight = FontWeight.Bold)
                        Text("Chỉnh tên hiển thị", color = pack.mutedInk, fontSize = 10.sp)
                    }
                    Icon(Icons.Rounded.Edit, null, tint = pack.mutedInk)
                }
            }
        }
        item {
            Surface(color = pack.surface.copy(alpha = 0.95f), shape = RoundedCornerShape(20.dp)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Tune, null, tint = pack.primary)
                    Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                        Text("Giảm chuyển động", color = pack.ink, fontWeight = FontWeight.SemiBold)
                        Text("Dành cho người nhạy cảm với animation", color = pack.mutedInk, fontSize = 10.sp)
                    }
                    Switch(checked = controller.state.reduceMotion, onCheckedChange = controller::setReduceMotion)
                }
            }
        }
        item { SectionTitle("Chủ đề", "", pack) }
        items(themePacks, key = { it.preset.name }) { option ->
            val selected = controller.state.activeSpace.theme == option.preset
            Card(
                onClick = { controller.setTheme(option.preset) },
                colors = CardDefaults.cardColors(containerColor = option.surface.copy(alpha = 0.97f)),
                shape = RoundedCornerShape(22.dp),
                border = androidx.compose.foundation.BorderStroke(if (selected) 2.5.dp else 1.dp, option.primary),
            ) {
                Row(Modifier.fillMaxWidth().padding(17.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(option.preset.emoji, fontSize = 34.sp)
                    Column(Modifier.weight(1f).padding(horizontal = 13.dp)) {
                        Text(option.preset.title, fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, color = option.ink, fontSize = 18.sp)
                        Text(option.preset.description, color = option.mutedInk, fontSize = 11.sp)
                    }
                    Text(if (selected) "✓" else "", color = option.primary, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        item { SectionTitle("Dữ liệu", "Lưu an toàn trên máy", pack) }
        item { SettingAction(Icons.Rounded.SaveAlt, "Sao lưu dữ liệu", "Xuất toàn bộ ví thành tệp JSON", pack) { exportLauncher.launch("lumi-wallet-backup.json") } }
        item { SettingAction(Icons.Rounded.FileOpen, "Khôi phục dữ liệu", "Nhập lại từ tệp sao lưu", pack) { importLauncher.launch(arrayOf("application/json", "text/plain")) } }
        item { SettingAction(Icons.Rounded.RestartAlt, "Làm mới toàn bộ ví", "Xóa dữ liệu và thiết lập lại từ đầu", pack, danger = true) { confirmReset = true } }
    }

    if (editProfile) {
        var name by remember { mutableStateOf(controller.state.profileName) }
        AlertDialog(
            onDismissRequest = { editProfile = false },
            containerColor = pack.surface,
            title = { Text("Lumi nên gọi bạn là gì?") },
            text = { OutlinedTextField(name, { name = it.take(30) }, singleLine = true, shape = RoundedCornerShape(15.dp)) },
            confirmButton = { Button(onClick = { controller.updateProfile(name); editProfile = false }) { Text("Lưu") } },
            dismissButton = { TextButton(onClick = { editProfile = false }) { Text("Hủy") } },
        )
    }
    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            containerColor = pack.surface,
            title = { Text("Thiết lập lại ví?") },
            text = { Text("Toàn bộ giao dịch và ngân sách sẽ bị xóa. Hãy sao lưu trước nếu bạn muốn giữ dữ liệu.") },
            confirmButton = {
                Button(
                    onClick = { confirmReset = false; controller.resetAllData() },
                    colors = ButtonDefaults.buttonColors(containerColor = pack.secondary),
                ) { Text("Xóa và làm lại") }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Hủy") } },
        )
    }
}

@Composable
private fun SettingAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    pack: ThemePack,
    danger: Boolean = false,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = pack.surface.copy(alpha = 0.95f)),
        shape = RoundedCornerShape(18.dp),
    ) {
        Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = if (danger) pack.secondary else pack.primary)
            Column(Modifier.padding(start = 12.dp)) {
                Text(title, color = if (danger) pack.secondary else pack.ink, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = pack.mutedInk, fontSize = 10.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExpenseDialog(
    pack: ThemePack,
    funds: List<MoneyFund>,
    initial: ExpenseTransaction? = null,
    initialCreatedAt: Long? = null,
    onDismiss: () -> Unit,
    onSave: (String, Long, ExpenseCategory, Long, String) -> Unit,
) {
    var title by remember(initial?.id) { mutableStateOf(initial?.title.orEmpty()) }
    var amount by remember(initial?.id) { mutableStateOf(initial?.amount?.toString().orEmpty()) }
    var category by remember(initial?.id) { mutableStateOf(initial?.category ?: ExpenseCategory.FOOD) }
    var fundId by remember(initial?.id) {
        mutableStateOf(initial?.fundId ?: (funds.firstOrNull { it.isPrimary } ?: funds.first()).id)
    }
    var createdAt by remember(initial?.id, initialCreatedAt) {
        mutableStateOf(initial?.createdAt ?: initialCreatedAt ?: System.currentTimeMillis())
    }
    val parsedAmount = amount.filter(Char::isDigit).toLongOrNull() ?: 0L

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = pack.surface,
        shape = RoundedCornerShape(26.dp),
        title = {
            Column {
                Text(if (initial == null) "Thêm khoản chi ✦" else "Sửa khoản chi", fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, color = pack.ink)
                Text(if (initial == null) "Lumi sẽ giúp bạn ghi nhớ" else "Cập nhật lại cho chính xác nhé", fontSize = 11.sp, color = pack.mutedInk)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Bạn đã mua gì?") },
                    placeholder = { Text("Ví dụ: Cơm trưa") },
                    singleLine = true,
                    shape = RoundedCornerShape(15.dp),
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter(Char::isDigit).take(12) },
                    label = { Text("Số tiền (₫)") },
                    placeholder = { Text("50000") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(15.dp),
                )
                Text("Trừ từ quỹ nào?", fontWeight = FontWeight.SemiBold, color = pack.ink, fontSize = 12.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    items(funds, key = { it.id }) { fund ->
                        FilterChip(
                            selected = fundId == fund.id,
                            onClick = { fundId = fund.id },
                            label = {
                                Text("${fund.emoji} ${fund.name} · ${formatCompact(fund.balance)}", fontSize = 10.sp)
                            },
                        )
                    }
                }
                Text("Danh mục chi tiêu", fontWeight = FontWeight.SemiBold, color = pack.ink, fontSize = 12.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    items(ExpenseCategory.entries, key = { it.name }) { option ->
                        FilterChip(
                            selected = category == option,
                            onClick = { category = option },
                            label = { Text("${option.emoji} ${option.title}", fontSize = 11.sp) },
                        )
                    }
                }
                Text("Ghi nhận vào ngày", fontWeight = FontWeight.SemiBold, color = pack.ink, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val today = LocalDate.now()
                    val selectedDay = Instant.ofEpochMilli(createdAt).atZone(ZoneId.systemDefault()).toLocalDate()
                    FilterChip(
                        selected = selectedDay == today,
                        onClick = { createdAt = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() },
                        label = { Text("Hôm nay") },
                    )
                    FilterChip(
                        selected = selectedDay == today.minusDays(1),
                        onClick = { createdAt = today.minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() },
                        label = { Text("Hôm qua") },
                    )
                    Text(formatDate(createdAt), Modifier.align(Alignment.CenterVertically), color = pack.mutedInk, fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title.ifBlank { category.title }, parsedAmount, category, createdAt, fundId) },
                enabled = parsedAmount > 0,
                colors = ButtonDefaults.buttonColors(containerColor = pack.secondary),
            ) { Text(if (initial == null) "Lưu khoản chi" else "Lưu thay đổi") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Để sau", color = pack.mutedInk) } },
    )
}

@Composable
private fun PageTitle(title: String, subtitle: String, pack: ThemePack) {
    Column {
        Text(title, fontFamily = AppFontFamily, fontSize = 29.sp, fontWeight = FontWeight.Bold, color = pack.ink)
        Text(subtitle, color = pack.mutedInk, fontSize = 12.sp)
    }
}

@Composable
private fun SectionTitle(title: String, action: String, pack: ThemePack) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, Modifier.weight(1f), fontFamily = AppFontFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = pack.ink)
        if (action.isNotEmpty()) Text(action, color = pack.primary, fontSize = 11.sp)
    }
}

@Composable
private fun EmptyCard(text: String, pack: ThemePack) {
    Surface(color = pack.surface.copy(alpha = 0.90f), shape = RoundedCornerShape(17.dp)) {
        Text(text, Modifier.fillMaxWidth().padding(15.dp), color = pack.mutedInk, fontSize = 12.sp)
    }
}

private fun lumiReminder(state: DashboardState): String {
    val budget = state.monthlyBudget.coerceAtLeast(1)
    val ratio = state.spentThisMonth.toFloat() / budget
    val overdrawnFund = state.funds.firstOrNull { it.balance < 0 }
    val lowFund = state.funds.firstOrNull { fund ->
        fund.balance in 0..(state.monthlyBudget / 10).coerceAtLeast(100_000)
    }
    val nearLimit = ExpenseCategory.entries.firstOrNull { category ->
        val categoryBudget = state.categoryBudgets[category.name] ?: return@firstOrNull false
        if (categoryBudget <= 0) return@firstOrNull false
        state.transactionsThisMonth.filter { it.category == category }.sumOf { it.amount } >= categoryBudget * 0.85
    }
    return when {
        overdrawnFund != null -> "Quỹ ${overdrawnFund.name} đang âm ${formatMoney(-overdrawnFund.balance)}. Mình cân đối lại nhé!"
        lowFund != null && state.transactions.isNotEmpty() -> "Quỹ ${lowFund.name} chỉ còn ${formatMoney(lowFund.balance)}. Lumi nhắc nhẹ nè."
        state.transactionsThisMonth.isEmpty() -> "Ví tháng này thật yên bình. Mình bắt đầu ghi chi tiêu nhé?"
        nearLimit != null -> "Hũ ${nearLimit.title} sắp đầy rồi đó. Mình cân nhắc nhẹ nha!"
        ratio >= 0.85f -> "Chúng mình đã dùng hơn 85% ngân sách tháng rồi nè."
        ratio >= 0.60f -> "Đi được hơn nửa ngân sách rồi. Lumi vẫn theo dõi cùng bạn!"
        else -> "Bạn vẫn còn ${formatMoney(state.monthlyBudget - state.spentThisMonth)}. Làm tốt lắm!"
    }
}

private fun formatMoney(amount: Long): String = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN")).format(amount) + " ₫"

private fun formatCompact(amount: Long): String {
    val sign = if (amount < 0) "-" else ""
    val absolute = kotlin.math.abs(amount)
    return sign + when {
        absolute >= 1_000_000 -> "${"%.1f".format(Locale.US, absolute / 1_000_000f)}tr"
        absolute >= 1_000 -> "${absolute / 1_000}k"
        else -> absolute.toString()
    }
}

private fun formatDate(epochMillis: Long): String = Instant.ofEpochMilli(epochMillis)
    .atZone(ZoneId.systemDefault())
    .format(DateTimeFormatter.ofPattern("dd/MM"))

private fun parseQuickExpense(input: String): Triple<String, Long, ExpenseCategory>? {
    val match = Regex("""^(.+?)\s+(\d[\d.,]*)\s*(k|nghìn|tr|triệu)?$""", RegexOption.IGNORE_CASE)
        .matchEntire(input.trim()) ?: return null
    val title = match.groupValues[1].trim()
    val rawAmount = match.groupValues[2]
    val suffix = match.groupValues[3].lowercase()
    val amount = when (suffix) {
        "k", "nghìn" -> (rawAmount.replace(',', '.').toDoubleOrNull()?.times(1_000))?.toLong()
        "tr", "triệu" -> (rawAmount.replace(',', '.').toDoubleOrNull()?.times(1_000_000))?.toLong()
        else -> rawAmount.filter(Char::isDigit).toLongOrNull()
    } ?: return null
    if (amount <= 0) return null
    val normalized = title.lowercase()
    val category = when {
        listOf("cà phê", "ca phe", "cafe", "cơm", "com", "ăn", "an ", "trà", "tra ", "bánh", "banh").any(normalized::contains) -> ExpenseCategory.FOOD
        listOf("xăng", "xang", "grab", "taxi", "xe", "gửi", "gui xe").any(normalized::contains) -> ExpenseCategory.TRANSPORT
        listOf("áo", "ao ", "quần", "quan", "mua", "shop").any(normalized::contains) -> ExpenseCategory.SHOPPING
        listOf("phim", "game", "nhạc", "nhac", "chơi", "choi").any(normalized::contains) -> ExpenseCategory.FUN
        listOf("điện", "dien", "nước", "nuoc", "wifi", "thuê", "thue").any(normalized::contains) -> ExpenseCategory.BILLS
        else -> ExpenseCategory.OTHER
    }
    return Triple(title, amount, category)
}
