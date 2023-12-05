using System.Net;
using System.Text.Json.Serialization;
using Microsoft.AspNetCore.Http.Json;
using Microsoft.Extensions.Options;
using Microsoft.OpenApi.Any;
using Microsoft.OpenApi.Models;
using Swashbuckle.AspNetCore.SwaggerUI;
using SwiftRef;
using SwiftRef.Generated;

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
builder.Services.AddHttpClient<SwiftRefApiClient>((serviceProvider, client) =>
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

app.MapGet("/bic/{bic}", async (SwiftRefApiClient swiftRefApiClient, string bic) =>
{
    return await swiftRefApiClient.GetBicDetailsAsync(bic);
}).WithOpenApi(operation =>
{
    operation.Summary = "Get BIC details";
    var parameter = operation.Parameters[0];
    parameter.Content.Add("bic", new OpenApiMediaType()
    {
        Example = new OpenApiString("swhqbebb")
    });
    return operation;
});

app.MapGet("/iban/{iban}", async (SwiftRefApiClient swiftRefApiClient, string iban) =>
{
    return await swiftRefApiClient.GetIbanDetailsAsync(iban);
}).WithOpenApi(operation =>
{
    operation.Summary = "Get IBAN details";
    var parameter = operation.Parameters[0];
    parameter.Content.Add("iban", new OpenApiMediaType()
    {
        Example = new OpenApiString("IT60X0542811101000000123456")
    });
    return operation;
});

// Swagger configuration
app.UseSwagger();
app.UseSwaggerUI(settings =>
{
    settings.DefaultModelRendering(ModelRendering.Model);
    settings.DefaultModelsExpandDepth(-1);
    settings.SwaggerEndpoint("/swagger/v1/swagger.json", "SwiftRef API");
    settings.RoutePrefix = "test";
});

app.Run();

public partial class Program { }