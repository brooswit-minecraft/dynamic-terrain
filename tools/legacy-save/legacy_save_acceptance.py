#!/usr/bin/env python3
"""Disposable CI only.

MINECRAFT-499 / Wroosbit gap 1: a direct `dynamicterrain:layered_dirt` registry lookup on a
freshly-placed block is not proof that a world *saved by the pre-extraction, monolithic Dynamic
Terrain* still resolves correctly once reopened with the split Layered Slopes + Dynamic Terrain
pair. This boots the actual pre-extraction build as a dedicated server (no client needed), places
representative layered blocks via RCON, saves the world to disk, then boots the actual split-pair
build against that same saved world and reads the blocks back via RCON -- a real save/reload
upgrade, not a same-process assumption.

Usage: legacy_save_acceptance.py <old-dt-dir> <new-dt-dir> [extra gradle args for the new build...]
"""
import json
import os
import re
import signal
import socket
import struct
import subprocess
import sys
import time
from pathlib import Path

RCON_PASSWORD = "acceptance"
LEVEL_NAME = "legacy-save-acceptance"
# (registry name, x, y, z, layers) -- representative materials/layer-counts, same LAYERS values
# LayeredBlockGameTests already covers in isolation; here the point is survival across the split.
BLOCKS = [
    ("layered_dirt", 0, 100, 0, 8),
    ("layered_grass", 2, 100, 0, 5),
    ("layered_stone", 4, 100, 0, 16),
    ("layered_sand", 6, 100, 0, 1),
]
MARKER_POS = (0, 200, 0)


def rcon(host, port, password, *commands):
    sock = socket.create_connection((host, port), timeout=10)
    try:
        def send(request_id, packet_type, payload):
            body = struct.pack('<ii', request_id, packet_type) + payload.encode() + b'\x00\x00'
            sock.sendall(struct.pack('<i', len(body)) + body)

        def recv():
            length = struct.unpack('<i', sock.recv(4))[0]
            data = b''
            while len(data) < length:
                chunk = sock.recv(length - len(data))
                if not chunk:
                    raise RuntimeError("RCON connection closed early")
                data += chunk
            request_id, packet_type = struct.unpack('<ii', data[:8])
            return request_id, packet_type, data[8:-2].decode(errors='replace')

        send(1, 3, password)
        request_id, _, _ = recv()
        if request_id == -1:
            raise RuntimeError("RCON auth failed")
        results = []
        for index, command in enumerate(commands, start=2):
            send(index, 2, command)
            _, _, response = recv()
            results.append(response)
        return results
    finally:
        sock.close()


def write_server_properties(game_dir, port, rcon_port):
    game_dir.mkdir(parents=True, exist_ok=True)
    (game_dir / 'eula.txt').write_text('eula=true\n')
    (game_dir / 'server.properties').write_text(
        "server-ip=127.0.0.1\n"
        f"server-port={port}\n"
        "online-mode=false\n"
        "enforce-secure-profile=false\n"
        f"level-name={LEVEL_NAME}\n"
        "level-type=minecraft:flat\n"
        "gamemode=creative\n"
        "spawn-protection=0\n"
        "view-distance=4\n"
        "simulation-distance=4\n"
        "max-players=1\n"
        "enable-rcon=true\n"
        f"rcon.port={rcon_port}\n"
        f"rcon.password={RCON_PASSWORD}\n"
        "broadcast-rcon-to-ops=false\n"
    )


def run_server(cwd, extra_gradle_args, log_path, ready_deadline=360):
    log = open(log_path, 'w')
    process = subprocess.Popen(
        ['./gradlew', 'runServer', '--no-daemon', '-Dorg.gradle.jvmargs=-Xmx1G', *extra_gradle_args],
        cwd=cwd, stdout=log, stderr=subprocess.STDOUT, start_new_session=True)
    deadline = time.monotonic() + ready_deadline
    while time.monotonic() < deadline:
        text = Path(log_path).read_text(errors='replace')
        if re.search(r'Done \(', text):
            return process, log
        if re.search(r'Failed to start the minecraft server|ModLoadingException', text):
            raise RuntimeError(f"server failed to start, see {log_path}:\n{text[-2000:]}")
        if process.poll() is not None:
            raise RuntimeError(f"server exited before readiness, see {log_path}")
        time.sleep(2)
    raise RuntimeError(f"server startup timeout, see {log_path}")


