import asyncio
import websockets

async def handler(websocket):
    print("Client connected")
    async for message in websocket:
        print(f"Received: {message}")
        await websocket.send(f"ACK:{message}")

async def main():
    print("Starting minimal server on 8765")
    async with websockets.serve(handler, "0.0.0.0", 8765):
        await asyncio.Future()

if __name__ == "__main__":
    asyncio.run(main())
