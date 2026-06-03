import sys

file_path = 'd:/update(27)/update(27)/signup/src/main/resources/templates/MyFiveYearPlan.html'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

start_marker = '<section class="panel">'
end_marker = '</script>'

start_idx = content.find(start_marker)
end_idx = content.rfind(end_marker) + len(end_marker)

if start_idx != -1 and end_idx != -1:
    new_content = """<section class="panel" style="background: transparent; border: none; box-shadow: none; padding: 0;">
            <div th:if="${successMessage}" class="message success" th:text="${successMessage}"></div>
            <div th:if="${errorMessage}" class="message error" th:text="${errorMessage}"></div>

            <!-- Auto-Generate Section (Shown if no plan) -->
            <div id="generatePlanContainer" style="display: none; background: rgba(30,64,175,0.1); border: 1px solid rgba(59,130,246,0.3); border-radius: 18px; padding: 1.5rem; margin-top: 0.5rem;">
                <h3 style="color:var(--accent); margin-bottom: 1rem; font-family:'Space Grotesk',sans-serif;">✨ Generate Auto-Plan</h3>
                <p style="color:var(--text-muted); margin-bottom: 1.25rem; font-size:0.95rem;">Select your path below to automatically generate a comprehensive 5-year roadmap, or <a href="/api/auth/home" style="color:var(--accent);">go to Home</a>.</p>
                <div style="display:grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1rem; margin-bottom: 1.25rem;">
                    <div class="field">
                        <label>Domain</label>
                        <select id="genDomain" style="background: rgba(10, 12, 16, 0.82);">
                            <option value="Frontend">Frontend</option>
                            <option value="Backend">Backend</option>
                            <option value="AI">AI</option>
                            <option value="Machine Learning">Machine Learning</option>
                            <option value="Data Science">Data Science</option>
                            <option value="Mobile">Mobile</option>
                            <option value="DevOps">DevOps</option>
                            <option value="Cloud">Cloud</option>
                            <option value="Security">Security</option>
                        </select>
                    </div>
                    <div class="field">
                        <label>Language/Tech</label>
                        <input type="text" id="genLanguage" placeholder="e.g. React, Python, Java" style="background: rgba(10, 12, 16, 0.82);">
                    </div>
                    <div class="field">
                        <label>Level</label>
                        <select id="genLevel" style="background: rgba(10, 12, 16, 0.82);">
                            <option value="Beginner">Beginner</option>
                            <option value="Advanced">Advanced</option>
                        </select>
                    </div>
                </div>
                <button type="button" class="btn btn-primary" onclick="generateDynamicPlan()" style="width:100%;">Generate Plan</button>
            </div>

            <!-- Generated Plan Cards (Shown if plan exists) -->
            <div id="planCardsContainer" style="display: none; flex-direction: column; gap: 1rem;">
                <div style="margin-bottom: 1rem; background: rgba(22, 27, 34, 0.92); border: 1px solid var(--card-border); border-radius: 16px; padding: 1.5rem;">
                    <h2 id="displayTitle" style="font-family: 'Space Grotesk', sans-serif; font-size: 1.5rem; margin-bottom: 0.5rem; color: var(--text-main);"></h2>
                    <p id="displayVision" style="color: var(--text-muted); line-height: 1.6; font-size: 1.05rem;"></p>
                </div>
                <div id="cardsWrapper" style="display: flex; flex-direction: column; gap: 1rem;"></div>
            </div>

        </section>
    </main>

    <script>
        function renderPlan(data) {
            if (!data.plan || !data.plan.id) {
                document.getElementById('generatePlanContainer').style.display = 'block';
                document.getElementById('globalProgressContainer').style.display = 'none';
                document.getElementById('planCardsContainer').style.display = 'none';
            } else {
                document.getElementById('generatePlanContainer').style.display = 'none';
                document.getElementById('planCardsContainer').style.display = 'flex';
                
                if (data.plan.progress !== undefined && data.plan.progress !== null) {
                    document.getElementById('globalProgressContainer').style.display = 'block';
                    document.getElementById('globalProgressBar').style.width = data.plan.progress + '%';
                    document.getElementById('globalProgressText').textContent = 'Progress: ' + data.plan.progress + '%';
                    
                    const statusEl = document.getElementById('globalProgressStatus');
                    if (data.plan.progress >= 100) {
                        statusEl.textContent = '✅ Completed';
                        statusEl.style.background = 'rgba(52, 211, 153, 0.2)';
                        statusEl.style.color = 'var(--success)';
                    } else {
                        statusEl.textContent = 'In Progress';
                        statusEl.style.background = 'rgba(59, 130, 246, 0.2)';
                        statusEl.style.color = 'var(--accent)';
                    }
                }

                document.getElementById('displayTitle').textContent = data.plan.title || '';
                document.getElementById('displayVision').textContent = data.plan.vision || '';

                const wrapper = document.getElementById('cardsWrapper');
                wrapper.innerHTML = '';
                
                if (data.goals || data.years) {
                    const goals = data.goals || data.years;
                    goals.forEach((goal) => {
                        const card = document.createElement('div');
                        card.style.cssText = "background: rgba(22, 27, 34, 0.92); border: 1px solid var(--card-border); border-radius: 16px; padding: 1.25rem; box-shadow: 0 10px 30px rgba(0,0,0,0.15); transition: transform 0.2s;";
                        
                        const isCompleted = goal.status === 'COMPLETED';
                        const checkIcon = isCompleted ? '✅' : '✔';
                        
                        card.innerHTML = `
                            <div style="display:flex; justify-content:space-between; align-items:flex-start;">
                                <div>
                                    <h3 style="font-size:1.1rem; font-weight:600; margin-bottom:8px; display:flex; align-items:center; gap:8px;">
                                        <span style="font-size: 1.2rem; color: var(--success);">${checkIcon}</span> 
                                        <span style="color:var(--accent);">Year ${goal.yearNumber || goal.year}</span> <span style="color: var(--text-muted);">→</span> ${goal.goalTitle || goal.title}
                                    </h3>
                                    <p style="color:var(--text-muted); font-size:0.95rem; line-height:1.5; padding-left:34px;">${goal.goalDescription || goal.description}</p>
                                </div>
                            </div>
                        `;
                        wrapper.appendChild(card);
                    });
                }
            }
        }

        async function loadPlanData() {
            try {
                const res = await fetch("/api/auth/my-five-year-plan", { credentials: 'include' });
                if (res.ok) {
                    const data = await res.json();
                    renderPlan(data);
                }
            } catch (e) { console.error('Failed to load plan data', e); }
        }

        async function generateDynamicPlan() {
            const domain = document.getElementById('genDomain').value;
            const language = document.getElementById('genLanguage').value;
            const level = document.getElementById('genLevel').value;

            if (!language) {
                alert("Please enter a language/tech");
                return;
            }

            try {
                const profRes = await fetch('/api/auth/profile', { credentials: 'include' });
                if (!profRes.ok) return;
                const profile = await profRes.json();

                const csrfToken = document.querySelector('input[name="_csrf"]')?.value || '';

                const res = await fetch('/api/auth/generate-plan', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json', 'X-CSRF-TOKEN': csrfToken },
                    body: JSON.stringify({ userId: profile.id, domain: domain, language: language, level: level }),
                    credentials: 'include'
                });

                if (res.ok) {
                    const newData = await res.json();
                    const formattedData = {
                        plan: {
                            id: newData.plan_id,
                            title: newData.language + " Mastery Roadmap",
                            vision: "To become an expert in " + newData.domain + " using " + newData.language,
                            progress: newData.progress || 0
                        },
                        goals: newData.years
                    };
                    renderPlan(formattedData);
                } else {
                    alert("Failed to generate plan. Try again.");
                }
            } catch(e) {
                console.error(e);
                alert("Failed to generate plan. Try again.");
            }
        }

        // Initialize plan data on load
        loadPlanData();
    </script>"""
    
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(content[:start_idx] + new_content + content[end_idx:])
    print('Successfully modified MyFiveYearPlan.html')
else:
    print('Failed to find markers')
