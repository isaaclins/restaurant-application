# 🤖 Copilot Anweisungen für dieses Projekt

> Diese Datei definiert, wie der AI-Assistent in diesem Projekt agieren soll.

---

## 📋 Allgemeine Verhaltensregeln

### Sprache

- Antworte in der Sprache, in der der User schreibt (Deutsch oder Englisch)
- Code-Kommentare: Englisch
- Dokumentation: Deutsch (da Schulprojekt)

### Dokumentation

- **IMMER** relevante Änderungen in `DOCUMENTATION.md` festhalten
- Datum und Kontext bei jedem Eintrag

---

## 🎯 Reaktionen auf verschiedene Prompt-Typen

### 1. Ideen & Vorschläge

**Trigger**: "Ich denke...", "Was wenn wir...", "Wir sollten...", "I think we should..."

**Aktion**:

1. ✅ Idee analysieren und verstehen
2. 💬 Mit User diskutieren (Vor-/Nachteile, Alternativen)
3. 📝 In `DOCUMENTATION.md` unter "💡 Ideen & Verbesserungen" dokumentieren
4. ❓ Fragen ob Idee umgesetzt werden soll

**Beispiel-Antwort**:

```
Okay, Lass mich das durchdenken:

**Vorteile:**
- ...

**Nachteile/Bedenken:**
- ...

**Alternativen:**
- ...

Soll ich das in die Dokumentation aufnehmen? Und willst du es direkt umsetzen?
```

---

### 2. Probleme & Fehler

**Trigger**: "Es funktioniert nicht", "Fehler", "Error", "Bug", "Hilfe"

**Aktion**:

1. 🔍 Problem analysieren (Logs, Code, Kontext)
2. 🛠️ Lösung vorschlagen und umsetzen
3. 📝 In `DOCUMENTATION.md` unter "🐛 Probleme & Lösungen" dokumentieren
4. 💡 Präventionsmassnahmen vorschlagen

---

### 3. Feature-Implementierung

**Trigger**: "Implementiere...", "Erstelle...", "Baue...", "Create...", "Build..."

**Aktion**:

1. 📋 Aufgabe in `PROJECT_PLAN.md` checken (ist es ein Must-Have?)
2. 🏗️ Implementieren
3. ✅ Status in `PROJECT_PLAN.md` aktualisieren
4. 📝 In `DOCUMENTATION.md` Tageseintrag aktualisieren

---

### 4. Fragen & Erklärungen

**Trigger**: "Warum...?", "Wie funktioniert...?", "Erkläre..."

**Aktion**:

1. 📚 Erklären mit Beispielen
2. 🔗 Relevante Ressourcen verlinken
3. 📝 Bei wichtigen Erkenntnissen: In "Learnings" dokumentieren

---

### 5. Entscheidungen

**Trigger**: "Sollen wir X oder Y?", "Was ist besser?", "Welche Option?"

**Aktion**:

1. ⚖️ Optionen vergleichen (Tabelle mit Pros/Cons)
2. 💬 Empfehlung geben mit Begründung
3. 📝 Nach Entscheidung: In "Technische Entscheidungen" dokumentieren (ADR-Format)

---

### 6. Tages-Abschluss / Check-in

**Trigger**: "Was haben wir heute gemacht?", "Zusammenfassung", "Stand?"

**Aktion**:

1. 📊 Fortschritt zusammenfassen
2. ✅ Erledigte Tasks auflisten
3. 📝 Tageseintrag in `DOCUMENTATION.md` vervollständigen
4. 🎯 Nächste Schritte vorschlagen

---

## 📁 Projekt-Struktur

Bei Änderungen diese Struktur beachten:

```
restaurant-application/
├── .github/
│   └── copilot-instructions.md    # Diese Datei
├── README.md                       # Projekt-Übersicht
├── PROJECT_PLAN.md                 # Must-Haves & Zeitplan
├── DOCUMENTATION.md                # Entwicklungs-Tagebuch
├── docker-compose.yml              # Infrastruktur
├── eureka-server/                  # Service Discovery
├── api-gateway/                    # Gateway
├── product-catalog-service/        # Produktkatalog
├── cart-service/                   # Warenkorb
├── order-service/                  # Bestellungen
├── payment-service/                # Zahlung (Mockup)
└── frontend/                       # React App
```

---

## ⚠️ Wichtige Regeln

1. **Nie Code ändern ohne zu fragen**, ausser bei offensichtlichen Bugs
2. **Immer dokumentieren** – Schulprojekt erfordert Nachvollziehbarkeit
3. **Clean Code** – Lesbare Variablennamen, Kommentare wo nötig
4. **Kleine Commits** – Logische Einheiten, aussagekräftige Messages
5. **Tests nicht vergessen** – Mindestens Happy Path testen

---

## 🔄 Workflow

```
User-Input
    ↓
Verstehen & Kategorisieren
    ↓
Diskutieren (wenn nötig)
    ↓
Umsetzen
    ↓
Dokumentieren in DOCUMENTATION.md
    ↓
Status in PROJECT_PLAN.md aktualisieren
```

---

_Letzte Aktualisierung: 04.12.2025_
