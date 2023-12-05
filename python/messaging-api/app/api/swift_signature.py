import base64
import hashlib
import os

import jwt

from app.api.utils import get_timestamp, random_chars, get_key, get_certificate, get_proxies


class SwiftSignature:
    def __init__(self):
        self.consumer_key = os.getenv('CONSUMER_KEY')
        self.key_password = bytes(os.getenv('KEY_PASSWORD'), 'utf-8')
        self.cert_subject, self.x5c = get_certificate()
        self.key = get_key()
        self.proxies = get_proxies()

    def generate(self, endpoint, body):
        headers = {
            'alg': 'RS256',
            'x5c': [self.x5c],
            'typ': 'JWT'
        }

        body_b64 = base64.b64encode(body.encode())
        digest = hashlib.sha256(body_b64).digest()
        digest_b64 = base64.b64encode(digest).decode()

        now = get_timestamp()
        payload = {
            'iat': now,
            'nbf': now,
            'exp': now + 15,
            'jti': random_chars(32),
            'sub': self.cert_subject,
            'aud': endpoint[8:],  # removes 'https://' prefix
            'digest': digest_b64
        }

        return jwt.encode(payload=payload, headers=headers, key=self.key, algorithm="RS256")
