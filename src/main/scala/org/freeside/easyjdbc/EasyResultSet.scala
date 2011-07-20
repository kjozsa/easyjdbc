/**
 *
 */
package org.freeside.easyjdbc

import java.sql.ResultSet
import java.sql.Date
import java.sql.Time
import java.sql.Timestamp
import java.io.InputStream
import java.sql.SQLWarning
import java.sql.ResultSetMetaData
import java.io.Reader
import java.sql.Statement
import java.sql.Ref
import java.sql.RowId
import java.sql.Clob
import java.net.URL
import java.sql.NClob
import java.sql.Blob
import java.sql.SQLXML
import java.util.Calendar

/**
 * @author kjozsa
 */
class EasyResultSet(val rs: ResultSet) {
  private var count = 0

  def withCount[T](block: Int => T) = {
    count += 1
    block(count)
  }

  def nextBoolean = withCount(rs.getBoolean(_))
  def nextByte = withCount(rs.getByte(_))
  def nextInt = withCount(rs.getInt(_))
  def nextLong = withCount(rs.getLong(_))
  def nextFloat = withCount(rs.getFloat(_))
  def nextDouble = withCount(rs.getDouble(_))
  def nextBigDecimal = withCount(rs.getBigDecimal(_))
  def nextTimestamp = withCount(rs.getTimestamp(_))
  def nextDate = withCount(rs.getTimestamp(_))
  def nextString = withCount(rs.getString(_))
}

