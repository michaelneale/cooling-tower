package jboss.cloud.dns


import java.net.InetAddress
import java.text.SimpleDateFormat
import java.util.Date
import org.apache.commons.io.IOUtils
import org.xbill.DNS._
import org.scala_tools.javautils.Imports._
import java.io.{FileInputStream, FileOutputStream, File}

/**
 * Manage the database of name registrations for the cloud servers
 * Pass in the root of the directory to hold the zone files, and the IP of the DNS server itself
 */
case class Registrar(rootDirectory: File, dnsServerAddress: String) {

  val TTL = 60

  def registerNewDomain(domain: String) = {
    val adminEmailAddress = Name.fromString("admin." + domain, Name.root)
    val domainName = Name.fromString(domain, Name.root)
    val serial = new SimpleDateFormat("yyyyMMdd").format(new Date).toInt
    val refresh = 86400 //1d
    val FOUR_WEEKS = 2419200
    val expiry = FOUR_WEEKS //4w
    val retry = 120
    val minimum = 60
    val primaryDNSName = Name.fromString("dns", domainName) 
    
    /* start of authority */
    val soa = new SOARecord(domainName, DClass.IN, TTL, primaryDNSName, adminEmailAddress, serial, refresh, retry, expiry, minimum)

    /* The NS stuff (should really have 2 of them, but this is the primary) : */
    val primaryDNSARecord = new ARecord(primaryDNSName, DClass.IN, FOUR_WEEKS, InetAddress.getByName(dnsServerAddress))
    val primaryNSRecord = new NSRecord(domainName, DClass.IN, FOUR_WEEKS, primaryDNSName)

    
    val zone = new Zone(domainName, Array[Record](soa, primaryNSRecord, primaryDNSARecord))
    IOUtils.write(zone.toMasterFile, new FileOutputStream(new File(rootDirectory, domain)))
    zone.toMasterFile
  }

  def removeDomain(domain: String) = new File(rootDirectory, domain).delete

  def updateDefaultAddress(domain: String, address: String) = {
    val zone = loadZone(domain)
    val name = Name.fromString(domain, Name.root)
    recordsFor(zone).find(r => r.getName == name && (r.isInstanceOf[ARecord] || r.isInstanceOf[CNAMERecord])).map(zone.removeRecord(_))
    zone.addRecord(makeRecord(name, address))
    saveZone(zone, domain)
  }

  def defaultAddressFor(domain: String) = {
    val zone = loadZone(domain)
    val name = Name.fromString(domain, Name.root)
    getAddress(recordsFor(zone).find(r => r.getName == name && (r.isInstanceOf[ARecord] || r.isInstanceOf[CNAMERecord])))
  }

  def zoneFileFor(domain: String) :String = IOUtils.toString(new FileInputStream(new File(rootDirectory, domain)))
  def listDomains = rootDirectory.listFiles.map(_.getName)


  /** Show a list of subdomains, excluding DNS and such */
  def listSubDomains(domain: String) = {
    val zone = loadZone(domain)
    recordsFor(zone)
            .filter(r => r.isInstanceOf[ARecord] || r.isInstanceOf[CNAMERecord])
            .filter(_.getName.toString != domain)
            .map(_.getName.toString.split("\\.")(0))
            .filter(!_.startsWith("dns"))   //do not want DNS or default entries showing up
  }

  /** Probably can have optional params for TTL etc... */
  def updateSubDomain(domain: String, subDomain: String, address: String)  = {
    val zone = loadZone(domain)
    val name = Name.fromString(subDomain, zone.getOrigin)
    recordsFor(zone).find(_.getName == name).map(zone.removeRecord(_))
    zone.addRecord(makeRecord(name, address))
    saveZone(zone, domain)
  }

  private def makeRecord(name: Name, address: String) = {
    if (address.matches("[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+") || address.indexOf(":") > -1) {
      new ARecord(name, DClass.IN, TTL, InetAddress.getByName(address))
    } else {
      new CNAMERecord(name, DClass.IN, TTL, Name.fromString(address, Name.root))
    }
  }

  def subDomainAddress(domain: String, subDomain: String) = {
    val name = Name.fromString(subDomain, Name.fromString(domain, Name.root))
    getAddress(recordsFor(loadZone(domain)).find(_.getName == name))
  }

  private def getAddress(rec: Option[Record]) : String = {
    rec match {
      case Some(record) => record match {
        case a: ARecord => a.getAddress.getHostAddress
        case c: CNAMERecord => {
          val nm = c.getTarget.toString
          nm.substring(0, nm.size - 1)
        }
        case _ => ""
      }
      case None => ""
    }
  }

  def removeSubDomain(domain: String, subDomain: String) = {
    val zone = loadZone(domain)
    val name = Name.fromString(subDomain, Name.fromString(domain, Name.root))    
    recordsFor(zone).filter(_.getName == name).map(zone.removeRecord(_))
    saveZone(zone, domain)
  }


  private def loadZone(domain: String) : Zone = new Zone(Name.fromString(domain, Name.root), new File(rootDirectory, domain).getPath)

  /** Update serial and save */
  private def saveZone(zone: Zone, domain: String) =  {
    val old = zone.getSOA
    zone.removeRecord(old)
    val soa = new SOARecord(old.getName, DClass.IN, TTL, old.getHost, old.getAdmin, old.getSerial + 1, old.getRefresh, old.getRetry, old.getExpire, old.getMinimum)
    zone.addRecord(soa)
    IOUtils.write(zone.toMasterFile, new FileOutputStream(new File(rootDirectory, domain)))
  }


  private def recordsFor(z: Zone) : Seq[Record] = {
    val ls = new java.util.ArrayList[Record]
    val it = z.iterator
    while (it.hasNext) {
      val rec = it.next.asInstanceOf[RRset]
      val recs = rec.rrs
      while(recs.hasNext) {
        val rec = recs.next      
        ls.add(rec.asInstanceOf[Record])
      }
    }
    ls.asScala
  }

  


}