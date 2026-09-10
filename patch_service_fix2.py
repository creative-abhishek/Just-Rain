import re

with open('app/src/main/java/com/example/RainAudioService.kt', 'r') as f:
    content = f.read()

# Remove thunderIds.add lines
content = re.sub(r'\s*thunderIds\.add\(sp\.load\(this, R\.raw\.thunder_\d, 1\)\)', '', content)

with open('app/src/main/java/com/example/RainAudioService.kt', 'w') as f:
    f.write(content)
