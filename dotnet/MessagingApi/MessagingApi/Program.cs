using System.Net;
using System.Text.Json.Serialization;
using MessagingApi;
using MessagingApi.Generated;
using Microsoft.AspNetCore.Http.Json;
using Microsoft.Extensions.Options;
using Microsoft.OpenApi.Any;
using Microsoft.OpenApi.Models;
using Swashbuckle.AspNetCore.SwaggerUI;

var builder = WebApplication.CreateBuilder(args);

builder.Services.Configure<Settings>(builder.Configuration.GetSection("Settings"));

// Swagger
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen(options =>
{
    options.AddServer(new OpenApiServer()
    {
        Url = "/"
    });
});

// Use String for enums
builder.Services.Configure<JsonOptions>(o =>
{
    o.SerializerOptions.Converters.Add(new JsonStringEnumConverter());
    o.SerializerOptions.DefaultIgnoreCondition = JsonIgnoreCondition.WhenWritingNull;
});

// OAuth and Swift signature classes and handlers
builder.Services.AddSingleton<OAuthTokenService>();
builder.Services.AddSingleton<SignatureService>();
builder.Services.AddTransient<AuthorizationClientHandler>();
builder.Services.AddTransient<SignatureClientHandler>();

// OAuth HTTPClient configuration
builder.Services.AddHttpClient<OAuthTokenService>().ConfigurePrimaryHttpMessageHandler((c) =>
{
    var handler = new HttpClientHandler();
    var config = c.GetRequiredService<IOptions<Settings>>().Value;
    if (!string.IsNullOrEmpty(config.ProxyHost))
    {
        handler.Proxy = new WebProxy(config.ProxyHost, config.ProxyPort);
    }
    return handler;
});

// API HTTP client configuration
builder.Services.AddHttpClient<MessagingApiClient>((serviceProvider, client) =>
{
    var config = serviceProvider.GetRequiredService<IOptions<Settings>>().Value;
    client.BaseAddress = new Uri(config.BaseAddress + config.Endpoint);
}).ConfigurePrimaryHttpMessageHandler((c) =>
{
    var handler = new HttpClientHandler();
    var config = c.GetRequiredService<IOptions<Settings>>().Value;
    if (!string.IsNullOrEmpty(config.ProxyHost))
    {
        handler.Proxy = new WebProxy(config.ProxyHost, config.ProxyPort);
    }
    return handler;
}).AddHttpMessageHandler<AuthorizationClientHandler>()
.AddHttpMessageHandler<SignatureClientHandler>();

// Create the endpoints
var app = builder.Build();

app.MapGet("/distributions/list", async (MessagingApiClient messagingApi) =>
{
    ListDistributionsResponse listDistributionsResponse = await messagingApi.ListDistributionsAsync(100, 0);
    return listDistributionsResponse;
}).WithOpenApi(operation =>
{
    operation.Summary = "Download distribution list";
    return operation;
});

app.MapGet("/fin/{distributionId}/download", async (MessagingApiClient messagingApi, long distributionId) =>
{
    FinMessageDownloadResponse response = await messagingApi.DownloadFinMessageAsync(distributionId);
    return response;
}).WithOpenApi(operation =>
{
    operation.Summary = "Download distribution";
    var parameter = operation.Parameters[0];
    parameter.Content.Add("distributionId", new OpenApiMediaType()
    {
        Example = new OpenApiString("44984189500")
    });
    return operation;
});

app.MapPost("/fin/send", async (MessagingApiClient messagingApi, FinMessageEmission finMessageEmission) =>
{
    SendMessageResponse response = await messagingApi.SendFinMessageAsync("", finMessageEmission);
    return response;
}).WithOpenApi(operation =>
{
    operation.Summary = "Send FIN message";
    operation.RequestBody.Content["application/json"].Example = new OpenApiString("{\"sender_reference\": \"1234\", \"message_type\": \"fin.999\", \"sender\": \"ABCD1234XXXX\", \"receiver\": \"ABCD1234XXXX\", \"payload\": \"OjIwOjEyMzRcclxuOjc5OlRlc3Q=\", \"network_info\": { \"network_priority\": \"Normal\", \"uetr\": \"099d4eb3-3d68-45fa-9afd-30b19af48728\", \"delivery_monitoring\": \"NonDelivery\", \"possible_duplicate\": false } }");
    return operation;
});

// Swagger configuration
app.UseSwagger();
app.UseSwaggerUI(settings =>
{
    settings.DefaultModelRendering(ModelRendering.Model);
    settings.DefaultModelsExpandDepth(-1);
    settings.SwaggerEndpoint("/swagger/v1/swagger.json", "Messaging API");
    settings.RoutePrefix = "test";
});

app.Run();

public partial class Program { }