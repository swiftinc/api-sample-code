using System.Net.Http.Headers;
using System.Security.Claims;
using System.Security.Cryptography.X509Certificates;
using System.Text;
using System.Text.Json.Serialization;
using Microsoft.Extensions.Options;
using Microsoft.IdentityModel.JsonWebTokens;
using Microsoft.IdentityModel.Tokens;
using Microsoft.Net.Http.Headers;
using JwtHeaderParameterNames = Microsoft.IdentityModel.JsonWebTokens.JwtHeaderParameterNames;
using JwtRegisteredClaimNames = Microsoft.IdentityModel.JsonWebTokens.JwtRegisteredClaimNames;

namespace PreVal;

public class OAuthTokenService
{
    private readonly ILogger<OAuthTokenService> _logger;

    private readonly HttpClient _oauthHttpClient;

    private readonly Settings _settings;

    private JwtTokenHolder? _tokenHolder;

    private string _consumerKey;

    public OAuthTokenService(HttpClient httpClient, ILogger<OAuthTokenService> logger, IOptions<Settings> settings)
    {
        _logger = logger;
        _oauthHttpClient = httpClient;
        _settings = settings.Value;
        _oauthHttpClient.BaseAddress = new Uri(_settings.BaseAddress);
        _oauthHttpClient.DefaultRequestHeaders.Add(HeaderNames.Accept, "application/json");
        _consumerKey = Environment.GetEnvironmentVariable("CONSUMER_KEY") ?? throw new Exception("CONSUMER_KEY environment variable not found");
        var consumerSecret = Environment.GetEnvironmentVariable("CONSUMER_SECRET") ?? throw new Exception("CONSUMER_SECRET environment variable not found");
        string credential = Convert.ToBase64String(Encoding.UTF8.GetBytes(string.Format("{0}:{1}", _consumerKey, consumerSecret)));
        _oauthHttpClient.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Basic", credential);
    }

    public async Task<string> GetAccessToken()
    {
        if (isAccessTokenExpired())
        {
            _logger.LogInformation("Retrieving new OAuth token");

            var assertion = CreateJwt();
            var parameters = new Dictionary<string, string>
            {
                ["grant_type"] = _settings.GrantType,
                ["scope"] = _settings.Scope,
                ["assertion"] = assertion
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

    private string CreateJwt()
    {
        var now = DateTime.UtcNow;
        X509Certificate2 signingCert = new X509Certificate2(_settings.KeyStore, _settings.KeyStorePassword);
        var exportedCertificate = Convert.ToBase64String(signingCert.Export(X509ContentType.Cert), Base64FormattingOptions.None);

        var key = new RsaSecurityKey(signingCert.GetRSAPrivateKey());
        var descriptor = new SecurityTokenDescriptor
        {
            TokenType = "JWT",
            Issuer = _consumerKey,
            Audience = new Uri(_oauthHttpClient.BaseAddress!, _settings.TokenUri).ToString().Replace("https://", ""),
            IssuedAt = now,
            NotBefore = now,
            Expires = now.AddSeconds(15),
            Claims = new Dictionary<string, object>()
            {
                [JwtRegisteredClaimNames.Jti] = Guid.NewGuid().ToString()
            },
            Subject = new ClaimsIdentity(new[] { new Claim(JwtRegisteredClaimNames.Sub, signingCert.Subject) }),
            SigningCredentials = new SigningCredentials(key, SecurityAlgorithms.RsaSha256),
            AdditionalHeaderClaims = new Dictionary<string, object>()
            {
                [JwtHeaderParameterNames.X5c] = new List<string> { exportedCertificate }
            }
        };

        var handler = new JsonWebTokenHandler();
        return handler.CreateToken(descriptor);
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