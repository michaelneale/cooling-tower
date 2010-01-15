package jboss.cloud.dns


import java.io.File
import java.net.InetAddress
import java.text.SimpleDateFormat
import java.util.{Calendar, Date}
import org.xbill.DNS._
/**
 * 
 * @author Michael Neale
 */

trait NameRegistry {

  def listDomains: Array[String]
  def listSubDomains(domain: String): Array[String]

  def registerNewDomain(domain: String, defaultAddress: String)
  def changeDefaultAddress(domain: String, defaultAddress: String)

  def bindSubDomain(domain: String, subDomain: String, address: String)
  def aliasSubDomain(domain: String, subDomain: String, targetURI: String)


  def currentAddress(domain: String) : String
  def currentAddress(domain: String, subDomain: String) : String

  /** TODO MX records separately. */
}

/**
 * Manage the database of name registrations for the cloud servers
 * Pass in the root of the directory to hold the zone files, and the IP of the DNS server itself
 */
case class Registrar(rootDirectory: File, dnsServerAddress: String) extends NameRegistry {

  val TTL = 60

  def listSubDomains(domain: String) = null

  def registerNewDomain(domain: String, defaultAddress: String) = {

    val adminEmailAddress = Name.fromString("admin." + domain, Name.root)
    val domainName = Name.fromString(domain, Name.root)
    val serial = new SimpleDateFormat("yyyymmdd").format(new Date).toInt
    val refresh = 86400 //1d
    val expiry = 2419200 //4w
    val retry = 120
    val minimum = 60
    val defaultDNSName = Name.fromString("dns." + domain, Name.root)
    val soa = new SOARecord(domainName, DClass.IN, TTL, defaultDNSName, adminEmailAddress, serial, refresh, retry, expiry, minimum)


    val dnsRecord = new ARecord(defaultDNSName, DClass.IN, 2419200, InetAddress.getByName(dnsServerAddress))
    val nsEntry = new NSRecord(domainName, DClass.IN, 2419200, defaultDNSName)
    val zone = new Zone(domainName, Array[Record](soa, nsEntry, dnsRecord))
    println("ZONE: \n" + zone.toMasterFile)
    zone
  }

  def changeDefaultAddress(domain: String, defaultAddress: String) = null

  def currentAddress(domain: String) = null

  def bindSubDomain(domain: String, subDomain: String, address: String) = null

  def listDomains = rootDirectory.listFiles.map(_.getName) 

  def aliasSubDomain(domain: String, subDomain: String, targetURI: String) = null

  def currentAddress(domain: String, subDomain: String) = null
}