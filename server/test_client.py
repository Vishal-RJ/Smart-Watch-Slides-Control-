#!/usr/bin/env python3
"""
Test WebSocket Client
Simulates the Wear OS watch sending NEXT and PREV commands.

Usage:
    python test_client.py [ip]
"""

import asyncio
import sys
import websockets


async def run_client(server_ip: str = "127.0.0.1", port: int = 8765):
    uri = f"ws://{server_ip}:{port}"
    print(f"Connecting to {uri}...")

    try:
        async with websockets.connect(uri) as websocket:
            print(f"Connected to {uri}!")
            print("Commands: [n]ext, [p]rev, [q]uit")

            while True:
                user_input = await asyncio.to_thread(input, "Enter command (n/p/q): ")
                cmd = user_input.strip().lower()

                if cmd in ("n", "next"):
                    await websocket.send("NEXT")
                    print("Sent: NEXT")
                elif cmd in ("p", "prev"):
                    await websocket.send("PREV")
                    print("Sent: PREV")
                elif cmd in ("q", "quit", "exit"):
                    print("Exiting...")
                    break
                else:
                    print("Unknown choice. Enter 'n' for Next, 'p' for Prev, 'q' to Quit.")

    except Exception as e:
        print(f"Connection error: {e}")


if __name__ == "__main__":
    target_ip = sys.argv[1] if len(sys.argv) > 1 else "127.0.0.1"
    asyncio.run(run_client(target_ip))
