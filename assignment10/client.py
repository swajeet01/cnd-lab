"""
@File: client.py
@Description: Client script for CND Lab FHE assignment.
@Author: Swajeet Swarnkar
M.Tech. IT NIT Raipur 2023-25
"""

import socket
import sys
import threading
import json

import numpy as np
from Pyfhel import Pyfhel, PyCtxt, PyPtxt

IP, PORT = "127.0.0.1", 12345

if len(sys.argv) != 3:
    print('Usage: python3 client.py <IP> <PORT>')
    exit(1)
else:
    IP = str(sys.argv[1])
    PORT = int(sys.argv[2])

client_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client_sock.connect((IP, PORT))

he_client = Pyfhel()
he_client.contextGen(scheme='ckks', n=2**13, scale=2**30, qi_sizes=[30]*5)
he_client.keyGen()
he_client.relinKeyGen()
he_client.rotateKeyGen()
# he_client.save_secret_key('sec.key')

print('Ready.')

try:
    nums_str = input()
    nums = list(map(lambda item: int(item), nums_str.split()))
    nums_length = len(nums)
    print('Input:', nums)
    np_nums = np.array(nums, dtype=np.float64)
    ptxt = he_client.encodeFrac(np_nums)
    ctxt = he_client.encryptPtxt(ptxt)
    s_context    = he_client.to_bytes_context()
    s_public_key = he_client.to_bytes_public_key()
    s_relin_key  = he_client.to_bytes_relin_key()
    s_rotate_key = he_client.to_bytes_rotate_key()
    s_ctxt       = ctxt.to_bytes()
    s_len        = nums_length
    req_body = json.dumps({
        's_context': s_context.decode('cp437'),
        's_public_key': s_public_key.decode('cp437'),
        's_relin_key': s_relin_key.decode('cp437'),
        's_rotate_key': s_rotate_key.decode('cp437'),
        's_ctxt': s_ctxt.decode('cp437'),
        's_len': nums_length
    })

    print('Sending.')
    client_sock.send(req_body.encode())
    client_sock.shutdown(socket.SHUT_WR)
    print('Sent.')

    frags = []
    print('Receiving.')
    while True:
        chunk = client_sock.recv(4096)
        if not chunk:
            break
        frags.append(chunk)
    print('Received.')
    res_bytes = b''.join(frags)
    res_ctxt = PyCtxt(pyfhel=he_client, bytestring=res_bytes)
    res_dec = he_client.decryptFrac(res_ctxt)
    print(f'Average computed: {res_dec[nums_length - 1]:.2f}')
except Exception as e:
    print(e)

print('Quit.')
client_sock.close()
