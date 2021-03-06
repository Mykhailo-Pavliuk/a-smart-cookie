package com.a_smart_cookie.dao.mysql;

import com.a_smart_cookie.dao.EntityColumn;
import com.a_smart_cookie.dao.PublicationDao;
import com.a_smart_cookie.dao.ResourceReleaser;
import com.a_smart_cookie.dto.catalog.CountRowsParameters;
import com.a_smart_cookie.dto.catalog.FilterParameters;
import com.a_smart_cookie.entity.Genre;
import com.a_smart_cookie.entity.Language;
import com.a_smart_cookie.entity.Publication;
import com.a_smart_cookie.exception.DaoException;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Data access object for publication related entities implemented with MySql
 */
public class MysqlPublicationDao extends PublicationDao {

	private static final Logger LOG = Logger.getLogger(MysqlPublicationDao.class);

	@Override
	public List<Publication> findPublicationsByFilterParameters(FilterParameters filterParameters) throws DaoException {
		LOG.debug("MysqlPublicationDao starts finding publications by filter parameters");

		StringBuilder queryBuilder = getQueryWithAppliedFilterParameters(filterParameters);
		LOG.trace("Created query --> " + queryBuilder);

		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			pstmt = connection.prepareStatement(queryBuilder.toString());
			pstmt.setString(1, filterParameters.getLanguage().name().toLowerCase());
			rs = pstmt.executeQuery();

			LOG.debug("MysqlPublicationDao finished finding publications by filter parameters");
			return extractPublications(rs);

		} catch (SQLException e) {
			throw new DaoException("Can't find all publications with query '" + queryBuilder + "'", e);
		} finally {
			ResourceReleaser.close(rs);
			ResourceReleaser.close(pstmt);
		}
	}

	@Override
	public int getTotalNumberOfRequestedQueryRows(CountRowsParameters countRowsParameters) throws DaoException {
		LOG.debug("MysqlPublicationDao starts getting total number of rows by parameters");

		StringBuilder queryBuilder = getQueryWithAppliedCountRowsParameters(countRowsParameters);
		LOG.trace("Created query --> " + queryBuilder);

		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			pstmt = connection.prepareStatement(queryBuilder.toString());
			pstmt.setString(1, countRowsParameters.getLanguage().name().toLowerCase());
			rs = pstmt.executeQuery();

			int numberOfRows = 0;

			if (rs.next()) {
				numberOfRows = rs.getInt("count");
			}

			LOG.debug("MysqlPublicationDao finished getting total number of rows by parameters");
			return numberOfRows;

		} catch (SQLException e) {
			throw new DaoException("Can't count publications with query '" + queryBuilder + "'", e);
		} finally {
			ResourceReleaser.close(rs);
			ResourceReleaser.close(pstmt);
		}
	}



	@Override
	public Optional<Publication> getPublicationWithoutInfoById(int id) throws DaoException {
		LOG.debug("Method starts");

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = connection.prepareStatement(Query.Publication.GET_BY_ID.getQuery());
			pstmt.setInt(1, id);
			rs = pstmt.executeQuery();

			LOG.trace(pstmt);

			if (rs.next()) {
				LOG.trace("Finished --> Found publication");
				return Optional.of(extractPublication(rs));
			}

			LOG.trace("Finished --> Didn't find publication");
			return Optional.empty();

		} catch (SQLException e) {
			throw new DaoException("Can't get publication by id " + id, e);
		} finally {
			ResourceReleaser.close(rs);
			ResourceReleaser.close(pstmt);
		}
	}

	@Override
	public Publication getPublicationWithInfoByIdAndLanguage(int publicationId, Language language) throws DaoException {
		LOG.debug("Method starts");

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = connection.prepareStatement(Query.Publication.GET_PUBLICATION_WITH_INFO_BY_ID_AND_LANGUAGE.getQuery());
			pstmt.setInt(1, publicationId);
			pstmt.setString(2, language.name().toLowerCase());
			rs = pstmt.executeQuery();

			LOG.trace(pstmt);

			if (rs.next()) {
				return extractPublicationWithFullInfo(rs);
			}

			LOG.error("Finished --> Didn't find publication");
			throw new DaoException("Result set is empty");

		} catch (SQLException e) {
			throw new DaoException("Can't get publication by id " + publicationId + " in " + language, e);
		} finally {
			ResourceReleaser.close(rs);
			ResourceReleaser.close(pstmt);
		}
	}

	@Override
	public List<Publication> getPublicationsWithLimitByLanguage(int offset, int numberOfItems, Language language) throws DaoException {
		LOG.debug("Starts method");

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = connection.prepareStatement(Query.Publication.GET_PUBLICATIONS_WITH_INFO_AND_OFFSET_AND_ITEMS_PER_PAGE_BY_LANGUAGE.getQuery());
			pstmt.setString(1, language.name().toLowerCase());
			pstmt.setInt(2, offset);
			pstmt.setInt(3, numberOfItems);

			rs = pstmt.executeQuery();

			LOG.trace(pstmt);

			LOG.trace("Finished with found publications");
			return extractPublications(rs);

		} catch (SQLException e) {
			throw new DaoException("Can't get publications", e);
		} finally {
			ResourceReleaser.close(rs);
			ResourceReleaser.close(pstmt);
		}
	}

	@Override
	public int getTotalNumberOfPublications() throws DaoException {
		LOG.debug("Starts method");

		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			pstmt = connection.prepareStatement(Query.Publication.GET_TOTAL_NUMBER_OF_PUBLICATIONS.getQuery());
			rs = pstmt.executeQuery();

			LOG.debug("Finished method");
			if (rs.next()) {
				return rs.getInt(1);
			}

			throw new DaoException("Result set is empty");

		} catch (SQLException e) {
			throw new DaoException("Can't get number of publications", e);
		} finally {
			ResourceReleaser.close(rs);
			ResourceReleaser.close(pstmt);
		}
	}

	@Override
	public boolean deletePublicationById(int publicationId) throws DaoException {
		LOG.debug("Starts method");

		PreparedStatement pstmt = null;
		try {
			pstmt = connection.prepareStatement(Query.Publication.DELETE_BY_ID.getQuery());
			pstmt.setInt(1, publicationId);

			LOG.trace(pstmt);

			return pstmt.executeUpdate() > 0;

		} catch (SQLException e) {
			throw new DaoException("Can't delete publication by id = '" + publicationId + "'", e);
		} finally {
			ResourceReleaser.close(pstmt);
		}
	}

	@Override
	public boolean updatePublicationGenreAndPricePerMonthById(Genre genre, BigDecimal pricePerMonth, int publicationId) throws DaoException {
		LOG.debug("Starts method");

		PreparedStatement pstmt = null;
		try {
			pstmt = connection.prepareStatement(Query.Publication.UPDATE_PUBLICATION_GENRE_AND_PRICE_PER_MONTH_BY_ID.getQuery());
			pstmt.setString(1, genre.name().toLowerCase());
			pstmt.setBigDecimal(2, pricePerMonth);
			pstmt.setInt(3, publicationId);

			LOG.trace(pstmt);

			return pstmt.executeUpdate() > 0;

		} catch (SQLException e) {
			throw new DaoException("Can't update publication", e);
		} finally {
			ResourceReleaser.close(pstmt);
		}
	}

	@Override
	public boolean updatePublicationInfoByLanguage(String title, String description, int publicationId, Language language) throws DaoException {
		LOG.debug("Starts method");

		PreparedStatement pstmt = null;
		try {
			pstmt = connection.prepareStatement(Query.Publication.UPDATE_PUBLICATION_INFO_BY_ID_AND_LANGUAGE.getQuery());
			pstmt.setString(1, title);
			pstmt.setString(2,description);
			pstmt.setInt(3, publicationId);
			pstmt.setString(4, language.name().toLowerCase());

			LOG.trace(pstmt);

			return pstmt.executeUpdate() > 0;

		} catch (SQLException e) {
			throw new DaoException("Can't update publication info", e);
		} finally {
			ResourceReleaser.close(pstmt);
		}
	}

	@Override
	public int createPublication(int genre_id, BigDecimal pricePerMonth) throws DaoException {
		LOG.debug("Method starts");

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = connection.prepareStatement(Query.Publication.CREATE_PUBLICATION.getQuery(), Statement.RETURN_GENERATED_KEYS);
			pstmt.setInt(1, genre_id);
			pstmt.setBigDecimal(2, pricePerMonth);

			LOG.trace(pstmt);

			if (pstmt.executeUpdate() > 0) {
				rs = pstmt.getGeneratedKeys();
				if (rs.next()) {
					LOG.debug("Method finished");
					return rs.getInt(1);
				}
			}
			LOG.error("Result set is empty");
			throw new DaoException("Result set is empty");

		} catch (SQLException e) {
			throw new DaoException("Can't insert publication", e);
		} finally {
			ResourceReleaser.close(rs);
			ResourceReleaser.close(pstmt);
		}
	}

	@Override
	public boolean createPublicationInfos(int publicationId, Map<Integer, String> titles, Map<Integer, String> descriptions) throws DaoException {
		LOG.debug("Method starts");

		PreparedStatement pstmt = null;
		try {
			pstmt = connection.prepareStatement(Query.Publication.CREATE_PUBLICATION_INFO.getQuery());

			for (Map.Entry<Integer, String> entry : titles.entrySet()) {
				pstmt.setInt(1, publicationId);
				pstmt.setInt(2, entry.getKey());
				pstmt.setString(3, entry.getValue());
				pstmt.setString(4, descriptions.get(entry.getKey()));
				pstmt.addBatch();
			}

			LOG.trace(pstmt);

			LOG.debug("Method finished");
			return pstmt.executeBatch().length == Language.values().length;

		} catch (SQLException e) {
			throw new DaoException("Can't insert publication info", e);
		} finally {
			ResourceReleaser.close(pstmt);
		}
	}

	/**
	 * Construct query with filter parameters for getting requested publications.
	 *
	 * @param filterParameters Possible parameters for constructing query
	 * @return Built query in StringBuilder
	 */
	private StringBuilder getQueryWithAppliedFilterParameters(FilterParameters filterParameters) {
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append(Query.Publication.BUILDER_FIND_ALL_BY_LANGUAGE.getQuery());

		if (filterParameters.getSpecificGenre() != null) {
			queryBuilder.append(" AND genre.name = '")
					.append(filterParameters.getSpecificGenre().name().toLowerCase())
					.append("'");
		}

		if (filterParameters.getSearchedTitle() != null) {
			queryBuilder.append(" AND UPPER(publication_info.title) LIKE UPPER('%")
					.append(filterParameters.getSearchedTitle())
					.append("%') ");
		}

		queryBuilder.append(" ORDER BY ");
		queryBuilder.append(filterParameters.getSortingParameter().getValue());
		queryBuilder.append(" ");
		queryBuilder.append(filterParameters.getSortingDirection());

		queryBuilder.append(" LIMIT ")
				.append(filterParameters.getPaginationOffset())
				.append(", ")
				.append(filterParameters.getItemsPerPage());

		queryBuilder.append(";");

		return queryBuilder;
	}

	/**
	 * Construct query with CountRowsParameters for getting count of requested rows.
	 *
	 * @param countRowsParameters Possible parameters for constructing query
	 * @return Built query in StringBuilder
	 */
	private StringBuilder getQueryWithAppliedCountRowsParameters(CountRowsParameters countRowsParameters) {
		StringBuilder queryBuilder = new StringBuilder();

		queryBuilder.append(Query.Publication.BUILDER_GET_NUMBER_OF_ROWS_FOUNDED_BY_LANGUAGE.getQuery());

		if (countRowsParameters.getGenre() != null) {
			queryBuilder.append(" AND genre.name = '")
					.append(countRowsParameters.getGenre().name().toLowerCase())
					.append("'");
		}

		if (countRowsParameters.getSearchedTitle() != null) {
			queryBuilder.append(" AND UPPER(publication_info.title) LIKE UPPER('%")
					.append(countRowsParameters.getSearchedTitle())
					.append("%') ");
		}

		queryBuilder.append(";");
		return queryBuilder;
	}

	/**
	 * Extracts publications from ResultSet to List of publications.
	 *
	 * @param rs External ResultSet
	 * @return List of extracted publications
	 */
	private List<Publication> extractPublications(ResultSet rs) throws SQLException {
		List<Publication> publications = new ArrayList<>();

		while (rs.next()) {
			publications.add(extractPublicationWithFullInfo(rs));
		}

		return publications;
	}

	/**
	 * Method to extract publication from ResultSet.
	 *
	 * @param rs External ResultSet
	 * @return Extracted publication with publication info
	 */
	private Publication extractPublicationWithFullInfo(ResultSet rs) throws SQLException {
		return new Publication.PublicationBuilder()
				.withId(rs.getInt(EntityColumn.Publication.ID.getName()))
				.withGenre(Genre.safeFromString(rs.getString(EntityColumn.Genre.NAME.getName())))
				.withTitle(rs.getString(EntityColumn.PublicationInfo.TITLE.getName()))
				.withDescription(rs.getString(EntityColumn.PublicationInfo.DESCRIPTION.getName()))
				.withPricePerMonth(rs.getBigDecimal(EntityColumn.Publication.PRICE_PER_MONTH.getName()))
				.build();
	}

	/**
	 * Method to extract short publication from ResultSet.
	 *
	 * @param rs External ResultSet
	 * @return Extracted short publication
	 */
	private Publication extractPublication(ResultSet rs) throws SQLException {
		return new Publication.PublicationBuilder()
				.withId(rs.getInt(EntityColumn.Publication.ID.getName()))
				.withPricePerMonth(rs.getBigDecimal(EntityColumn.Publication.PRICE_PER_MONTH.getName()))
				.build();
	}

}
