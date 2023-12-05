import os

import jwt
import requests
from requests.auth import HTTPBasicAuth

from app.api.utils import key_path, random_chars, get_timestamp, get_certificate, get_key, get_proxies


class OAuthToken:
    bearer_token = None
    bearer_token_expires_in = 0

    def __init__(self, base_path):
        self.endpoint = base_path + os.getenv('TOKEN_URI')
        self.consumer_key = os.getenv('CONSUMER_KEY')
        self.consumer_secret = os.getenv('CONSUMER_SECRET')
        self.scope = os.getenv('SCOPE')
        self.key_password = bytes(os.getenv('KEY_PASSWORD'), 'utf-8')
        self.cert_subject, self.x5c = get_certificate()
        self.key = get_key()
        self.proxies = get_proxies()

    def __call__(self, request):
        if self.is_expired():
            headers = {
                'alg': 'RS256',
                'x5c': [self.x5c],
                'typ': 'JWT'
            }

            now = get_timestamp()
            payload = {
                'iat': now,
                'nbf': now,
                'exp': now + 5 * 60,
                'jti': random_chars(32),
                'iss': self.consumer_key,
                'sub': self.cert_subject,
                'aud': self.endpoint[8:],  # removes 'https://' prefix
            }

            assertion = jwt.encode(payload=payload, headers=headers, key=self.key, algorithm="RS256")

            headers = {
                'Content-Type': 'application/x-www-form-urlencoded',
                'Accept': 'application/json',
            }

            # JWT Bearer Grant Type using Swift issued PKI Certificate
            form_data = {
                'grant_type': 'urn:ietf:params:oauth:grant-type:jwt-bearer',
                'assertion': assertion,
                'scope': self.scope
            }

            if os.getenv('SWIFT_CA') is not None:
                ca = key_path(os.getenv('SWIFT_CA'))
            else:
                ca = True
            response = requests.request("POST", self.endpoint, data=form_data, headers=headers,
                                    auth=HTTPBasicAuth(self.consumer_key, self.consumer_secret),
                                            proxies=self.proxies, verify=ca)
            if response.ok:
                self.bearer_token = response.json()["access_token"]
                # Clock skew of 10 secs
                self.bearer_token_expires_in = get_timestamp() + int(response.json()["expires_in"]) * 1000 - 10000
            else:
                raise RuntimeError(str(response.json()))

        request.headers['Authorization'] = 'Bearer ' + self.bearer_token
        return request

    def is_expired(self):
        return get_timestamp() > self.bearer_token_expires_in
