from flask_restful import Resource


class Distributions(Resource):
    def __init__(self, api_client):
        self.api_client = api_client

    def get(self):
        """
        Get distribution list
        ---
        tags:
          - Distributions
        responses:
          200:
            description: Returns distribution list
        """
        return self.api_client.get_request('/distributions'), 200
