import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

search = """@Composable
fun DiagnosticRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color(0xFF64748B),
            fontSize = 12.sp
        )
        Text(
            text = value,
            color = Color(0xFF94A3B8),
            fontSize = 12.sp,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )
    }
}"""
content = content.replace(search, "")

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
