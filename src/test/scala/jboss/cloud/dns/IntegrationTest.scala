package jboss.cloud.dns


class DNSIntegrationTest {
  /**
   * get("/naming") returns list of domains under management, and links to zone info, current default address
   * get("/naming/domain.com") returns list of subdomains
   * get("/naming/domain.com/zone") returns its IP or CNAME
   * get("/naming/domain.com/zone") returns its IP or CNAME
   *
   * post("/naming") - post a new zone to manage
   * post("/naming/domain") - post a new subdomain
   * put....
   * delete....
   *
   * Architectural Change:
   *
   *  Also refactor test helper for HTTP tests into a trait to mix in to the test?
   *  Need to think what top level 4 features are, and how they are URL structured...
   *
   *
   */

  
}