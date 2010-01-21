package jboss.cloud.dns


class DNSIntegrationTest {
  /**
   * get("/naming") returns list of domains under management
   * get("/naming/domain.com") returns list of subdomains
   * get("/naming/domain.com/subdomain") returns its IP or CNAME
   *
   * post("/naming") - post a new zone to manage
   * post("/naming/domain") - post a new subdomain
   * put....
   * delete....
   *
   * Architectural Change:
   * Change RESTEasy to use Application, and have it return the resources in it (programmatic conf). Perhaps have some content providers??
   * Also refactor test helper for HTTP tests into a trait to mix in to the test? 
   *
   */

  
}