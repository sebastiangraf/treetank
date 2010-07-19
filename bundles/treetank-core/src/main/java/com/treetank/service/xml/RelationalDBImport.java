package com.treetank.service.xml;

import java.sql.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <h1>RelationalDBImport</h1>
 * 
 * <p>
 * Import temporal data from a relational database like PostgreSQL, Oracle or
 * MySQL.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 *
 */
public final class RelationalDBImport implements IImport {

  /** Logger. */
  private static final Log LOGGER = LogFactory.getLog(RelationalDBImport.class);

  /** Driver class string. */
  private final String mDriverClass;

  /** Connection URL. */
  private final String mConnURL;

  /** Username for database access. */
  private final String mUserName;

  /** Password for database access. */
  private final String mUserPass;

  /** Database connection. */
  private transient Connection mConnection = null;

  /** SQL query used to extract revisions. */
  private final String mSQLQuery;

  /**
   * Constructor.
   * 
   * @param driverClass
   *                   Driver class used for specific database driver.
   * @param connURL
   *                   URL to connect to.
   * @param userName
   *                   Username credential.
   * @param userPass
   *                   Password credential.
   * @param SQLQuery
   *                   SQL query used to extract revisions.
   */
  public RelationalDBImport(
      final String driverClass,
      final String connURL,
      final String userName,
      final String userPass,
      final String SQLQuery) {
    mDriverClass = driverClass;
    mConnURL = connURL;
    mUserName = userName;
    mUserPass = userPass;
    mSQLQuery = SQLQuery;
    try {
      Class.forName(mDriverClass).newInstance();
      mConnection = DriverManager.getConnection(mConnURL, mUserName, mUserPass);
    } catch (final InstantiationException e) {
      LOGGER.error(e.getMessage(), e);
    } catch (final IllegalAccessException e) {
      LOGGER.error(e.getMessage(), e);
    } catch (final ClassNotFoundException e) {
      LOGGER.error(e.getMessage(), e);
    } catch (final SQLException e) {
      LOGGER.error(e.getMessage(), e);
    }
  }

  @Override
  public void check(final Object database, final Object tsps) {
    try {
      final PreparedStatement prepStatement =
          mConnection.prepareStatement(mSQLQuery);
      final ResultSet result = prepStatement.executeQuery();
      
      while (result.next()) {
        
      }
    } catch (final SQLException e) {
      LOGGER.error(e.getMessage(), e);
    } finally {
      try {
        mConnection.close();
      } catch (final SQLException e) {
        LOGGER.error(e.getMessage(), e);
      }
    }
  }

}
