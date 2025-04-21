<h1 align="center">🏋️‍♀️ AiRise – Your Smart Personal Health Companion</h1>

<p align="center">
  <i>An AI-powered fitness and wellness platform designed to provide personalized coaching, real-time progress tracking, smart nutrition advice, and gamified motivation.</i>
</p>

<p align="center">
  <img src="Media/banner.jpg" alt="AiRise Banner" width="100%">
</p>

---

<h2>📱 Overview</h2>

<p>
AiRise bridges the gap between convenience and results. Built with full-stack flexibility and mobile optimization in mind, it empowers users to take control of their health with personalized routines, meal planning, progress tracking, and smart-device integrations—all driven by AI.
</p>

<h3>✨ Key Features</h3>

<ul>
  <li><b>AI-Powered Coaching:</b> Personalized workouts, meal plans, and goal tracking.</li>
  <li><b>Smart Progress Tracker:</b> Visual analytics and adaptive goal scaling.</li>
  <li><b>Meal Planner & Nutrition Tracker:</b> Daily intake tracking and meal suggestions.</li>
  <li><b>Gamification & Social Engagement:</b> Leaderboards, streaks, and community challenges.</li>
  <li><b>Smart Device Integration:</b> Connect with fitness trackers and health apps.</li>
  <li><b>Push & Email Notifications:</b> Stay on track with timely alerts and weekly summaries.</li>
</ul>

---

<h2>📂 Project Structure</h2>

```
/frontend   # Android (Kotlin) mobile application
/backend    # RESTful backend API (.NET 9)
```

<p>Each folder contains its own README with setup details and architecture.</p>

---

<h2>🚀 Getting Started</h2>

<h3>✅ Prerequisites</h3>

<ul>
  <li>.NET 9 SDK</li>
  <li>Android Studio</li>
  <li>Kotlin Multiplatform</li>
</ul>

<h3>⚙️ Environment Setup</h3>

```bash
git clone https://github.com/JSDWRLD/AiRise
cd airise
```

<b>Frontend (Mobile App):</b>

```bash
cd frontend
# Open in Android Studio
```

<b>Backend (API):</b>

```bash
cd backend
# Open in Visual Studio
```

---

<h2>📸 Visuals</h2>

<details>
  <summary>📱 Core Features</summary>
  <table>
    <tr>
      <td align="center"><b>Homescreen</b><br><img src="Media/home.png" width="250"/></td>
      <td align="center"><b>AI Coach Chat</b><br><img src="Media/chat.png" width="250"/></td>
    </tr>
    <tr>
      <td align="center"><b>Workouts</b><br><img src="Media/workout.png" width="250"/></td>
      <td></td>
    </tr>
  </table>
</details>

<details>
  <summary>⚙️ Onboarding & Settings</summary>
  <table>
    <tr>
      <td align="center"><b>Onboarding UI</b><br><img src="Media/onboard.png" width="250"/></td>
      <td></td>
    </tr>
  </table>
</details>

---

<h2>🧠 Architecture</h2>

<p align="center">
  <img src="Media/erd.png" alt="Architecture Diagram" width="75%">
</p>

<p align="center"><i>An overview of the system: mobile frontend ↔ backend services ↔ database & third-party APIs</i></p>

---

<h2>🛠️ Technologies</h2>

<table>
  <tr>
    <th>Frontend</th>
    <th>Backend</th>
    <th>AI & ML</th>
    <th>Database</th>
    <th>Integrations</th>
  </tr>
  <tr>
    <td>Kotlin, KMP</td>
    <td>.NET 9, Firebase Auth</td>
    <td>Google Gemini, Image Processor</td>
    <td>MongoDB, SupaBase</td>
    <td>Google Fit, Apple Health, Wearables</td>
  </tr>
</table>

---

<h2>📌 Rules & Guidelines</h2>

<h3>🔀 Branching Strategy</h3>

- Always use feature branches: `git checkout -b feature/my-feature`
- Never push directly to `main`

<h3>✅ Pull Request Protocol</h3>

- At least <b>2 reviewers</b> must approve before merging.
- Ping the team on Discord (`@everyone`) for visibility.
- Keep PRs <b>small, atomic, and focused</b>.

<h3>✍️ Commit Format</h3>

```bash
type: short description
```

> Example: `feat: add AI meal suggestion endpoint`

<h3>🔐 Security</h3>

- Do <b>not</b> commit `.env` files or secrets.
- Use `.gitignore` to protect sensitive data.

---

<h2>🧪 Feature Checklist</h2>

<h3>✅ Core Features</h3>

- [x] AI-powered workout suggestions  
- [x] Nutrition and meal tracking  
- [x] Progress analytics  
- [x] Community and gamification  
- [x] Smart device integration  

<h3>🚧 Stretch Goals</h3>

- [ ] 📷 <b>Computer Vision</b> for form checking  
- [ ] 🥗 Meal image analysis for macronutrient estimation  

---

<h2>🤝 Contributing</h2>

Please read the <a href="CONTRIBUTING.md">CONTRIBUTING.md</a> for guidelines.

---

<h2>🙌 Credits</h2>

Made with ❤️ by <b>404 Not Found</b> at <b>California State University Sacramento</b>.  
Special thanks to our mentors, professors, and testers who supported development.

---

<blockquote>
  <p><i>"The only bad workout is the one that didn’t happen."</i><br>— Random Dude</p>
</blockquote>
