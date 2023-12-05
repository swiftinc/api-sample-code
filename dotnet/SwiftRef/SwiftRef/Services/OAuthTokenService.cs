using System.Net.Http.Headers;
using System.Text;
using System.Text.Json.Serialization;
using Microsoft.Extensions.Options;
using Microsoft.Net.Http.Headers;

namespace SwiftRef;

public class OAuthTokenService
{
    private readonly ILogger<OAuthTokenService> _logger;

    private readonly HttpClient _oauthHttpClient;

    private readonly Settings _settings;

    private JwtTokenHolder? _tokenHolder;

    public OAuthTokenService(HttpClient httpClient, ILogger<OAuthTokenService> logger, IOptions<Settings> settings)
    {
        _logger = logger;
        _oauthHttpClient = httpClient;
        _settings = settings.Value;
        _oauthHttpClient.BaseAddress = new Uri(_settings.BaseAddress);
        _oauthHttpClient.DefaultRequestHeaders.Add(HeaderNames.Accept, "application/json");
        var consumerKey = Environment.GetEnvironmentVariable("CONSUMER_KEY") ?? throw new Exception("CONSUMER_KEY environment variable not found");
        var consumerSecret = Environment.GetEnvironmentVariable("CONSUMER_SECRET") ?? throw new Exception("CONSUMER_SECRET environment variable not found");
        string credential = Convert.ToBase64String(Encoding.UTF8.GetBytes(string.Format("{0}:{1}", consumerKey, consumerSecret)));
        _oauthHttpClient.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Basic", credential);
    }

    public async Task<string> GetAccessToken()
    {
        if (isAccessTokenExpired())
        {
            _logger.LogInformation("Retrieving new OAuth token");

            var parameters = new Dictionary<string, string>
            {
                ["grant_type"] = _settings.GrantType,
                ["username"] = _settings.UserName,
                ["password"] = _settings.Password
            };

            var httpRequestMessage = new HttpRequestMessage(HttpMethod.Post, _settings.TokenUri)
            {
                Content = new FormUrlEncodedContent(parameters)
            };

            var httpResponseMessage = _oauthHttpClient.Send(httpRequestMessage);

            if (!httpResponseMessage.IsSuccessStatusCode)
            {
                var contentStream = await httpResponseMessage.Content.ReadAsStringAsync();
                _logger.LogError(contentStream);
                throw new Exception("Error retrieving OAuth token");
            }

            _tokenHolder = await httpResponseMessage.Content.ReadFromJsonAsync<JwtTokenHolder>() ?? throw new Exception("Error parsing OAuth token");
            _logger.LogDebug(_tokenHolder.ToString());
            _tokenHolder.ExpiresIn = DateTimeOffset.UtcNow.ToUnixTimeSeconds() + _tokenHolder.ExpiresIn - _settings.TokenTimeSkewInSeconds;
        }

        return _tokenHolder?.AccessToken ?? throw new Exception("Error retrieving access token");
    }

    private bool isAccessTokenExpired()
    {
        if (_tokenHolder == null)
        {
            return true;
        }

        return DateTimeOffset.UtcNow.ToUnixTimeSeconds() > _tokenHolder.ExpiresIn;
    }

    private class JwtTokenHolder
    {
        [JsonPropertyName("access_token")]
        public required string AccessToken { get; init; }

        [JsonPropertyName("expires_in")]
        public required long ExpiresIn { get; set; }

        public override string ToString()
        {
            return "AccessToken: " + AccessToken + ", ExpiresIn:" + ExpiresIn;
        }
    }
}