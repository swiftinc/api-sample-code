import json

from flask_restful import Resource


class Fin(Resource):
    def __init__(self, api_client):
        self.api_client = api_client

    def get(self, distribution_id):
        """
        Download FIN message
        ---
        tags:
          - Fin
        parameters:
          - in: path
            name: distribution_id
            type: integer
            default: 44984189500
            required: true
            description: Distribution id
        responses:
          200:
            description: Returns a FIN message
            application/json:
              schema:
                type: object
        """
        return self.api_client.get_request('/fin/messages/{}'.format(distribution_id)), 200

    def post(self):
        """
        Send FIN 999 message
        ---
        tags:
          - Fin
        responses:
          200:
            description: Returns a FIN message
            application/json:
              schema:
                type: object
        """
        payload = json.dumps({"sender_reference": "1234", "message_type": "fin.999", "sender": "ABCD1234XXXX",
                              "receiver": "ABCD1234XXXX", "payload": "OjIwOjEyMzRcclxuOjc5OlRlc3Q=",
                              "network_info": {"network_priority": "Normal",
                                               "uetr": "099d4eb3-3d68-45fa-9afd-30b19af48728",
                                               "delivery_monitoring": "NonDelivery", "possible_duplicate": False}},
                             separators=(',', ':'))
        return self.api_client.post_request('/fin/messages', payload), 200
