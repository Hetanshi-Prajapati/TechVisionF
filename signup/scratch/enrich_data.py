import re
import json

file_path = r'd:\update(27)\update(4)\update(27)\signup\src\main\resources\templates\Roadmap.html'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

start_str = "const roadmapData = "
start_idx = content.find(start_str)
if start_idx == -1:
    print("Not found")
    exit()

stack = []
end_idx = -1
for i in range(start_idx + len(start_str), len(content)):
    if content[i] == '{':
        stack.append('{')
    elif content[i] == '}':
        stack.pop()
        if len(stack) == 0:
            end_idx = i
            break

json_str = content[start_idx + len(start_str):end_idx + 1]
try:
    data = json.loads(json_str)
except Exception as e:
    print("Parse error:", e)
    exit()

def enrich_lang(lang_name, domain):
    return {
        "1": {
            "title": f"{lang_name} Basics",
            "concepts": f"<b>What is {lang_name}?</b><br>A core technology used in {domain}.<br><br><b>Syntax & Basics</b><br>Learn the foundational structure and usage.<br><br><b>Advanced Features</b><br>Master the advanced capabilities.<br><br><b>Best Practices</b><br>Write clean and efficient code.",
            "learn": [
                f"What is {lang_name}",
                "Basic concepts",
                "Advanced usage",
                "Best practices",
                "Real-world application"
            ],
            "tasks": [
                {
                    "id": 1,
                    "title": "Setup Environment",
                    "desc": f"Install and configure {lang_name}.",
                    "example": f"// Initialization for {lang_name}\nstart_{lang_name.lower().replace(' ', '_')}();"
                },
                {
                    "id": 2,
                    "title": "First Project",
                    "desc": "Create your first basic script.",
                    "example": f"console.log('Hello {lang_name}!');"
                },
                {
                    "id": 3,
                    "title": "Advanced Integration",
                    "desc": "Use advanced features.",
                    "example": f"import {{ feature }} from '{lang_name.lower().replace(' ', '-')}';"
                }
            ],
            "project": f"Build a real-world application using {lang_name}",
            "quiz": [
                {
                    "q": f"What is the primary use of {lang_name}?",
                    "options": ["Database", f"{domain} development", "Operating System", "Networking"],
                    "ans": 1
                },
                {
                    "q": "Which is a core feature?",
                    "options": ["Syntax checking", "Hardware control", "Extensibility", "RAM management"],
                    "ans": 2
                },
                {
                    "q": f"How do you start a {lang_name} project?",
                    "options": ["Initialize env", "Buy a server", "Write HTML", "Compile OS"],
                    "ans": 0
                },
                {
                    "q": "Which of these is a best practice?",
                    "options": ["Hardcode values", "Write comments", "Ignore errors", "Skip tests"],
                    "ans": 1
                },
                {
                    "q": f"What does {lang_name} compile or run on?",
                    "options": ["Its specific runtime/engine", "A toaster", "Excel", "Only Linux"],
                    "ans": 0
                }
            ]
        }
    }

# Languages to enrich
langs = {
    "Frontend": ["HTML", "CSS", "JavaScript", "Angular", "Vue"],
    "Backend": ["Java", "Python", "PHP", "Spring Boot", "Django", "Express"],
    "AI": ["Keras"],
    "Machine Learning": ["TensorFlow", "PyTorch"],
    "Data Science": ["Pandas", "NumPy", "Matplotlib"],
    "Mobile": ["Java", "Kotlin", "Swift"],
    "DevOps": ["Docker", "Jenkins", "GitHub Actions"],
    "Cloud": ["Google Cloud"],
    "Security": ["Kali Linux", "Metasploit", "Burp Suite"]
}

for domain, ls in langs.items():
    if domain not in data:
        data[domain] = {}
    for l in ls:
        data[domain][l] = enrich_lang(l, domain)

new_json_str = json.dumps(data, indent=2)
new_content = content[:start_idx + len(start_str)] + new_json_str + content[end_idx + 1:]

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(new_content)

print("Successfully enriched roadmapData!")
