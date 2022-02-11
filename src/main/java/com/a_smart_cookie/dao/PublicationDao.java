package com.a_smart_cookie.dao;

import com.a_smart_cookie.entity.Language;
import com.a_smart_cookie.entity.Publication;
import com.a_smart_cookie.exception.DaoException;

import java.util.List;

public abstract class PublicationDao extends AbstractDao<Publication> {

	public abstract List<Publication> findAllByLanguage(Language language) throws DaoException;

	public abstract List<Publication> findLimitedWithOffsetByLanguage(int limit, int offset, Language  language) throws DaoException;

	public abstract int getTotalNumberOfPublications() throws DaoException;

}