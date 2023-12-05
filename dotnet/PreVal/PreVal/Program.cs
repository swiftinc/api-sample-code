using System.Net;
using System.Text.Json.Serialization;
using Microsoft.AspNetCore.Http.Json;
using Microsoft.Extensions.Options;
using Microsoft.OpenApi.Any;
using Microsoft.OpenApi.Models;
using Swashbuckle.AspNetCore.SwaggerUI;
using PreVal;
using PreVal.Generated;

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
builder.Services.AddTransient<AuthorizationClientHandler>();

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
builder.Services.AddHttpClient<PreValApiClientExt>((serviceProvider, client) =>
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
}).AddHttpMessageHandler<AuthorizationClientHandler>();

// Create the endpoints
var app = builder.Build();

app.MapPost("/verifyBeneficiaryAccount", async (PreValApiClientExt prevalApiClient, AccountVerificationRequest request) =>
{
    return await prevalApiClient.VerifyBeneficiaryAccountAsync("swhqbebb", request);
}).WithOpenApi(operation =>
{
    operation.Summary = "Verify beneficiary account";
    operation.RequestBody.Content["application/json"].Example = new OpenApiString("{ \"correlation_identifier\": \"112211221122\", \"context\": \"BENR\", \"uetr\": \"97ed4827-7b6f-4491-a06f-b548d5a7512d\", \"creditor_account\": \"7892368367\", \"creditor_name\": \"DEF Electronics\", \"creditor_agent\": { \"bicfi\": \"AAAAUS2L\" }}");
    return operation;
});

// Swagger configuration
app.UseSwagger();
app.UseSwaggerUI(settings =>
{
    settings.DefaultModelRendering(ModelRendering.Model);
    settings.DefaultModelsExpandDepth(-1);
    settings.SwaggerEndpoint("/swagger/v1/swagger.json", "Pre-Validation API");
    settings.RoutePrefix = "test";
});

app.Run();

public partial class Program { }