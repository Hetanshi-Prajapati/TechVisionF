file_path = r'd:\update(27)\update(4)\update(27)\signup\src\main\resources\templates\Roadmap.html'

with open(file_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if 'courseLinks' in line:
        print(f"Line {i+1}: {line.strip()}")
