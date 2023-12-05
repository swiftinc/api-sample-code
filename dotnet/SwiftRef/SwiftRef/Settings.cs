namespace SwiftRef;

public class Settings
{
    public required string BaseAddress { get; init; }
    public required string Endpoint { get; init; }
    public required string TokenUri { get; init; }
    public required string GrantType { get; init; }
    public required long TokenTimeSkewInSeconds { get; init; }
    public required string ProxyHost { get; init; }
    public required int ProxyPort { get; init; }
    public required string UserName { get; init; }
    public required string Password { get; init; }
}