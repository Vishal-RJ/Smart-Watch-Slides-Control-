# ⌚ Wear OS Presentation Clicker & Laptop Receiver

A gesture-driven, low-latency, and battery-efficient Wear OS remote clicker for PowerPoint, Google Slides, Keynote, Canva, and PDF presentations.

---

## 🌟 Key Features

### 1. Dual Interaction Model: Touch + Wrist Gestures
- **Full-Screen Touch Target:** The entire screen acts as a seamless touch surface.
  - **Single Tap:** Advance to the **Next Slide** (`"NEXT"` payload, crisp short haptic pulse).
  - **Double Tap:** Return to the **Previous Slide** (`"PREV"` payload, distinct dual haptic pulse).
  - **Silent & Clean:** Slides advance immediately with tactile haptic feedback without on-screen text clutter.
- **Hardware IMU Wrist Gestures (Samsung Galaxy Watch 4 / Wear OS):**
  - **Wrist Flick / Twist Outward:** Triggers **Next Slide** with haptic confirmation.
  - **Wrist Flick / Twist Inward:** Triggers **Previous Slide** with dual haptic confirmation.
  - **Background Thread Polling:** 50Hz IMU sensor polling on a dedicated `HandlerThread` without UI lag.
  - **Debounce Cooldown:** 850ms cooldown prevents recoil/return swing false triggers.
  - **Toggle in Settings:** Enable or disable hand gestures anytime in the settings dialog.

### 2. Ambient Background Clock & OLED Theme
- **Ambient Idle Clock:** Subtle muted grey (`#808080` @ 38% alpha) background clock displaying:
  - **Current Time** (e.g. `10:42 PM`)
  - **Day of the Week & Date** (e.g. `Sunday, Aug 30`)
- **Pure Solid Black Background (`#000000`):** Maximizes OLED battery efficiency on Samsung Galaxy Watch 4/5/6 and Wear OS devices.
- **Liquid Sinusoidal Wavy Ripple Animation:** 60fps concentric wave ripple expanding smoothly from touch origin `(x, y)`.
- **Always-On Display / KeepScreenOn Flag:** Keeps the screen ready throughout your presentation.

### 3. Settings & In-App Config
- **Long Press / Top Pill:** Opens the IP and Settings dialog.
- **Adjust Laptop IP:** Easy stepper buttons (+1, +10, -1, -10) and quick subnet presets (`192.168.1.x`, `10.0.0.x`, etc.).
- **Gesture Control Toggle:** Turn `Hand Gestures (ON/OFF)` with state persistence across app restarts.

### 4. Python Receiver Server (`server.py`)
- Standalone WebSocket server running on `0.0.0.0:8765`.
- Simulates native **Right Arrow** (`pyautogui.press('right')`) and **Left Arrow** (`pyautogui.press('left')`).
- Auto-detects and displays all active local LAN IP addresses on startup.

---

## 📁 Project Architecture

```
watch ppt control/
├── app/                                 # Wear OS 3+ Compose Module
│   ├── build.gradle.kts                 # Android & Compose dependencies
│   ├── src/main/
│   │   ├── AndroidManifest.xml          # Permissions (INTERNET, VIBRATE, HIGH_SAMPLING_RATE_SENSORS)
│   │   ├── java/com/presentation/wearclicker/
│   │   │   ├── AppConfig.kt             # Default Server IP, Port & Settings Keys
│   │   │   ├── MainActivity.kt          # Main Activity & KeepScreenOn flag
│   │   │   ├── gesture/
│   │   │   │   └── WristGestureDetector.kt # 50Hz Background IMU Accelerometer + Gyroscope engine
│   │   │   ├── network/
│   │   │   │   ├── ConnectionState.kt   # Sealed connection states
│   │   │   │   └── PresentationWebSocketClient.kt # OkHttp WebSocket with auto-reconnect
│   │   │   ├── ui/
│   │   │   │   ├── ClickerScreen.kt     # Full-screen gesture detector & Ambient UI
│   │   │   │   ├── BackgroundClock.kt   # Muted grey ambient clock (Time, Date, Day)
│   │   │   │   ├── WavyRippleEffect.kt  # 60fps Canvas sinusoidal expanding ripple
│   │   │   │   ├── IpConfigDialog.kt    # OLED Settings, IP adjuster & Gesture Toggle
│   │   │   │   ├── PresentationViewModel.kt # StateFlow ViewModel & Sensor Coordinator
│   │   │   │   └── theme/Theme.kt       # OLED Black & Light Blue palette
│   │   │   └── util/
│   │   │       └── HapticHelper.kt      # Short & Double vibration patterns
│   │   └── res/                         # Values, colors, styles, launcher icons
├── server/                              # Laptop Server
│   ├── server.py                        # Python WebSocket receiver
│   ├── requirements.txt                 # websockets, pyautogui
│   └── test_client.py                   # Local CLI client tester
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
3. Note your laptop's local LAN IP displayed in the console (e.g., `ws://192.168.1.17:8765`).

---

### Step 2: Configure & Launch Watch App

1. Ensure the Watch and Laptop are on the **same Wi-Fi network**.
2. Long-press on the watch face or tap the top status pill to open **Settings**.
3. Set your laptop IP and optionally turn **Hand Gestures ON**.
4. Tap **Save**.

---

### Step 3: Presenting Gestures & Controls

| Action / Gesture | Command | Laptop Simulated Key | Tactile Feedback |
| :--- | :--- | :--- | :--- |
| **Single Tap (Anywhere)** | Next Slide (`"NEXT"`) | Right Arrow (`→`) | 1x Short Crisp Haptic Pulse |
| **Double Tap (Anywhere)** | Previous Slide (`"PREV"`) | Left Arrow (`←`) | 2x Dual Distinct Haptic Pulses |
| **Wrist Flick Outward (Twist Right)** | Next Slide (`"NEXT"`) | Right Arrow (`→`) | 1x Short Crisp Haptic Pulse |
| **Wrist Flick Inward (Twist Left)** | Previous Slide (`"PREV"`) | Left Arrow (`←`) | 2x Dual Distinct Haptic Pulses |
| **Long Press / Top Pill** | Open Settings | N/A | Medium Haptic Pulse |
