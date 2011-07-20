/**
 *
 */
package org.freeside.easyjdbc

import java.sql.ResultSet

/**
 * @author kjozsa
 */
class EasyResultSet(rs: ResultSet) {
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