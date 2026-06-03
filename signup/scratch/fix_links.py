file_path = r'd:\update(27)\update(4)\update(27)\signup\src\main\resources\templates\Roadmap.html'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# I will replace the new_links block I added earlier with safer URLs
replacements = {
    '"HTML": "https://www.coursera.org/learn/html"': '"HTML": "https://www.coursera.org/learn/html-css-javascript-for-web-developers"',
    '"CSS": "https://www.coursera.org/learn/css"': '"CSS": "https://www.coursera.org/learn/html-css-javascript-for-web-developers"',
    '"JavaScript": "https://www.coursera.org/learn/javascript"': '"JavaScript": "https://www.coursera.org/learn/javascript-basics"',
    '"Angular": "https://www.coursera.org/learn/angular"': '"Angular": "https://www.coursera.org/learn/single-page-web-apps-with-angularjs"',
    '"Vue": "https://www.coursera.org/learn/vue-js"': '"Vue": "https://www.coursera.org/courses?query=vue.js"',
    '"Java": "https://www.coursera.org/learn/java"': '"Java": "https://www.coursera.org/learn/java-programming"',
    '"PHP": "https://www.coursera.org/learn/php"': '"PHP": "https://www.coursera.org/learn/web-applications-php"',
    '"Spring Boot": "https://www.coursera.org/learn/spring-framework"': '"Spring Boot": "https://www.coursera.org/courses?query=spring%20boot"',
    '"Django": "https://www.coursera.org/learn/django"': '"Django": "https://www.coursera.org/learn/django-web-framework"',
    '"Express": "https://www.coursera.org/learn/express"': '"Express": "https://www.coursera.org/courses?query=express%20js"',
    '"Keras": "https://www.coursera.org/learn/keras"': '"Keras": "https://www.coursera.org/courses?query=keras"',
    '"PyTorch": "https://www.coursera.org/learn/pytorch"': '"PyTorch": "https://www.coursera.org/learn/deep-neural-networks-with-pytorch"',
    '"Pandas": "https://www.coursera.org/learn/pandas"': '"Pandas": "https://www.coursera.org/learn/python-data-analysis"',
    '"NumPy": "https://www.coursera.org/learn/numpy"': '"NumPy": "https://www.coursera.org/courses?query=numpy"',
    '"Matplotlib": "https://www.coursera.org/learn/matplotlib"': '"Matplotlib": "https://www.coursera.org/courses?query=matplotlib"',
    '"Kotlin": "https://www.coursera.org/learn/kotlin-for-java-developers"': '"Kotlin": "https://www.coursera.org/learn/kotlin-for-java-developers"',
    '"Swift": "https://www.coursera.org/learn/swift"': '"Swift": "https://www.coursera.org/learn/swift-programming"',
    '"Docker": "https://www.coursera.org/learn/docker"': '"Docker": "https://www.coursera.org/learn/docker-essentials"',
    '"Jenkins": "https://www.coursera.org/learn/jenkins"': '"Jenkins": "https://www.coursera.org/courses?query=jenkins"',
    '"GitHub Actions": "https://www.coursera.org/learn/github-actions"': '"GitHub Actions": "https://www.coursera.org/courses?query=github%20actions"',
    '"Google Cloud": "https://www.coursera.org/learn/google-cloud"': '"Google Cloud": "https://www.coursera.org/learn/gcp-fundamentals"',
    '"Kali Linux": "https://www.coursera.org/learn/kali-linux"': '"Kali Linux": "https://www.coursera.org/courses?query=kali%20linux"',
    '"Metasploit": "https://www.coursera.org/learn/metasploit"': '"Metasploit": "https://www.coursera.org/courses?query=metasploit"',
    '"Burp Suite": "https://www.coursera.org/learn/burp-suite"': '"Burp Suite": "https://www.coursera.org/courses?query=burp%20suite"'
}

for old, new in replacements.items():
    content = content.replace(old, new)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Links updated successfully!")
