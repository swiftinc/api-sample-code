def test_distributions(test_client):
    """
    GIVEN a Flask application configured for testing
    WHEN the '/distributions/list' page is requested (GET)
    THEN check that the response is valid
    """
    response = test_client.get('/distributions/list')
    assert response.status_code == 200
