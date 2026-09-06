import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Add states
state_search = "val rippleDurationMulti by RainState.rippleDurationMultiplier.collectAsStateWithLifecycle()"
state_replace = """val rippleDurationMulti by RainState.rippleDurationMultiplier.collectAsStateWithLifecycle()
    val rippleShapeMulti by RainState.rippleShapeMultiplier.collectAsStateWithLifecycle()
    val dropSizeMulti by RainState.rainDropSizeMultiplier.collectAsStateWithLifecycle()"""
content = content.replace(state_search, state_replace)

# Draw ripples
draw_ripples_search = """            // Draw concentric ripples (Puddle level)
            for (r in particleSystem.ripples) {
                drawOval(
                    color = Color(0xFF60A5FA).copy(alpha = r.alpha * 0.9f),
                    topLeft = Offset(r.x - r.radius, r.y - r.radius * 0.35f),
                    size = Size(r.radius * 2f, r.radius * 0.7f),
                    style = Stroke(width = 1.8.dp.toPx())
                )
                drawOval(
                    color = Color(0xFF38BDF8).copy(alpha = r.alpha * 0.7f),
                    topLeft = Offset(r.x - r.radius * 1.3f, r.y - r.radius * 0.35f * 1.3f),
                    size = Size(r.radius * 2.6f, r.radius * 0.7f * 1.3f),
                    style = Stroke(width = 0.8.dp.toPx())
                )
            }"""
draw_ripples_replace = """            // Draw concentric ripples (Puddle level)
            val shapeRatio = 0.05f + rippleShapeMulti * 0.95f
            for (r in particleSystem.ripples) {
                drawOval(
                    color = Color(0xFF60A5FA).copy(alpha = r.alpha * 0.9f),
                    topLeft = Offset(r.x - r.radius, r.y - r.radius * shapeRatio),
                    size = Size(r.radius * 2f, r.radius * 2f * shapeRatio),
                    style = Stroke(width = 1.8.dp.toPx())
                )
                drawOval(
                    color = Color(0xFF38BDF8).copy(alpha = r.alpha * 0.7f),
                    topLeft = Offset(r.x - r.radius * 1.3f, r.y - r.radius * shapeRatio * 1.3f),
                    size = Size(r.radius * 2.6f, r.radius * 2f * shapeRatio * 1.3f),
                    style = Stroke(width = 0.8.dp.toPx())
                )
            }"""
content = content.replace(draw_ripples_search, draw_ripples_replace)

# Draw drops
draw_drops_search = """            // Draw falling raindrop streaks (Wind slanted)
            val windSlope = (windFrequency - 0.5f) * 3500f
            for (d in particleSystem.drops) {
                val headX = d.x
                val headY = d.y
                
                val tailX = d.x - (windSlope / d.speed) * d.length
                val tailY = d.y - d.length
                
                val w = d.width.dp.toPx()"""
draw_drops_replace = """            // Draw falling raindrop streaks (Wind slanted)
            val windSlope = (windFrequency - 0.5f) * 3500f
            val dropScale = 0.2f + dropSizeMulti * 2.3f
            for (d in particleSystem.drops) {
                val headX = d.x
                val headY = d.y
                
                val scaledLength = d.length * dropScale
                val tailX = d.x - (windSlope / d.speed) * scaledLength
                val tailY = d.y - scaledLength
                
                val w = d.width.dp.toPx() * dropScale"""
content = content.replace(draw_drops_search, draw_drops_replace)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
