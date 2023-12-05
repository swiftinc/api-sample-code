using System.Net.Http.Headers;

namespace SwiftRef;

public class AuthorizationClientHandler : DelegatingHandler
{
    private readonly OAuthTokenService _oAuthTokenService;

    public AuthorizationClientHandler(OAuthTokenService oAuthTokenService)
    {
        _oAuthTokenService = oAuthTokenService;
    }

    protected override async Task<HttpResponseMessage> SendAsync(HttpRequestMessage request, CancellationToken cancellationToken)
    {
        var accessToken = await _oAuthTokenService.GetAccessToken();
        request.Headers.Authorization = new AuthenticationHeaderValue("Bearer", accessToken);
        return await base.SendAsync(request, cancellationToken);
    }
}