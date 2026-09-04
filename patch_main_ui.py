import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Add states in MainScreen
state_search = "val thunderEnabled by RainState.thunderEnabled.collectAsStateWithLifecycle()"
state_replace = """val thunderEnabled by RainState.thunderEnabled.collectAsStateWithLifecycle()
    val rippleLevel by RainState.rippleLevel.collectAsStateWithLifecycle()
    val backgroundImageUri by RainState.backgroundImageUri.collectAsStateWithLifecycle()
    
    val imagePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> RainState.backgroundImageUri.value = uri }
    )"""
content = content.replace(state_search, state_replace)

# Draw AsyncImage behind everything
bg_search = """        // 1. Particle Layer (Rain & Splash)
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {"""
bg_replace = """        // Background Image
        if (backgroundImageUri != null) {
            coil.compose.AsyncImage(
                model = backgroundImageUri,
                contentDescription = "Background",
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 1. Particle Layer (Rain & Splash)
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {"""
content = content.replace(bg_search, bg_replace)

# Modify Settings Dialog to add slider and button
settings_search = """                        DiagnosticRow(label = "Lightning Interval", value = if (isStormMode) "7s - 18s" else "Disabled")
                    }
                }"""
settings_replace = """                        DiagnosticRow(label = "Lightning Interval", value = if (isStormMode) "7s - 18s" else "Disabled")
                        
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
                }"""
content = content.replace(settings_search, settings_replace)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
