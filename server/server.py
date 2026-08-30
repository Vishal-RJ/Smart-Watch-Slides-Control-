import asyncio
import pyautogui
import websockets
import socket
import logging
import sys

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s [%(levelname)s] %(message)s',
    datefmt='%H:%M:%S'
)
logger = logging.getLogger(__name__)

# Disable pyautogui failsafe to avoid accidental crashes
pyautogui.FAILSAFE = False

async def handle_command(websocket):
    client_ip = websocket.remote_address[0]
    logger.info(f"🟢 Watch connected successfully from {client_ip}!")

    try:
        await websocket.send("CONNECTED_TO_LAPTOP")
        async for message in websocket:
            command = str(message).strip().upper()
            logger.info(f"📩 Command received: {command}")

            if command == "NEXT":
                pyautogui.press("right")
                logger.info("👉 Slide Advanced: NEXT (Right Arrow)")
                await websocket.send("ACK:NEXT")

            elif command == "PREV":
                pyautogui.press("left")
                logger.info("👈 Slide Returned: PREV (Left Arrow)")
                await websocket.send("ACK:PREV")

            elif command in ("PING", "HEARTBEAT"):
                await websocket.send("PONG")

    except websockets.exceptions.ConnectionClosed:
        logger.info(f"🔴 Watch disconnected: {client_ip}")
    except Exception as e:
        logger.error(f"⚠️ Error handling connection: {e}")

def get_lan_ips():
    """Retrieve all local IPv4 addresses for this machine."""
    ips = []
    try:
        hostname = socket.gethostname()
        for ip in socket.gethostbyname_ex(hostname)[2]:
            if not ip.startswith("127."):
                ips.append(ip)
    except Exception:
        pass
    return ips

async def main():
    port = 8765
    lan_ips = get_lan_ips()
    primary_ip = lan_ips[0] if lan_ips else "127.0.0.1"

    print("\n" + "=" * 54)
    print("  🎬 WEAR OS PRESENTATION CLICKER SERVER")
    print("=" * 54)
    print(f"  Server Status:  RUNNING (Port {port})")
    print(f"  Primary IP:     {primary_ip}")
    if len(lan_ips) > 1:
        print(f"  Other IPs:      {', '.join(lan_ips[1:])}")
    print("-" * 54)
    print("  👉 Ensure your Watch is connected to the same Wi-Fi")
    print(f"  👉 On your Watch, verify Laptop IP is: {primary_ip}")
    print("  👉 Open your PowerPoint / Slides and tap your watch!")
    print("=" * 54 + "\n")

    async with websockets.serve(handle_command, "0.0.0.0", port):
        await asyncio.Future()

if __name__ == "__main__":
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        print("\n🛑 Server stopped.")
        sys.exit(0)
