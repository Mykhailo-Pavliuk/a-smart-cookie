package com.a_smart_cookie.dao;

import com.a_smart_cookie.dao.pool.StandardConnectionPool;
import com.a_smart_cookie.exception.DaoException;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;

/**
 * Performs transactions and sets connections on AbstractDao.
 * Can perform regular requests and with transaction ones.
 */
public final class EntityTransaction {

	private static final Logger LOG = Logger.getLogger(EntityTransaction.class);

    private Connection connection;

	/**
	 * Regular initializes connection on dao.
	 *
	 * @param dao Dao which connection should be set for.
	 */
    public void init(AbstractDao dao) throws DaoException {
		LOG.debug("Entity transaction starts regular init");
		initializeConnection();
		dao.setConnection(connection);
		LOG.debug("Entity transaction finished regular init");
    }

	/**
	 * Ends regular request with closing connection.
	 */
	public void end() {
		LOG.debug("Entity transaction starts regular end");
        if (connection != null) {
            ResourceReleaser.close(connection);
        }
		LOG.debug("Entity transaction starts regular end");
    }

	/**
	 * Initializes transaction with initialising connections and setting auto commit to 'false'.
	 *
	 * @param dao Required dao
	 * @param daos Optional vararg parameter for adding necessary number of daos.
	 */
    public void initTransaction(AbstractDao dao, AbstractDao... daos) {
		LOG.debug("Entity transaction starts initializing transaction");
		initializeConnection();

		try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
			LOG.error("Entity transaction can't switch off auto committing", e);
        }

        dao.setConnection(connection);
        for (AbstractDao daoElement : daos) {
            daoElement.setConnection(connection);
        }
		LOG.debug("Entity transaction finished initializing transaction");
    }

	/**
	 * Ends transaction with closing connections and setting auto commit to 'true' back.
	 */
    public void endTransaction() {
		LOG.debug("Entity transaction starts ending transaction");
        if (connection != null) {
            try {
                connection.setAutoCommit(true);
				LOG.debug("Entity transaction finished ending transaction");
            } catch (SQLException e) {
				LOG.error("Entity transaction can't switch on auto committing", e);
            }
            ResourceReleaser.close(connection);
        }
    }

	/**
	 * Commits transaction changes.
	 */
    public void commit() {
		LOG.debug("Entity transaction starts commit");
        try {
            connection.commit();
			LOG.debug("Entity transaction finished commit");
        } catch (SQLException e) {
			LOG.error("Entity transaction can't make commit", e);
        }
    }

	/**
	 * Rollback transaction changes.
	 */
    public void rollback() {
		LOG.debug("Entity transaction starts rollback");
        try {
            connection.rollback();
			LOG.debug("Entity transaction finished rollback");
        } catch (SQLException e) {
			LOG.error("Entity transaction can't make rollback", e);
        }
    }

	/**
	 * Rollback transaction changes to savepoint.
	 *
	 * @param savepoint Savepoint which rollback to.
	 */
	public void rollback(Savepoint savepoint) {
		LOG.debug("Starts rollback to savepoint");
		try {
			connection.rollback(savepoint);
			LOG.debug("Finished rollback to savepoint");
		} catch (SQLException e) {
			LOG.error("Can't make rollback to savepoint", e);
		}
	}

	/**
	 * Sets savepoint to connection.
	 *
	 * @return Created savepoint.
	 * @throws DaoException Throws when can't set savepoint.
	 */
	public Savepoint setSavepoint() throws DaoException {
		LOG.debug("Starts creating savepoint");
		try {
			return connection.setSavepoint();
		} catch (SQLException e) {
			throw new DaoException("Can't set savepoint");
		}
	}

	private void initializeConnection() {
		LOG.debug("Entity transaction starts initializing connection");
		if (connection == null) {
			try {
				connection = StandardConnectionPool.getConnection();
				LOG.debug("Entity transaction finished initializing connection");
			} catch (SQLException e) {
				LOG.error("Entity transaction can't initialize connection", e);
			}
		}
	}
}
