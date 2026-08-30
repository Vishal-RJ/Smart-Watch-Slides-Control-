# ⌚ Smart Watch Slides Control (Wear OS + PC)

[![Wear OS](https://img.shields.io/badge/Wear%20OS-3.0%2B-blue.svg)](https://developer.android.com/wear)
[![Android Studio](https://img.shields.io/badge/Kotlin-Jetpack%20Compose-purple.svg)](https://developer.android.com/jetpack/compose)
[![Python](https://img.shields.io/badge/Python-3.8%2B-green.svg)](https://www.python.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

An intuitive, low-latency, and battery-optimized presentation remote control system designed for **Samsung Galaxy Watch 4 / Wear OS 3+ smartwatches** and **Windows/Mac/Linux laptops**.

Navigate seamlessly through **PowerPoint, Google Slides, Keynote, Canva, and PDF** presentations using **wrist gestures** or **silent screen taps** with instant tactile haptic feedback.

---

## 🌟 Key Highlights & Features

### 1. Dual Control Modality

| Control Method | Trigger Action | Simulated Key | Haptic Feedback |
| :--- | :--- | :--- | :--- |
| **Single Tap (Anywhere)** | Tap watch face once | **Right Arrow (`→`)** | 1x Short Crisp Haptic Pulse |
| **Double Tap (Anywhere)** | Quick double-tap | **Left Arrow (`←`)** | 2x Dual Distinct Pulses |
| **Wrist Flick Outward** | Rapid forearm twist/flick right | **Right Arrow (`→`)** | 1x Short Crisp Haptic Pulse |
| **Wrist Flick Inward** | Rapid forearm twist/flick left | **Left Arrow (`←`)** | 2x Dual Distinct Pulses |
| **Long Press / Top Pill** | Hold screen or tap status pill | *Opens Settings / IP Config* | Medium Haptic Pulse |

### 2. 🦾 Hardware IMU Motion Recognition (Galaxy Watch 4)
* **50Hz Dedicated Background Sensor Polling:** Operates on an isolated `HandlerThread` without dropping frames on the Compose UI thread.
* **Gyroscope Angular Velocity Engine:** Measures rapid angular rotational acceleration ($\omega > 4.2 \text{ rad/s}$) on roll and yaw axes.
* **850ms Cooldown Recoil Filter:** Eliminates false triggers when your wrist returns to a resting position after a flick.
* **Toggleable via Settings:** Easily enable or disable hand gesture recognition from the in-app settings screen to conserve battery when desired.

### 3. 🕒 Ambient Background Clock & OLED Dark Mode
* **OLED Pure Black (`#000000`):** Turns off OLED pixels to maximize battery life during long presentation sessions.
* **Ambient Idle Clock:** Subtle muted grey (`#808080` @ 38% alpha) background clock displaying:
  * **Digital Time:** e.g., `10:42 PM`
  * **Day & Date:** e.g., `Sunday, Aug 30`
* **Liquid Wavy Ripple Canvas:** 60fps concentric sinusoidal wave animation expanding from the exact touch origin coordinates `(x, y)`.
* **Silent Navigation:** No disruptive on-screen text overlays; haptics confirm every slide transition blindly.

### 4. ⚡ High-Speed WebSocket Python Receiver Server
* Lightweight, standalone Python WebSocket server (`server.py`).
* Runs on `0.0.0.0:8765` and translates network payloads (`NEXT`, `PREV`) into native keyboard arrow keystrokes using `pyautogui`.
* Automatically discovers and displays all available local LAN IP addresses.

---

## 🏗️ System Architecture

```
                                          Local Wi-Fi Network
┌────────────────────────────────┐         WebSocket (Port 8765)         ┌────────────────────────────────┐
│   Samsung Galaxy Watch 4       │ ────────────────────────────────────► │      Laptop / PC Receiver      │
│   (Wear OS 3+ / Jetpack)       │                                       │          (Python 3)            │
│                                │ ◄──────────────────────────────────── │                                │
│ • Full-Screen Touch Detection  │             ACK / Status              │ • WebSocket Server (8765)      │
│ • IMU Gyroscope Gesture Engine │                                       │ • PyAutoGUI Keystroke Bridge   │
│ • Haptic Pulse Feedback        │                                       │ • PowerPoint / Keynote / PDF   │
│ • Ambient Background Clock     │                                       └────────────────────────────────┘
└────────────────────────────────┘
```

---

## 📁 Repository Structure

```
Smart-Watch-Slides-Control/
├── app/                                 # Wear OS 3+ Android Application
│   ├── build.gradle.kts                 # Dependencies & Build Configuration
│   ├── src/main/
│   │   ├── AndroidManifest.xml          # Permissions (INTERNET, VIBRATE, HIGH_SAMPLING_RATE_SENSORS)
│   │   ├── java/com/presentation/wearclicker/
│   │   │   ├── AppConfig.kt             # Default Server IP, Port, Preferences Keys
│   │   │   ├── MainActivity.kt          # Entry Activity (KeepScreenOn flag)
│   │   │   ├── gesture/
│   │   │   │   └── WristGestureDetector.kt # 50Hz Gyroscope & Accelerometer motion engine
│   │   │   ├── network/
│   │   │   │   ├── ConnectionState.kt   # Connection state models
│   │   │   │   └── PresentationWebSocketClient.kt # OkHttp WebSocket client with auto-reconnect
│   │   │   ├── ui/
│   │   │   │   ├── ClickerScreen.kt     # Main interaction screen & touch handling
│   │   │   │   ├── BackgroundClock.kt   # Muted grey ambient clock composable
│   │   │   │   ├── WavyRippleEffect.kt  # 60fps Canvas sinusoidal ripple animation
│   │   │   │   ├── IpConfigDialog.kt    # In-app IP editor & Gesture Toggle
│   │   │   │   ├── PresentationViewModel.kt # StateFlow lifecycle & sensor coordinator
│   │   │   │   └── theme/Theme.kt       # OLED Black color palette
│   │   │   └── util/
│   │   │       └── HapticHelper.kt      # Crisp single & dual vibration waveforms
│   │   └── res/                         # App icons, layouts, and strings
├── server/                              # Laptop Receiver Server
│   ├── server.py                        # Python WebSocket receiver & PyAutoGUI bridge
│   ├── requirements.txt                 # websockets, pyautogui
│   └── test_client.py                   # Local CLI diagnostic tester
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## 🚀 Getting Started

### 1. Start the Laptop Server

1. Open PowerShell or a terminal inside the `server/` directory:
   ```bash
   cd server
   pip install -r requirements.txt
   ```
2. Launch the receiver server:
   ```bash
   python server.py
   ```
3. The server terminal will display your laptop's local LAN IP address:
   ```
   ==============================================================
          WEAR OS PRESENTATION CLICKER - RECEIVER SERVER       
   ==============================================================
    Server Port : 8765
    Available Laptop IP(s):
      -> ws://192.168.1.17:8765
   ==============================================================
   ```

*(Keep this terminal running in the background during your presentation)*.

---

### 2. Deploy to Samsung Galaxy Watch 4

#### Option A: Direct ADB Install over Wi-Fi
1. Enable **Developer Options** and **Wireless Debugging** on your Galaxy Watch.
2. Note the IP and pairing port on your watch, then connect via ADB:
   ```bash
   adb pair <WATCH_IP>:<PAIR_PORT>
   adb connect <WATCH_IP>:<PORT>
   ```
3. Install the pre-built debug APK:
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

#### Option B: Build & Run with Android Studio
1. Open the project folder in **Android Studio**.
2. Select your paired Galaxy Watch from the target device dropdown.
3. Click **Run (`Shift + F10`)**.

---

### 3. Connect & Present

1. Ensure the **Watch** and **Laptop** are connected to the **same Wi-Fi network** (or laptop mobile hotspot).
2. Open the **Wear Clicker** app on your watch.
3. **Set Laptop IP:**
   - Tap the top status pill or long-press the screen to open **Settings**.
   - Adjust the IP octets to match your laptop's IP address (e.g. `192.168.1.17`).
   - Toggle **Hand Gestures ON** (optional).
   - Tap **Save**.
4. The top status indicator will turn **green** (`Connected`).
5. Open your presentation (PowerPoint / Google Slides / Keynote / PDF) and present freely using wrist flicks or screen taps!

---

## 📜 License
This project is licensed under the MIT License.
