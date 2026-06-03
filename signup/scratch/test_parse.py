import re
import json

file_path = r'd:\update(27)\update(4)\update(27)\signup\src\main\resources\templates\Roadmap.html'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

match = re.search(r'const roadmapData = (\{.*?\});\s*const currentDomain', content, re.DOTALL)
if match:
    json_str = match.group(1)
    try:
        data = json.loads(json_str)
        print("Successfully parsed roadmapData as JSON!")
        print(f"Domains: {list(data.keys())}")
    except json.JSONDecodeError as e:
        print(f"Failed to parse JSON: {e}")
else:
    print("Could not find roadmapData.")
