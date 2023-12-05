namespace PreVal.Tests;

using System.Net;
using System.Net.Http.Json;
using Microsoft.AspNetCore.Mvc.Testing;
using PreVal.Generated;

public class IntegrationTest
{
    [Fact]
    public async Task VerifyBeneficiaryAccountEndpoint()
    {
        await using var application = new WebApplicationFactory<Program>();
        using var client = application.CreateClient();

        AccountVerificationRequest request = new AccountVerificationRequest()
        {
            Correlation_identifier = "112211221122",
            Context = AccountValidationCheckContext1Code.BENR,
            Uetr = "97ed4827-7b6f-4491-a06f-b548d5a7512d",
            Creditor_account = "7892368367",
            Creditor_name = "DEF Electronics",
            Creditor_agent = new FinancialInstitutionIdentification20()
            {
                Bicfi = "AAAAUS2L"
            }
        };

        var result = await client.PostAsJsonAsync("/verifyBeneficiaryAccount", request);
        Assert.Equal(HttpStatusCode.OK, result.StatusCode);
    }
}