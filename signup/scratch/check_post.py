file_path = r'd:\update(27)\update(4)\update(27)\signup\src\main\resources\templates\Roadmap.html'

with open(file_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

found_modal = False
found_submit = False

for i, line in enumerate(lines):
    if 'createPostModal' in line:
        print(f"Line {i+1} (Modal): {line.strip()}")
        found_modal = True
    if 'submitPost' in line:
        print(f"Line {i+1} (Submit): {line.strip()}")
        found_submit = True

if not found_modal:
    print("createPostModal not found in Roadmap.html")
if not found_submit:
    print("submitPost not found in Roadmap.html")
