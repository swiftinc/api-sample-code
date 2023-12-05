import os
from dotenv import load_dotenv, find_dotenv
from flasgger import Swagger
from flask import Flask
from flask_restful import Api

from app.api.api_client import ApiClient
from app.resources.distributions import Distributions
from app.resources.fin import Fin

dotenv_path = find_dotenv(raise_error_if_not_found=True)
load_dotenv(dotenv_path)


def create_app():
    app = Flask(__name__)
    api = Api(app)
    enable_swagger(app)
    api_client = ApiClient(os.getenv('URL'))
    api.add_resource(Distributions, '/distributions/list', resource_class_args={api_client})
    api.add_resource(Fin, '/fin/<distribution_id>/download', endpoint='fin_download', methods=['GET'],
                     resource_class_args={api_client})
    api.add_resource(Fin, '/fin/send', endpoint='fin_send', methods=['POST'], resource_class_args={api_client})
    return app


def enable_swagger(app):
    swagger_template = dict(
        info={
            'title': 'Messaging API',
            'version': '1.0.0',
        }
    )
    swagger_config = {
        "headers": [],
        "specs": [
            {
                "endpoint": 'messaging-api',
                "route": '/messaging-api.json',
                "rule_filter": lambda rule: True,
                "model_filter": lambda tag: True,
            }
        ],
        "static_url_path": "/flasgger_static",
        "swagger_ui": True,
        "specs_route": "/test/"
    }

    Swagger(app, template=swagger_template, config=swagger_config)


if __name__ == '__main__':
    flaks_app = create_app()
    flaks_app.run(port=8080)
