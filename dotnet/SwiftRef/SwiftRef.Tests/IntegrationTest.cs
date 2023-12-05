namespace SwiftRef.Tests;

using System.Net;
using Microsoft.AspNetCore.Mvc.Testing;

public class IntegrationTest
{
    [Fact]
    public async Task GetBicDetailsEndpoint()
    {
        await using var application = new WebApplicationFactory<Program>();
        using var client = application.CreateClient();
        var result = await client.GetAsync("/bic/swhqbebb");
        Assert.Equal(HttpStatusCode.OK, result.StatusCode);
    }

    [Fact]
    public async Task GetIbanDetailsEndpoint()
    {
        await using var application = new WebApplicationFactory<Program>();
        using var client = application.CreateClient();
        var result = await client.GetAsync("/iban/IT60X0542811101000000123456");
        Assert.Equal(HttpStatusCode.OK, result.StatusCode);
    }
}