import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Replace all targetGroundY logic to use RainState.rippleLevel.value
content = content.replace(
    'targetGroundY = h * 0.65f + Random.nextFloat() * (h * 0.35f)',
    'targetGroundY = if (RainState.rippleLevel.value <= 0.01f) h + 100f else h * (1f - RainState.rippleLevel.value) + Random.nextFloat() * (h * RainState.rippleLevel.value)'
)

content = content.replace(
    'targetGroundY = height * 0.65f + Random.nextFloat() * (height * 0.35f)',
    'targetGroundY = if (RainState.rippleLevel.value <= 0.01f) height + 100f else height * (1f - RainState.rippleLevel.value) + Random.nextFloat() * (height * RainState.rippleLevel.value)'
)

content = content.replace(
    'd.targetGroundY = height * 0.65f + Random.nextFloat() * (height * 0.35f)',
    'd.targetGroundY = if (RainState.rippleLevel.value <= 0.01f) height + 100f else height * (1f - RainState.rippleLevel.value) + Random.nextFloat() * (height * RainState.rippleLevel.value)'
)

# And now UI changes for Mute:
# 1. Binaural -> Mute, Icons.Default.GraphicEq -> VolumeUp, Icons.Default.Pause -> VolumeOff
# Wait, currently it's:
# imageVector = if (isPlaying) Icons.Default.GraphicEq else Icons.Default.Pause
# tint = if (isPlaying) Color(0xFF60A5FA) else Color(0xFF475569)
# text = "Binaural"
# text = if (isPlaying) "Active" else "Paused"

# We change this to:
# isMuted
# imageVector = if (!isMuted) Icons.Default.VolumeUp else Icons.Default.VolumeOff
# tint = if (!isMuted) Color(0xFF60A5FA) else Color(0xFF475569)
# text = "Mute"
# text = if (!isMuted) "Audio On" else "Muted"

mute_search = """                                    Column(modifier = Modifier.weight(1f)) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Default.GraphicEq else Icons.Default.Pause,
                                            contentDescription = "Binaural Synthesis",
                                            tint = if (isPlaying) Color(0xFF60A5FA) else Color(0xFF475569),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = "Binaural",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (isPlaying) "Active" else "Paused",
                                            color = Color(0xFF64748B),
                                            fontSize = 10.sp
                                        )
                                    }"""
                                    
mute_replace = """                                    val isMuted by RainState.isMuted.collectAsStateWithLifecycle()
                                    Column(modifier = Modifier.weight(1f)) {
                                        Icon(
                                            imageVector = if (!isMuted) androidx.compose.material.icons.filled.VolumeUp else androidx.compose.material.icons.filled.VolumeOff,
                                            contentDescription = "Mute Toggle",
                                            tint = if (!isMuted) Color(0xFF60A5FA) else Color(0xFF475569),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = "Audio",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (!isMuted) "Active" else "Muted",
                                            color = Color(0xFF64748B),
                                            fontSize = 10.sp
                                        )
                                    }"""
content = content.replace(mute_search, mute_replace)

# Change the clickable action for the new Mute button
clickable_search = """                                        .clickable {
                                            val intent = Intent(context, RainAudioService::class.java).apply {
                                                action = RainAudioService.ACTION_TOGGLE
                                            }
                                            context.startService(intent)
                                            triggerTouchHaptic(context, 0.8f)
                                        }"""
clickable_replace = """                                        .clickable {
                                            RainState.isMuted.value = !RainState.isMuted.value
                                            triggerTouchHaptic(context, 0.8f)
                                        }"""
content = content.replace(clickable_search, clickable_replace)

# Fix wind modulation percentage
# DiagnosticRow(label = "Wind Modulation", value = "${(windFrequency * 100).toInt()}%")
wind_search = 'DiagnosticRow(label = "Wind Modulation", value = "${(windFrequency * 100).toInt()}%")'
wind_replace = 'DiagnosticRow(label = "Wind Modulation", value = "${(kotlin.math.abs(windFrequency - 0.5f) * 200).toInt()}%")'
content = content.replace(wind_search, wind_replace)

# Fix Settings title
content = content.replace('Text(\n                                text = "Binaural Synthesis Diagnostics",', 'Text(\n                                text = "Settings",')

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
