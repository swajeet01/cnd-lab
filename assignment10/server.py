"""
@File: server.py
@Description: Server script for CND Lab FHE assignment.
@Author: Swajeet Swarnkar
M.Tech. IT NIT Raipur 2023-25
"""

import socket
import sys
import threading
import json

import numpy as np
from Pyfhel import Pyfhel, PyCtxt, PyPtxt

PORT = 12345

if len(sys.argv) != 2:
    print('Usage: python3 server.py <PORT>')
    exit(1)
else:
    PORT = int(sys.argv[1])

server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)

try:
    server_socket.bind(('', PORT))
except socket.gaierror as err:
    print('Can\'t bind.')
    exit(2)

server_socket.listen()

print(f'Listening for connections on {PORT}.')

def client_callback(client_sock, info):
    frags = []
    print('Receiving.')
    while True:
        chunk = client_sock.recv(4096)
        if not chunk:
            break
        frags.append(chunk)
    print('Received.')
    if len(frags) == 0:
        return
    serl_json = b''.join(frags)
    req_body = json.loads(serl_json.decode())
    he_server = Pyfhel()
    he_server.from_bytes_context(req_body['s_context'].encode('cp437'))
    he_server.from_bytes_public_key(req_body['s_public_key'].encode('cp437'))
    he_server.from_bytes_relin_key(req_body['s_relin_key'].encode('cp437'))
    he_server.from_bytes_rotate_key(req_body['s_rotate_key'].encode('cp437'))
    ctxt = PyCtxt(pyfhel=he_server, bytestring=req_body['s_ctxt'].encode('cp437'))
    print(f'Encrypted data received: {ctxt}')
    data_len = req_body['s_len']
    # w = np.ones(data_len, dtype=np.float64)
    ctxt /= data_len
    for shift in range(1, data_len):
        ctxt += (ctxt >> shift)
    print(f'Encrypted data send {ctxt}')
    client_sock.send(ctxt.to_bytes())
    client_sock.shutdown(socket.SHUT_WR)

while True:
    try:
        (client_socket, client_info) = server_socket.accept()
        client_thread = threading.Thread(target=client_callback, args=(client_socket, client_info))
        client_thread.start()
    except KeyboardInterrupt:
        break

print('Quit.')
server_socket.close()
