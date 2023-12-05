using System.Security.Claims;
using System.Security.Cryptography;
using System.Security.Cryptography.X509Certificates;
using System.Text;
using Microsoft.Extensions.Options;
using Microsoft.IdentityModel.JsonWebTokens;
using Microsoft.IdentityModel.Tokens;
using JwtHeaderParameterNames = Microsoft.IdentityModel.JsonWebTokens.JwtHeaderParameterNames;
using JwtRegisteredClaimNames = Microsoft.IdentityModel.JsonWebTokens.JwtRegisteredClaimNames;

namespace MessagingApi;

public class SignatureService
{

    private readonly Settings _settings;

    public SignatureService(IOptions<Settings> settings)
    {
        _settings = settings.Value;
    }

    public string CreateSign(string url, byte[] body)
    {
        var base64 = Convert.ToBase64String(body);
        string digestBase64;
        using (SHA256 sha256Hash = SHA256.Create())
        {
            byte[] digest = sha256Hash.ComputeHash(Encoding.UTF8.GetBytes(base64));
            digestBase64 = Convert.ToBase64String(digest);
        }

        var now = DateTime.UtcNow;
        X509Certificate2 signingCert = new X509Certificate2(_settings.KeyStore, _settings.KeyStorePassword);
        var exportedCertificate = Convert.ToBase64String(signingCert.Export(X509ContentType.Cert), Base64FormattingOptions.None);

        var key = new RsaSecurityKey(signingCert.GetRSAPrivateKey());
        var descriptor = new SecurityTokenDescriptor
        {
            TokenType = "JWT",
            Audience = url.Replace("https://", ""),
            IssuedAt = now,
            NotBefore = now,
            Expires = now.AddSeconds(15),
            Claims = new Dictionary<string, object>()
            {
                [JwtRegisteredClaimNames.Jti.ToString()] = Guid.NewGuid().ToString(),
                ["digest"] = digestBase64
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
}