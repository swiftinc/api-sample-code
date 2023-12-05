import os
import random
import time

from cryptography.hazmat.primitives.serialization import load_pem_private_key
from cryptography.x509 import load_pem_x509_certificate


def key_path(key_name):
    return os.path.join(os.path.dirname(os.path.realpath(__file__)), "..", "..", "certs", key_name)


def get_certificate():
    with open(key_path(os.getenv('CERTIFICATE')), "rb") as pem_data:
        cert_pem = pem_data.read()
        cert = load_pem_x509_certificate(cert_pem)
        cert_lines = str(cert_pem, 'utf-8').replace('-----BEGIN CERTIFICATE-----', '').replace(
            '-----END CERTIFICATE-----', '').splitlines()
        cert_subject = cert.subject.rfc4514_string().lower()
        x5c = ''.join(cert_lines)
        return cert_subject, x5c


def get_key():
    with open(key_path(os.getenv('PRIVATE_KEY')), "rb") as key_data:
        return load_pem_private_key(key_data.read(), password=bytes(os.getenv('KEY_PASSWORD'), 'utf-8'))


def get_proxies():
    proxy = os.getenv('PROXY')
    if proxy:
        return {'https': proxy}
    else:
        return None


def random_chars(number_of_chars):
    charset = 'abcdefghijklmnopqrstuvwxyz0123456789'
    return ''.join(random.choice(charset) for _ in range(number_of_chars))


def get_timestamp():
    return int(round(time.time()))
