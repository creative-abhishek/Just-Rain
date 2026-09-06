import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Expand interactive states for new sliders
search_interaction = """            val durationSliderInteractionSource = remember { MutableInteractionSource() }
            val isDurationSliderDragged by durationSliderInteractionSource.collectIsDraggedAsState()
            val isDurationSliderPressed by durationSliderInteractionSource.collectIsPressedAsState()
            val isDurationSliderActive = isDurationSliderDragged || isDurationSliderPressed

            val isSliderActive = isLevelSliderActive || isRadiusSliderActive || isDurationSliderActive"""

replace_interaction = """            val durationSliderInteractionSource = remember { MutableInteractionSource() }
            val isDurationSliderDragged by durationSliderInteractionSource.collectIsDraggedAsState()
            val isDurationSliderPressed by durationSliderInteractionSource.collectIsPressedAsState()
            val isDurationSliderActive = isDurationSliderDragged || isDurationSliderPressed

            val shapeSliderInteractionSource = remember { MutableInteractionSource() }
            val isShapeSliderDragged by shapeSliderInteractionSource.collectIsDraggedAsState()
            val isShapeSliderPressed by shapeSliderInteractionSource.collectIsPressedAsState()
            val isShapeSliderActive = isShapeSliderDragged || isShapeSliderPressed

            val dropSizeSliderInteractionSource = remember { MutableInteractionSource() }
            val isDropSizeSliderDragged by dropSizeSliderInteractionSource.collectIsDraggedAsState()
            val isDropSizeSliderPressed by dropSizeSliderInteractionSource.collectIsPressedAsState()
            val isDropSizeSliderActive = isDropSizeSliderDragged || isDropSizeSliderPressed

            val isSliderActive = isLevelSliderActive || isRadiusSliderActive || isDurationSliderActive || isShapeSliderActive || isDropSizeSliderActive"""
content = content.replace(search_interaction, replace_interaction)

# Reorder the contents of the column inside the card
search_card_content = """                            Spacer(modifier = Modifier.height(12.dp))
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
                            modifier = Modifier.fillMaxWidth().alpha(if (isSliderActive && !isLevelSliderActive) 0f else 1f),
                            interactionSource = levelSliderInteractionSource
                        )
                        
                        Column(modifier = Modifier.alpha(contentAlpha)) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Ripple Radius", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        androidx.compose.material3.Slider(
                            value = rippleRadiusMulti,
                            onValueChange = { RainState.rippleRadiusMultiplier.value = it },
                            valueRange = 0f..1f,
                            modifier = Modifier.fillMaxWidth().alpha(if (isSliderActive && !isRadiusSliderActive) 0f else 1f),
                            interactionSource = radiusSliderInteractionSource
                        )
                        
                        Column(modifier = Modifier.alpha(contentAlpha)) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Ripple Animation Duration", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        androidx.compose.material3.Slider(
                            value = rippleDurationMulti,
                            onValueChange = { RainState.rippleDurationMultiplier.value = it },
                            valueRange = 0f..1f,
                            modifier = Modifier.fillMaxWidth().alpha(if (isSliderActive && !isDurationSliderActive) 0f else 1f),
                            interactionSource = durationSliderInteractionSource
                        )
                        
                        Column(modifier = Modifier.alpha(contentAlpha)) {"""

replace_card_content = """                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Ripple Animation Level", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        androidx.compose.material3.Slider(
                            value = rippleLevel,
                            onValueChange = { RainState.rippleLevel.value = it },
                            valueRange = 0f..1f,
                            modifier = Modifier.fillMaxWidth().alpha(if (isSliderActive && !isLevelSliderActive) 0f else 1f),
                            interactionSource = levelSliderInteractionSource
                        )
                        
                        Column(modifier = Modifier.alpha(contentAlpha)) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Ripple Radius", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        androidx.compose.material3.Slider(
                            value = rippleRadiusMulti,
                            onValueChange = { RainState.rippleRadiusMultiplier.value = it },
                            valueRange = 0f..1f,
                            modifier = Modifier.fillMaxWidth().alpha(if (isSliderActive && !isRadiusSliderActive) 0f else 1f),
                            interactionSource = radiusSliderInteractionSource
                        )
                        
                        Column(modifier = Modifier.alpha(contentAlpha)) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Ripple Animation Duration", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        androidx.compose.material3.Slider(
                            value = rippleDurationMulti,
                            onValueChange = { RainState.rippleDurationMultiplier.value = it },
                            valueRange = 0f..1f,
                            modifier = Modifier.fillMaxWidth().alpha(if (isSliderActive && !isDurationSliderActive) 0f else 1f),
                            interactionSource = durationSliderInteractionSource
                        )
                        
                        Column(modifier = Modifier.alpha(contentAlpha)) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Ripple Oval Shape", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        androidx.compose.material3.Slider(
                            value = rippleShapeMulti,
                            onValueChange = { RainState.rippleShapeMultiplier.value = it },
                            valueRange = 0f..1f,
                            modifier = Modifier.fillMaxWidth().alpha(if (isSliderActive && !isShapeSliderActive) 0f else 1f),
                            interactionSource = shapeSliderInteractionSource
                        )

                        Column(modifier = Modifier.alpha(contentAlpha)) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Rain Drop Size", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        androidx.compose.material3.Slider(
                            value = dropSizeMulti,
                            onValueChange = { RainState.rainDropSizeMultiplier.value = it },
                            valueRange = 0f..1f,
                            modifier = Modifier.fillMaxWidth().alpha(if (isSliderActive && !isDropSizeSliderActive) 0f else 1f),
                            interactionSource = dropSizeSliderInteractionSource
                        )
                        
                        Column(modifier = Modifier.alpha(contentAlpha)) {
                            Spacer(modifier = Modifier.height(12.dp))
                            DiagnosticRow(label = "Active Audio Buffer", value = "4096 Bytes")
                            DiagnosticRow(label = "Sample Resolution", value = "44.1kHz stereo PCM")
                            DiagnosticRow(label = "Dynamic Rain Rate", value = "${(rainIntensity * 100).toInt()}%")
                            DiagnosticRow(label = "Wind Modulation", value = "${(kotlin.math.abs(windFrequency - 0.5f) * 200).toInt()}%")
                            DiagnosticRow(label = "Volume Setting", value = "${(volume * 100).toInt()}%")
                            DiagnosticRow(label = "Lightning Interval", value = if (thunderEnabled) "7s - 18s" else "Disabled")
                            
                            Spacer(modifier = Modifier.height(12.dp))"""
content = content.replace(search_card_content, replace_card_content)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
