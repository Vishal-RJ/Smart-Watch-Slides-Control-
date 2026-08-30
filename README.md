# ⌚ Wear OS Presentation Clicker & Laptop Receiver

A gesture-driven, low-latency, and battery-efficient Wear OS remote clicker for PowerPoint, Google Slides, Keynote, Canva, and PDF presentations.

---

## 🌟 Key Features

### 1. Gesture Interaction Model
- **Full-Screen Touch Target:** The entire screen acts as a single seamless touch surface. No need to look at the watch during presentations.
- **Single Tap:** Advance to the **Next Slide** (Sends `"NEXT"` payload, triggers a crisp short haptic pulse).
- **Double Tap (Fast consecutive taps):** Return to the **Previous Slide** (Sends `"PREV"` payload, triggers a distinct dual haptic pulse).
- **Debounced Precision:** Single taps are debounced against double-tap gestures to prevent accidental forward transitions when double-tapping backward.

### 2. OLED Black Theme & Liquid Wavy Ripple Animation
- **Pure Solid Black Background (`#000000`):** Maximizes OLED battery efficiency on Wear OS smartwatches (Samsung Galaxy Watch 4/5/6, Pixel Watch, TicWatch).
- **Sinusoidal Concentric Wave Rings:**
  - Originates directly from the touch coordinate `(x, y)`.
  - Expanding concentric rings with harmonic sinusoidal path distortions along the circumference.
  - Undulates and fades smoothly in Light Blue (`#4FC3F7` / `#81D4FA`).
  - Rendered at 60fps on Jetpack Compose `Canvas`.
- **Always-On Display Flag:** Keeps the watch screen active throughout your presentation.

### 3. Python Receiver Server (`server.py`)
- Standalone WebSocket server running on `0.0.0.0:8765`.
- Simulates native **Right Arrow** (`pyautogui.press('right')`) and **Left Arrow** (`pyautogui.press('left')`).
- Auto-detects and displays all active local LAN IP addresses on startup.
- Zero external dependencies beyond `websockets` and `pyautogui`.

---

## 📁 Project Architecture

```
watch ppt control/
├── app/                                 # Wear OS 3+ Compose Module
│   ├── build.gradle.kts                 # Android & Compose dependencies
│   ├── src/main/
│   │   ├── AndroidManifest.xml          # Permissions (INTERNET, VIBRATE, WAKE_LOCK)
│   │   ├── java/com/presentation/wearclicker/
│   │   │   ├── AppConfig.kt             # Default Server IP & Port (ws://IP:8765)
│   │   │   ├── MainActivity.kt          # Main Activity & KeepScreenOn flag
│   │   │   ├── network/
│   │   │   │   ├── ConnectionState.kt   # Sealed connection states
│   │   │   │   └── PresentationWebSocketClient.kt # OkHttp WebSocket with auto-reconnect
│   │   │   ├── ui/
│   │   │   │   ├── ClickerScreen.kt     # Full-screen gesture detector & HUD
│   │   │   │   ├── WavyRippleEffect.kt  # 60fps Canvas sinusoidal expanding ripple
│   │   │   │   ├── IpConfigDialog.kt    # On-watch OLED IP adjuster
│   │   │   │   ├── PresentationViewModel.kt
│   │   │   │   └── theme/Theme.kt       # OLED Black & Light Blue palette
│   │   │   └── util/
│   │   │       └── HapticHelper.kt      # Short & Double vibration patterns
│   │   └── res/                         # Values, colors, styles, launcher icons
├── server/                              # Laptop Server
│   ├── server.py                        # Python WebSocket receiver
│   ├── requirements.txt                 # websockets, pyautogui
│   └── test_client.py                   # Local CLI client tester
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/gradle-wrapper.properties
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## 🚀 Quick Setup & Usage

### Step 1: Run the Laptop Server

1. Open PowerShell or terminal in the `server` directory:
   ```bash
   cd server
   pip install -r requirements.txt
   ```
2. Start the server:
   ```bash
   python server.py
   ```
3. The server prints your laptop's local LAN IP:
   ```
   ==============================================================
          WEAR OS PRESENTATION CLICKER - RECEIVER SERVER       
   ==============================================================
    Server Port : 8765
    Binding     : 0.0.0.0:8765
    Available Laptop IP(s):
      -> ws://192.168.1.105:8765
   --------------------------------------------------------------
    Configure your Watch app to connect to:
      >>> ws://192.168.1.105:8765 <<<
   ==============================================================
   ```

*(Keep this terminal running during your presentation)*

---

### Step 2: Configure & Launch Watch App

1. Ensure the Watch and Laptop are on the **same Wi-Fi network** (or Laptop Mobile Hotspot).
2. Set your laptop IP in `AppConfig.kt`:
   ```kotlin
   const val DEFAULT_LAPTOP_IP: String = "192.168.1.105"
   ```
   *(Or tap the status pill at the top of the watch screen / long-press anywhere on the watch to adjust the IP on the fly).*
3. Deploy to your smartwatch using Android Studio or ADB over Wi-Fi.

---

### Step 3: Presenting Gestures

| Gesture | Action | Feedback |
| :--- | :--- | :--- |
| **Single Tap (Anywhere)** | Next Slide (`"NEXT"`) | 1x Short Haptic Pulse + Light Blue Wavy Ripple |
| **Double Tap (Anywhere)** | Previous Slide (`"PREV"`) | 2x Distinct Haptic Pulses + Cyan Double Wave Burst |
| **Long Press / Top Pill** | Open IP Config Dialog | Medium Haptic Pulse |
