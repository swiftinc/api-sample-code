namespace MessagingApi;

public class SignatureClientHandler : DelegatingHandler
{
    private readonly string SignatureHeader = "X-SWIFT-Signature";

    private readonly SignatureService _signatureService;

    public SignatureClientHandler(SignatureService signatureService)
    {
        _signatureService = signatureService;
    }

    protected override async Task<HttpResponseMessage> SendAsync(HttpRequestMessage request, CancellationToken cancellationToken)
    {
        if (request.Headers.Contains(SignatureHeader) && request.Content != null)
        {
            request.Headers.Remove(SignatureHeader); // Remove empty signature
            var jws = _signatureService.CreateSign(request.RequestUri!.AbsoluteUri, await request.Content.ReadAsByteArrayAsync());
            request.Headers.Add(SignatureHeader, jws);
        }

        return await base.SendAsync(request, cancellationToken);
    }
}