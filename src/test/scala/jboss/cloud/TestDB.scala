package jboss.cloud


import java.io.File
import mapping.LocalDatabase

/**
 * 
 * @author Michael Neale
 */

object TestDB {

  private def delete(f: File) : Unit = if (f.isDirectory) f.listFiles.map(delete) else f.delete

  def getDB = {
    val ldb = new LocalDatabase
    val root = new File("testdb")
    delete(root)
    ldb.ROOT = root
    ldb
  }

  
}