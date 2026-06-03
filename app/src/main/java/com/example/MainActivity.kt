package com.example

import android.app.LocaleManager
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.TestRecord
import com.example.ui.SpeedTestViewModel
import com.example.ui.theme.MyApplicationTheme
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    private val viewModel: SpeedTestViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                var showLangMenu by remember { mutableStateOf(false) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    stringResource(R.string.app_name),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            actions = {
                                Box {
                                    IconButton(onClick = { showLangMenu = true }) {
                                        Icon(Icons.Default.Language, contentDescription = "Language")
                                    }
                                    DropdownMenu(
                                        expanded = showLangMenu,
                                        onDismissRequest = { showLangMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("English") },
                                            onClick = {
                                                setLocale("en")
                                                showLangMenu = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("中文") },
                                            onClick = {
                                                setLocale("zh")
                                                showLangMenu = false
                                            }
                                        )
                                    }
                                }
                                IconButton(onClick = { viewModel.clearHistory() }) {
                                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.clear_history))
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    MainScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun setLocale(languageCode: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val localeManager = getSystemService(LocaleManager::class.java)
            localeManager.applicationLocales = LocaleList.forLanguageTags(languageCode)
        } else {
            val locale = Locale(languageCode)
            Locale.setDefault(locale)
            val config = resources.configuration
            config.setLocale(locale)
            resources.updateConfiguration(config, resources.displayMetrics)
            recreate()
        }
    }
}

