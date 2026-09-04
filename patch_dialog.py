import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

search = """        // Interactive Diagnostic Stats Overlay
        if (showStatsOverlay) {
            androidx.compose.ui.window.Dialog(onDismissRequest = { showStatsOverlay = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("stats_card"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0B0F19).copy(alpha = 0.95f)),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Settings",
                                color = Color(0xFF38BDF8),
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = { showStatsOverlay = false },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close, // placeholder for tiny close
                                    contentDescription = "Close",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        DiagnosticRow(label = "Active Audio Buffer", value = "4096 Bytes")
                        DiagnosticRow(label = "Sample Resolution", value = "44.1kHz stereo PCM")
                        DiagnosticRow(label = "Dynamic Rain Rate", value = "${(rainIntensity * 100).toInt()}%")
                        DiagnosticRow(label = "Wind Modulation", value = "${(kotlin.math.abs(windFrequency - 0.5f) * 200).toInt()}%")
                        DiagnosticRow(label = "Volume Setting", value = "${(volume * 100).toInt()}%")
                        DiagnosticRow(label = "Lightning Interval", value = if (thunderEnabled) "7s - 18s" else "Disabled")
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Ripple Animation Level", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        androidx.compose.material3.Slider(
                            value = rippleLevel,
                            onValueChange = { RainState.rippleLevel.value = it },
                            valueRange = 0f..1f,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        androidx.compose.material3.Button(
                            onClick = { 
                                imagePickerLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(
                                        androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                ) 
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
                        ) {
                            Icon(imageVector = Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Set Background Image", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        }"""

replace = """        // Interactive Diagnostic Stats Overlay
        if (showStatsOverlay) {
            val sliderInteractionSource = remember { MutableInteractionSource() }
            val isSliderDragged by sliderInteractionSource.collectIsDraggedAsState()
            val isSliderPressed by sliderInteractionSource.collectIsPressedAsState()
            val isSliderActive = isSliderDragged || isSliderPressed

            val overlayDimAlpha by androidx.compose.animation.core.animateFloatAsState(targetValue = if (isSliderActive) 0f else 0.6f, label = "")
            val contentAlpha by androidx.compose.animation.core.animateFloatAsState(targetValue = if (isSliderActive) 0f else 1f, label = "")
            val cardBgAlpha by androidx.compose.animation.core.animateFloatAsState(targetValue = if (isSliderActive) 0f else 0.95f, label = "")
            val cardBorderAlpha by androidx.compose.animation.core.animateFloatAsState(targetValue = if (isSliderActive) 0f else 1f, label = "")

            BackHandler { showStatsOverlay = false }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = overlayDimAlpha))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { showStatsOverlay = false }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSliderActive && rippleLevel > 0.01f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(rippleLevel)
                            .align(Alignment.BottomCenter)
                            .background(Color(0xFF60A5FA).copy(alpha = 0.2f))
                            .border(1.dp, Color(0xFF60A5FA).copy(alpha = 0.5f))
                    )
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .testTag("stats_card")
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        ),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0B0F19).copy(alpha = cardBgAlpha)),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B).copy(alpha = cardBorderAlpha))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Column(modifier = Modifier.alpha(contentAlpha)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Settings",
                                    color = Color(0xFF38BDF8),
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(
                                    onClick = { showStatsOverlay = false },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close, // placeholder for tiny close
                                        contentDescription = "Close",
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            DiagnosticRow(label = "Active Audio Buffer", value = "4096 Bytes")
                            DiagnosticRow(label = "Sample Resolution", value = "44.1kHz stereo PCM")
                            DiagnosticRow(label = "Dynamic Rain Rate", value = "${(rainIntensity * 100).toInt()}%")
                            DiagnosticRow(label = "Wind Modulation", value = "${(kotlin.math.abs(windFrequency - 0.5f) * 200).toInt()}%")
                            DiagnosticRow(label = "Volume Setting", value = "${(volume * 100).toInt()}%")
                            DiagnosticRow(label = "Lightning Interval", value = if (thunderEnabled) "7s - 18s" else "Disabled")
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Ripple Animation Level", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        androidx.compose.material3.Slider(
                            value = rippleLevel,
                            onValueChange = { RainState.rippleLevel.value = it },
                            valueRange = 0f..1f,
                            modifier = Modifier.fillMaxWidth(),
                            interactionSource = sliderInteractionSource
                        )
                        
                        Column(modifier = Modifier.alpha(contentAlpha)) {
                            Spacer(modifier = Modifier.height(8.dp))
                            androidx.compose.material3.Button(
                                onClick = { 
                                    imagePickerLauncher.launch(
                                        androidx.activity.result.PickVisualMediaRequest(
                                            androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    ) 
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
                            ) {
                                Icon(imageVector = Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Set Background Image", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }"""

if search in content:
    content = content.replace(search, replace)
    with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
        f.write(content)
    print("Replaced successfully")
else:
    print("Search string not found")

