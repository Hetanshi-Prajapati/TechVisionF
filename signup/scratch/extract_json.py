import re

file_path = r'd:\update(27)\update(4)\update(27)\signup\src\main\resources\templates\Roadmap.html'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Find the start of roadmapData
start_str = "const roadmapData = {"
start_idx = content.find(start_str)

if start_idx != -1:
    # Use a stack to find the matching closing brace
    stack = []
    end_idx = -1
    for i in range(start_idx + len("const roadmapData = "), len(content)):
        if content[i] == '{':
            stack.append('{')
        elif content[i] == '}':
            if len(stack) > 0:
                stack.pop()
            else:
                end_idx = i
                break
    
    if end_idx != -1:
        json_str = "{" + content[start_idx + len(start_str):end_idx + 1]
        print(f"Extracted json string of length {len(json_str)}")
        import json
        try:
            data = json.loads(json_str)
            print("Successfully parsed roadmapData!")
            print(f"Domains: {list(data.keys())}")
        except Exception as e:
            print(f"Error parsing: {e}")
    else:
        print("Could not find end of roadmapData")
else:
    print("Could not find start of roadmapData")
