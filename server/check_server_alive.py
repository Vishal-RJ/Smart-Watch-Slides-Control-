import asyncio
import websockets
import sys

async def check():
    try:
        async with asyncio.wait_for(websockets.connect("ws://localhost:8765"), timeout=2) as ws:
            print("SERVER_ALIVE")
    except Exception as e:
        print(f"SERVER_DEAD: {e}")

if __name__ == "__main__":
    asyncio.run(check())
