import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Gather state
gather_search = "val thunderEnabled by RainState.thunderEnabled.collectAsStateWithLifecycle()"
gather_replace = "val thunderEnabled by RainState.thunderEnabled.collectAsStateWithLifecycle()\n    val lightningInterval by RainState.lightningInterval.collectAsStateWithLifecycle()"
content = content.replace(gather_search, gather_replace)

# LaunchedEffect
effect_search = """    // Double-flicker Lightning animation loop
    LaunchedEffect(isStormMode, isPlaying, thunderEnabled) {
        if (isPlaying && thunderEnabled) {
            while (true) {
                // Flash every 7 to 18 seconds of deep storm
                val nextFlashDelay = Random.nextLong(7000, 18000)"""

effect_replace = """    // Double-flicker Lightning animation loop
    LaunchedEffect(isStormMode, isPlaying, thunderEnabled, lightningInterval) {
        if (isPlaying && thunderEnabled) {
            while (true) {
                val nextFlashDelay = when (lightningInterval) {
                    "Short" -> Random.nextLong(10000, 12000)
                    "Moderate" -> Random.nextLong(12000, 15000)
                    "Long" -> Random.nextLong(15000, 20000)
                    "Once in a while" -> Random.nextLong(20000, 60000)
                    else -> Random.nextLong(12000, 15000)
                }"""
content = content.replace(effect_search, effect_replace)

# Remove diagnostics and add dropdown
ui_search = """                            DiagnosticRow(label = "Active Audio Buffer", value = "4096 Bytes")
                            DiagnosticRow(label = "Sample Resolution", value = "44.1kHz stereo PCM")
                            DiagnosticRow(label = "Dynamic Rain Rate", value = "${(rainIntensity * 100).toInt()}%")
                            DiagnosticRow(label = "Wind Modulation", value = "${(kotlin.math.abs(windFrequency - 0.5f) * 200).toInt()}%")
                            DiagnosticRow(label = "Volume Setting", value = "${(volume * 100).toInt()}%")
                            DiagnosticRow(label = "Lightning Interval", value = if (thunderEnabled) "7s - 18s" else "Disabled")
                            
                            Spacer(modifier = Modifier.height(12.dp))"""

ui_replace = """                            var expanded by remember { mutableStateOf(false) }
                            val options = listOf("Short", "Moderate", "Long", "Once in a while")
                            
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                                        .clickable { expanded = true }
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Lightning Interval", color = Color.White, fontSize = 14.sp)
                                    Text(lightningInterval, color = Color(0xFF94A3B8), fontSize = 14.sp)
                                }
                                androidx.compose.material3.DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                    modifier = Modifier.background(Color(0xFF1E293B))
                                ) {
                                    options.forEach { option ->
                                        androidx.compose.material3.DropdownMenuItem(
                                            text = { Text(option, color = Color.White) },
                                            onClick = {
                                                RainState.lightningInterval.value = option
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))"""
content = content.replace(ui_search, ui_replace)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