def stop_server(process, log, port):
    try:
        rcon('127.0.0.1', port, RCON_PASSWORD, "stop")
    except Exception:
        pass
    try:
        process.wait(timeout=60)
    except subprocess.TimeoutExpired:
        os.killpg(process.pid, signal.SIGTERM)
        try:
            process.wait(timeout=15)
        except subprocess.TimeoutExpired:
            os.killpg(process.pid, signal.SIGKILL)
            process.wait()
    log.close()


def main():
    old_dir = Path(sys.argv[1]).resolve()
    new_dir = Path(sys.argv[2]).resolve()
    new_gradle_args = sys.argv[3:]
    workdir = Path.cwd()

    old_game_dir = old_dir / 'run'
    write_server_properties(old_game_dir, 25565, 25575)
    process, log = run_server(old_dir, [], workdir / 'legacy-save-old-server.log')
    try:
        for name, x, y, z, layers in BLOCKS:
            response = rcon('127.0.0.1', 25575, RCON_PASSWORD,
                             f"setblock {x} {y} {z} dynamicterrain:{name}[layers={layers}]")[0]
            if not response.strip():
                raise RuntimeError(f"pre-split placement of dynamicterrain:{name}[layers={layers}] "
                                    f"at ({x},{y},{z}) was not acknowledged by the old server")
        rcon('127.0.0.1', 25575, RCON_PASSWORD, "save-all flush")
        time.sleep(2)
    finally:
        stop_server(process, log, 25575)

    # A dedicated server's world directory is <gameDir>/<level-name> directly -- unlike a
    # singleplayer client, there is no intermediate "saves/" folder.
    old_world = old_game_dir / LEVEL_NAME
    if not old_world.exists():
        raise RuntimeError(f"pre-split world was never saved at {old_world}")

    new_game_dir = new_dir / 'run'
    write_server_properties(new_game_dir, 25566, 25576)
    new_game_dir.mkdir(parents=True, exist_ok=True)
    target_world = new_game_dir / LEVEL_NAME
    if target_world.exists():
        raise RuntimeError(f"refusing to reuse an existing world at {target_world}")
    subprocess.run(['cp', '-r', str(old_world), str(target_world)], check=True)

    process, log = run_server(new_dir, new_gradle_args, workdir / 'legacy-save-new-server.log')
    try:
        results = []
        for name, x, y, z, layers in BLOCKS:
            mx, my, mz = MARKER_POS
            rcon('127.0.0.1', 25576, RCON_PASSWORD, f"setblock {mx} {my} {mz} minecraft:air")
            response = rcon('127.0.0.1', 25576, RCON_PASSWORD,
                             f"execute if block {x} {y} {z} dynamicterrain:{name}[layers={layers}] "
                             f"run setblock {mx} {my} {mz} minecraft:emerald_block")[0]
            if not response.strip():
                raise RuntimeError(
                    f"legacy block/layers lost across the split at ({x},{y},{z}): expected "
                    f"dynamicterrain:{name}[layers={layers}] to still resolve after reopening the "
                    f"pre-split save with the split pair, but the state no longer matches")
            results.append({"pos": [x, y, z], "block": f"dynamicterrain:{name}", "layers": layers})
        Path('legacy-save-acceptance-result.json').write_text(
            json.dumps({"passed": True, "blocks": results}, indent=2))
        print(json.dumps({"passed": True, "blocks": len(results)}))
    finally:
        stop_server(process, log, 25576)


if __name__ == '__main__':
    main()
