# OpenAPI code generator

OpenAPI code generator can be used to generate the models.

```shell script
pip install openapi-generator-cli

openapi-generator generate -i SWIFT-API-Swift-Messaging-1.1.0-resolved.yaml --global-property models -g python -o ./models
```