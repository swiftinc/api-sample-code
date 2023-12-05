from app import create_app


def test_fin_download(test_client):
    """
    GIVEN a Flask application configured for testing
    WHEN the '/fin/messages/44984189500' page is requested (GET)
    THEN check that the response is valid
    """
    response = test_client.get('/fin/44984189500/download')
    assert response.status_code == 200


def test_fin_send(test_client):
    """
    GIVEN a Flask application configured for testing
    WHEN the '/fin/send' page is requested (POST)
    THEN check that the response is valid
    """
    response = test_client.post('/fin/send')
    assert response.status_code == 200
