namespace MessagingApi.Tests;

using System.Net;
using System.Net.Http.Json;
using System.Text;
using MessagingApi.Generated;
using Microsoft.AspNetCore.Mvc.Testing;

public class IntegrationTest
{
    [Fact]
    public async Task DistributionsListEndpoint()
    {
        await using var application = new WebApplicationFactory<Program>();
        using var client = application.CreateClient();
        var result = await client.GetAsync("/distributions/list");
        Assert.Equal(HttpStatusCode.OK, result.StatusCode);
    }

    [Fact]
    public async Task DownloadFinMessageEndpoint()
    {
        await using var application = new WebApplicationFactory<Program>();
        using var client = application.CreateClient();
        var result = await client.GetAsync("/fin/44984189500/download");
        Assert.Equal(HttpStatusCode.OK, result.StatusCode);
    }

    [Fact]
    public async Task SendFinMessageEndpoint()
    {
        await using var application = new WebApplicationFactory<Program>();
        using var client = application.CreateClient();
        FinMessageNetworkInfoEmission finMessageNetworkInfoEmission = new FinMessageNetworkInfoEmission
        {
            Network_priority = FinMessageNetworkInfoEmissionNetwork_priority.Normal,
            Uetr = Guid.NewGuid(),
            Delivery_monitoring = FinMessageNetworkInfoEmissionDelivery_monitoring.NonDelivery,
            Possible_duplicate = false
        };

        FinMessageEmission finMessage = new FinMessageEmission
        {
            Sender_reference = "1234",
            Sender = "ABCD1234XXXX",
            Receiver = "ABCD1234XXXX",
            Message_type = "fin.199",
            Payload = Encoding.UTF8.GetBytes(Convert.ToBase64String(Encoding.UTF8.GetBytes("\r\n:20:1234\r\n:79:Test"))),
            Network_info = finMessageNetworkInfoEmission
        };
        
        var result = await client.PostAsJsonAsync("/fin/send", finMessage);
        Assert.Equal(HttpStatusCode.OK, result.StatusCode);
    }
}