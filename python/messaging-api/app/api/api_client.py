import logging
import os
from http.client import HTTPConnection

import requests

from app.api.oauth_token import OAuthToken
from app.api.swift_signature import SwiftSignature
from app.api.utils import key_path, get_proxies


class ApiClient:
    def __init__(self, base_path):
        self.base_path = base_path
        self.endpoint = os.getenv('ENDPOINT')
        self.swift_signature = SwiftSignature()
        self.oauth_service = OAuthToken(base_path)
        self.enable_http_logs()
        self.proxies = get_proxies()

    def get_request(self, path):
        return self._request('GET', path)

    def post_request(self, path, body):
        return self._request('POST', path, body)

    def _request(self, method, path, body=None):
        url = self.base_path + self.endpoint + path

        headers = {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
        }

        request = requests.Request(method, url, data=body, headers=headers, auth=self.oauth_service)
        prepped = request.prepare()
        if method == 'POST':
            signature = self.swift_signature.generate(url, prepped.body)
            prepped.headers['X-SWIFT-Signature'] = signature
        with requests.Session() as session:
            if os.getenv('SWIFT_CA') is not None:
                ca = key_path(os.getenv('SWIFT_CA'))
            else:
                ca = True
            response = session.send(prepped, proxies=self.proxies, verify=ca)
            if response.ok:
                return response.json()
            else:
                raise RuntimeError(str(response.json()))

    @staticmethod
    def enable_http_logs():
        if os.getenv('ENABLE_HTTP_LOGS', 'false') == 'true':
            HTTPConnection.debuglevel = 1
            logging.basicConfig()
            logging.getLogger().setLevel(logging.DEBUG)
            requests_log = logging.getLogger("requests.packages.urllib3")
            requests_log.setLevel(logging.DEBUG)
            requests_log.propagate = True

