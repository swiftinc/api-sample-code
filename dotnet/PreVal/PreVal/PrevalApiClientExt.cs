namespace PreVal.Generated
{
    using System.Net.Http;

    public class PreValApiClientExt : PreValApiClient
    {
        public PreValApiClientExt(HttpClient httpClient) : base(httpClient)
        {

        }
    }

    public class Clearing_system_identification
    {
        public string? Code { get; set; }
        public string? Proprietary { get; set; }
    }

    public class Clearing_system_identification2
    {
        public string? Code { get; set; }
        public string? Proprietary { get; set; }
    }

    public class Scheme_name3
    {
        public string? Code { get; set; }
        public string? Proprietary { get; set; }
    }

    public class Postal_address
    {
        public PostalAddressUnstructured? structured { get; set; }
        public PostalAddressUnstructured? unstructured { get; set; }
    }

    public class Account_identification
    {
        public string? iban { get; set; }
        public GenericAccountIdentification1? other { get; set; }
    }

    public class Scheme_name
    {
        public string? Code { get; set; }
        public string? Proprietary { get; set; }
    }

    public class Address_type
    {
        public AddressType2Code? Code { get; set; }
        public GenericIdentification30? Proprietary { get; set; }
    }

    public class Scheme_name2
    {
        public string? Code { get; set; }
        public string? Proprietary { get; set; }
    }
}
