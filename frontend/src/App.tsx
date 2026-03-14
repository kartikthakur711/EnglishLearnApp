import { useEffect, useMemo, useState } from "react";

type AuthResponse = {
  token: string;
  loginId: string;
  displayName: string;
  levelBand: "MID" | "PRO";
  streak: number;
  totalPoints: number;
};

type Lesson = {
  id: number;
  chapterNo: number;
  chapterTitle: string;
  tenseName: string;
  levelBand: "MID" | "PRO";
  lessonText: string;
  practiceQuestion: string;
  sampleAnswer: string;
  completed: boolean;
  score: number;
};

type ChatHistory = {
  role: string;
  content: string;
  inputMode: string;
  createdAt: string;
};

const API = import.meta.env.VITE_API_BASE_URL ?? "";
const AUTH_KEY = "englishapp_auth";

export default function App() {
  const [auth, setAuth] = useState<AuthResponse | null>(null);
  const [mode, setMode] = useState<"login" | "register">("register");
  const [form, setForm] = useState({ loginId: "", password: "", displayName: "", levelBand: "MID" });
  const [chapters, setChapters] = useState<number[]>([]);
  const [selectedChapter, setSelectedChapter] = useState<number | "">("");
  const [level, setLevel] = useState<"MID" | "PRO">("MID");
  const [lessons, setLessons] = useState<Lesson[]>([]);
  const [answerMap, setAnswerMap] = useState<Record<number, string>>({});
  const [chatInput, setChatInput] = useState("");
  const [chat, setChat] = useState<ChatHistory[]>([]);
  const [feedback, setFeedback] = useState("");

  useEffect(() => {
    const raw = localStorage.getItem(AUTH_KEY);
    if (!raw) return;
    try {
      setAuth(JSON.parse(raw));
    } catch {
      localStorage.removeItem(AUTH_KEY);
    }
  }, []);

  useEffect(() => {
    if (!auth) return;
    localStorage.setItem(AUTH_KEY, JSON.stringify(auth));
    setLevel(auth.levelBand);
    loadLearn(auth.token, auth.levelBand, "");
    loadChatHistory(auth.token);
    refreshStreak(auth.token);
  }, [auth?.token]);

  const authHeader = useMemo(() => (auth ? { Authorization: `Bearer ${auth.token}` } : {}), [auth]);

  async function authSubmit() {
    const path = mode === "register" ? "/api/auth/register" : "/api/auth/login";
    const payload = mode === "register"
      ? form
      : { loginId: form.loginId, password: form.password };

    const res = await fetch(`${API}${path}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });
    const data = await res.json();
    if (!res.ok) {
      setFeedback(data.message ?? "Auth failed");
      return;
    }
    setAuth(data);
    setFeedback("");
  }

  async function refreshStreak(token: string) {
    const res = await fetch(`${API}/api/streak`, { headers: { Authorization: `Bearer ${token}` } });
    if (!res.ok || !auth) return;
    const data = await res.json();
    setAuth((prev) => prev ? { ...prev, streak: data.streak, totalPoints: data.totalPoints } : prev);
  }

  async function loadLearn(token: string, lv: "MID" | "PRO", chapter: number | "") {
    const chapterQuery = chapter === "" ? "" : `&chapterNo=${chapter}`;

    const [chaptersRes, lessonsRes] = await Promise.all([
      fetch(`${API}/api/learn/chapters?level=${lv}`, { headers: { Authorization: `Bearer ${token}` } }),
      fetch(`${API}/api/learn/lessons?level=${lv}${chapterQuery}`, { headers: { Authorization: `Bearer ${token}` } })
    ]);

    if (chaptersRes.ok) {
      setChapters(await chaptersRes.json());
    }
    if (lessonsRes.ok) {
      setLessons(await lessonsRes.json());
    }
  }

  async function refreshLearn() {
    if (!auth) return;
    await loadLearn(auth.token, level, selectedChapter);
    await refreshStreak(auth.token);
  }

  async function submitAnswer(lessonId: number) {
    if (!auth) return;
    const answerText = answerMap[lessonId] ?? "";
    const res = await fetch(`${API}/api/learn/answer`, {
      method: "POST",
      headers: { "Content-Type": "application/json", ...authHeader },
      body: JSON.stringify({ lessonId, answerText })
    });
    const data = await res.json();
    if (!res.ok) {
      setFeedback(data.message ?? "Submit failed");
      return;
    }

    setFeedback(`Score: ${data.score}. ${data.feedback}`);
    setAuth({ ...auth, streak: data.streak, totalPoints: data.totalPoints });
    refreshLearn();
  }

  async function sendChat(voiceInput: boolean, text?: string) {
    if (!auth) return;
    const message = (text ?? chatInput).trim();
    if (!message) return;

    const res = await fetch(`${API}/api/ai/chat`, {
      method: "POST",
      headers: { "Content-Type": "application/json", ...authHeader },
      body: JSON.stringify({ message, voiceInput })
    });

    const data = await res.json();
    if (!res.ok) {
      setFeedback(data.message ?? "Chat failed");
      return;
    }

    setChatInput("");
    setFeedback(data.suggestedPractice);
    await loadChatHistory(auth.token);
    speakText(data.speakText);
  }

  async function loadChatHistory(token: string) {
    const res = await fetch(`${API}/api/ai/history`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    if (res.ok) {
      setChat(await res.json());
    }
  }

  function speakText(text: string) {
    const utterance = new SpeechSynthesisUtterance(text);
    utterance.rate = 0.95;
    window.speechSynthesis.cancel();
    window.speechSynthesis.speak(utterance);
  }

  function startVoiceInput() {
    const AnyWin = window as any;
    const Recognition = AnyWin.SpeechRecognition || AnyWin.webkitSpeechRecognition;
    if (!Recognition) {
      setFeedback("Voice input not supported in this browser.");
      return;
    }
    const rec = new Recognition();
    rec.lang = "en-US";
    rec.onresult = (event: any) => {
      const transcript = event.results?.[0]?.[0]?.transcript ?? "";
      if (transcript) {
        setChatInput(transcript);
        sendChat(true, transcript);
      }
    };
    rec.onerror = () => setFeedback("Voice input failed. Try again.");
    rec.start();
  }

  function logout() {
    localStorage.removeItem(AUTH_KEY);
    setAuth(null);
    setChat([]);
    setLessons([]);
    setFeedback("");
  }

  if (!auth) {
    return (
      <div className="container">
        <div className="card">
          <h1>English Tense Coach</h1>
          <p className="small">Mid to Pro, chapter-wise tense learning with AI type + voice conversation.</p>
          <div className="row">
            <button className={mode === "register" ? "" : "secondary"} onClick={() => setMode("register")}>Register</button>
            <button className={mode === "login" ? "" : "secondary"} onClick={() => setMode("login")}>Login</button>
          </div>
          <div className="row" style={{ marginTop: 12 }}>
            <input placeholder="Login ID" value={form.loginId} onChange={(e) => setForm({ ...form, loginId: e.target.value })} />
            <input type="password" placeholder="Password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} />
            {mode === "register" && (
              <>
                <input placeholder="Display Name" value={form.displayName} onChange={(e) => setForm({ ...form, displayName: e.target.value })} />
                <select value={form.levelBand} onChange={(e) => setForm({ ...form, levelBand: e.target.value })}>
                  <option value="MID">MID</option>
                  <option value="PRO">PRO</option>
                </select>
              </>
            )}
            <button onClick={authSubmit}>{mode === "register" ? "Create Account" : "Login"}</button>
          </div>
          {feedback && <p>{feedback}</p>}
        </div>
      </div>
    );
  }

  return (
    <div className="container">
      <div className="card">
        <div className="row" style={{ justifyContent: "space-between", alignItems: "center" }}>
          <div>
            <h1>Welcome, {auth.displayName}</h1>
            <p className="ok">Daily Streak: {auth.streak} day(s) | Points: {auth.totalPoints}</p>
          </div>
          <button className="secondary" onClick={logout}>Logout</button>
        </div>
      </div>

      <div className="card">
        <h2>Learn Section (Chapter-wise Tense)</h2>
        <div className="row">
          <select value={level} onChange={async (e) => {
            const lv = e.target.value as "MID" | "PRO";
            setLevel(lv);
            setSelectedChapter("");
            if (auth) await loadLearn(auth.token, lv, "");
          }}>
            <option value="MID">MID</option>
            <option value="PRO">PRO</option>
          </select>
          <select value={selectedChapter} onChange={async (e) => {
            const chapter = e.target.value ? Number(e.target.value) : "";
            setSelectedChapter(chapter);
            if (auth) await loadLearn(auth.token, level, chapter);
          }}>
            <option value="">All Chapters</option>
            {chapters.map((c) => <option key={c} value={c}>{`Chapter ${c}`}</option>)}
          </select>
        </div>

        {lessons.map((lesson) => (
          <div key={lesson.id} className="card">
            <h3>{lesson.chapterTitle} - {lesson.tenseName}</h3>
            <p>{lesson.lessonText}</p>
            <button className="secondary" onClick={() => speakText(`${lesson.tenseName}. ${lesson.lessonText}. Question: ${lesson.practiceQuestion}`)}>
              AI Speak Lesson + Question
            </button>
            <p><b>Practice:</b> {lesson.practiceQuestion}</p>
            <textarea
              rows={3}
              placeholder="Type your answer"
              value={answerMap[lesson.id] ?? ""}
              onChange={(e) => setAnswerMap({ ...answerMap, [lesson.id]: e.target.value })}
              style={{ width: "100%", marginBottom: 8 }}
            />
            <div className="row">
              <button onClick={() => submitAnswer(lesson.id)}>Submit Answer</button>
              <span className="small">Status: {lesson.completed ? "Completed" : "Pending"} | Score: {lesson.score}</span>
            </div>
          </div>
        ))}
      </div>

      <div className="card">
        <h2>AI Talk (Type + Voice Conversation)</h2>
        <div className="chat-box">
          {[...chat].reverse().map((c, i) => (
            <div key={i} className="msg">
              <b>{c.role}:</b> {c.content}
            </div>
          ))}
        </div>
        <div className="row" style={{ marginTop: 10 }}>
          <input
            style={{ flex: 1 }}
            placeholder="Ask tense question"
            value={chatInput}
            onChange={(e) => setChatInput(e.target.value)}
          />
          <button onClick={() => sendChat(false)}>Send Type</button>
          <button className="secondary" onClick={startVoiceInput}>Speak to AI</button>
        </div>
      </div>

      {feedback && <div className="card"><p>{feedback}</p></div>}
    </div>
  );
}