@Composable
fun MainScreen(viewModel: SpeedTestViewModel, modifier: Modifier = Modifier) {
    val engineState by viewModel.engineState.collectAsStateWithLifecycle()
    val history by viewModel.testHistory.collectAsStateWithLifecycle()

    var isClientMode by remember { mutableStateOf(viewModel.isClientMode) }
    var selectedRecordForDetail by remember { mutableStateOf<TestRecord?>(null) }

    if (selectedRecordForDetail != null) {
        HistoryDetailDialog(
            record = selectedRecordForDetail!!,
            onDismiss = { selectedRecordForDetail = null }
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            if (engineState.error != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Error: ${engineState.error}",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (engineState.isRunning) {
                ActiveTestView(
                    state = engineState,
                    onStop = { viewModel.stopTest() }
                )
            } else {
                TestConfigurationView(
                    viewModel = viewModel,
                    isClientMode = isClientMode,
                    onModeChange = { 
                        isClientMode = it 
                        viewModel.isClientMode = it
                    },
                    onStartClient = { host, port, up, threads, dur, udp, udpBand ->
                        viewModel.host = host
                        viewModel.port = port.toString()
                        viewModel.isUpload = up
                        viewModel.threadCount = threads.toFloat()
                        viewModel.duration = dur.toFloat()
                        viewModel.isUdp = udp
                        viewModel.udpBandwidth = udpBand
                        viewModel.startClient(host, port, up, threads, dur, udp, udpBand)
                    },
                    onStartServer = { port ->
                        viewModel.port = port.toString()
                        viewModel.startServer(port)
                    }
                )
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            Text(
                text = stringResource(R.string.history),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                fontWeight = FontWeight.SemiBold
            )
        }
        
        if (history.isEmpty()) {
            item {
                Text(
                    stringResource(R.string.no_history),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            items(history) { record ->
                HistoryCard(
                    record = record,
                    onClick = { selectedRecordForDetail = record },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun TestConfigurationView(
    viewModel: SpeedTestViewModel,
    isClientMode: Boolean,
    onModeChange: (Boolean) -> Unit,
    onStartClient: (String, Int, Boolean, Int, Int, Boolean, String) -> Unit,
    onStartServer: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var host by remember { mutableStateOf(viewModel.host) }
    var port by remember { mutableStateOf(viewModel.port) }
    var isUpload by remember { mutableStateOf(viewModel.isUpload) }
    var threadCount by remember { mutableFloatStateOf(viewModel.threadCount) }
    var isUdp by remember { mutableStateOf(viewModel.isUdp) }
    var duration by remember { mutableFloatStateOf(viewModel.duration) }
    var udpBandwidth by remember { mutableStateOf(viewModel.udpBandwidth) }

    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TabRow(selectedTabIndex = if (isClientMode) 0 else 1, modifier = Modifier.fillMaxWidth()) {
            Tab(selected = isClientMode, onClick = { onModeChange(true) }) {
                Text(stringResource(R.string.client), modifier = Modifier.padding(16.dp))
            }
            Tab(selected = !isClientMode, onClick = { onModeChange(false) }) {
                Text(stringResource(R.string.server), modifier = Modifier.padding(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isClientMode) {
            OutlinedTextField(
                value = host,
                onValueChange = { host = it },
                label = { Text(stringResource(R.string.server_ip_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = port,
                onValueChange = { port = it },
                label = { Text(stringResource(R.string.port_hint)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Upload / Download direction toggle
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                FilterChip(
                    selected = isUpload,
                    onClick = { isUpload = true },
                    label = { Text(stringResource(R.string.upload)) },
                    modifier = Modifier.padding(end = 8.dp)
                )
                FilterChip(
                    selected = !isUpload,
                    onClick = { isUpload = false },
                    label = { Text(stringResource(R.string.download)) }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // TCP / UDP Protocol toggle
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                FilterChip(
                    selected = !isUdp,
                    onClick = { isUdp = false },
                    label = { Text("TCP") },
                    modifier = Modifier.padding(end = 8.dp)
                )
                FilterChip(
                    selected = isUdp,
                    onClick = { isUdp = true },
                    label = { Text("UDP") }
                )
            }

            if (isUdp) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = udpBandwidth,
                    onValueChange = { udpBandwidth = it },
                    label = { Text(stringResource(R.string.udp_bandwidth_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(R.string.threads_count, threadCount.toInt()))
            Slider(
                value = threadCount,
                onValueChange = { threadCount = it },
                valueRange = 1f..64f,
                steps = 63,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(R.string.test_duration, duration.toInt()))
            Slider(
                value = duration,
                onValueChange = { duration = it },
                valueRange = 1f..120f,
                steps = 119,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = { 
                    val p = port.toIntOrNull() ?: 5201
                    onStartClient(host, p, isUpload, threadCount.toInt(), duration.toInt(), isUdp, udpBandwidth) 
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.start_test))
            }
        } else {
            OutlinedTextField(
                value = port,
                onValueChange = { port = it },
                label = { Text(stringResource(R.string.listen_port_hint)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { 
                    val p = port.toIntOrNull() ?: 5201
                    onStartServer(p) 
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Icon(Icons.Default.NetworkCheck, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.start_listener))
            }
        }
    }
}

@Composable
fun ActiveTestView(
    state: com.example.engine.SpeedTestState,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (state.error != null) {
            Text(
                "Error: ${state.error}", 
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )
        }

        Text(stringResource(R.string.bandwidth), style = MaterialTheme.typography.titleLarge)
        Text(
            text = String.format(Locale.US, "%.1f Mbps", state.currentBandwidthMbps),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Chart
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            val history = state.bandwidthHistory
            if (history.isNotEmpty()) {
                val maxVal = max(state.maxBandwidthMbps, 10f)
                val primaryColor = MaterialTheme.colorScheme.primary
                
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val points = history.mapIndexed { index, value ->
                        val x = if (history.size > 1) (index.toFloat() / (history.size - 1)) * width else width
                        val y = height - ((value / maxVal) * height)
                        Offset(x, y)
                    }
                    
                    val path = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            lineTo(points[i].x, points[i].y)
                        }
                    }
                    
                    drawPath(
                        path = path,
                        color = primaryColor,
                        style = Stroke(width = 4.dp.toPx())
                    )
                }
            } else {
                Text(
                    stringResource(if (state.isServer) R.string.waiting_for_client else R.string.connecting), 
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.average), style = MaterialTheme.typography.labelMedium)
                Text(String.format(Locale.US, "%.1f", state.avgBandwidthMbps), style = MaterialTheme.typography.titleMedium)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.min), style = MaterialTheme.typography.labelMedium)
                Text(String.format(Locale.US, "%.1f", state.minBandwidthMbps), style = MaterialTheme.typography.titleMedium)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.max), style = MaterialTheme.typography.labelMedium)
                Text(String.format(Locale.US, "%.1f", state.maxBandwidthMbps), style = MaterialTheme.typography.titleMedium)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        if (state.bandwidthHistory.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.interval_header), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text(stringResource(R.string.bitrate_header), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    state.bandwidthHistory.forEachIndexed { index, value ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${index}.00-${index + 1}.00s", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                            Text(String.format(Locale.US, "%.1f Mbps", value), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onStop, 
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Icon(Icons.Default.Stop, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.stop_test))
        }
    }
}

@Composable
fun HistoryCard(record: TestRecord, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val localizedType = when(record.type) {
        "CLIENT" -> stringResource(R.string.client)
        "SERVER" -> stringResource(R.string.server)
        else -> record.type
    }
    
    val localizedMode = when(record.mode) {
        "UPLOAD" -> stringResource(R.string.upload)
        "DOWNLOAD" -> stringResource(R.string.download)
        "LISTEN" -> stringResource(R.string.server_listen)
        else -> record.mode
    }
    
    val isZh = Locale.getDefault().language == "zh"
    val datePattern = if (isZh) "yyyy-MM-dd HH:mm" else "MMM dd, HH:mm"
    val date = SimpleDateFormat(datePattern, Locale.getDefault()).format(Date(record.timestamp))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "${localizedType} • ${localizedMode}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(text = date, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "${stringResource(R.string.host_label)} ${record.targetHost}:${record.port}", style = MaterialTheme.typography.bodyMedium)
                if (record.threadCount > 0) {
                    Text(text = "${stringResource(R.string.threads_label)} ${record.threadCount}", style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${stringResource(R.string.avg_bandwidth)} ${String.format(Locale.getDefault(), "%.1f", record.averageBandwidthMbps)} Mbps",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${stringResource(R.string.max_label)} ${String.format(Locale.getDefault(), "%.1f", record.maxBandwidthMbps)} Mbps",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${stringResource(R.string.min_label)} ${String.format(Locale.getDefault(), "%.1f", record.minBandwidthMbps)} Mbps",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun HistoryDetailDialog(record: TestRecord, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(R.string.test_details),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                val isZh = Locale.getDefault().language == "zh"
                val datePattern = if (isZh) "yyyy-MM-dd HH:mm:ss" else "MMM dd, yyyy HH:mm:ss"
                val dateStr = SimpleDateFormat(datePattern, Locale.getDefault()).format(Date(record.timestamp))
                
                val localizedType = when(record.type) {
                    "CLIENT" -> stringResource(R.string.client)
                    "SERVER" -> stringResource(R.string.server)
                    else -> record.type
                }
                
                val localizedMode = when(record.mode) {
                    "UPLOAD" -> stringResource(R.string.upload)
                    "DOWNLOAD" -> stringResource(R.string.download)
                    "LISTEN" -> stringResource(R.string.server_listen)
                    else -> record.mode
                }
                
                Text(
                    text = "${localizedType} • ${localizedMode}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(text = "${stringResource(R.string.test_time)} $dateStr", style = MaterialTheme.typography.bodyMedium)
                Text(text = "${stringResource(R.string.host_label)} ${record.targetHost}:${record.port}", style = MaterialTheme.typography.bodyMedium)
                
                if (record.threadCount > 0) {
                    Text(text = "${stringResource(R.string.threads_count, record.threadCount)}", style = MaterialTheme.typography.bodyMedium)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.average), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(String.format(Locale.getDefault(), "%.1f", record.averageBandwidthMbps), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Mbps", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.max), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(String.format(Locale.getDefault(), "%.1f", record.maxBandwidthMbps), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Mbps", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.min), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(String.format(Locale.getDefault(), "%.1f", record.minBandwidthMbps), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Mbps", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Interval details list
                val intervals = record.bandwidthHistoryString.split(",").mapNotNull { it.toFloatOrNull() }
                if (intervals.isNotEmpty()) {
                    Text(
                        stringResource(R.string.interval_details),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 180.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .verticalScroll(rememberScrollState())
                            .padding(12.dp)
                    ) {
                        intervals.forEachIndexed { index, valMbps ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${index}.00-${index + 1}.00s", style = MaterialTheme.typography.bodySmall)
                                Text(String.format(Locale.getDefault(), "%.1f Mbps", valMbps), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}
