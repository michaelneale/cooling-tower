package jboss.cloud

/**
 * 
 * @author Michael Neale
 */


case class Image2(flavor: Int, var name: String) {
  def setName(s: String) = {
    name = s
  }
}