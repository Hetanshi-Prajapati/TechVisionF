from pathlib import Path
import re
root = Path(r'd:\update(27)\update(27)\signup\src\main\resources\templates')
replace_map = {
    'VTech': 'TechVision',
    'VTech Logo': 'TechVision Logo',
    'vtech.local': 'techvision.local',
    '/images/VT.png': '/images/LOGO.jpeg',
    '/images/logo.png': '/images/LOGO.jpeg',
    'type="image/png" href="/images/LOGO.jpeg"': 'type="image/jpeg" href="/images/LOGO.jpeg"',
    'alt="VTech"': 'alt="TechVision"',
    'alt="VTech Logo"': 'alt="TechVision Logo"',
}
count = 0
for path in root.rglob('*.html'):
    text = path.read_text(encoding='utf-8')
    original = text
    for old, new in replace_map.items():
        text = text.replace(old, new)
    def repl(m):
        group = m.group(1)
        if 'style=' in group:
            return re.sub(r'style="[^"]*"', 'style="height:40px;width:auto;object-fit:contain;vertical-align:middle;"', group) + m.group(2)
        return group + ' style="height:40px;width:auto;object-fit:contain;vertical-align:middle;"' + m.group(2)
    text = re.sub(r'(<img[^>]+src="/images/LOGO\.jpeg"[^>]*)(>)', repl, text)
    if text != original:
        path.write_text(text, encoding='utf-8')
        print('Updated', path)
        count += 1
print('Files updated:', count)
