package com.a_smart_cookie.dao;

/**
 * Entity column names holder.
 *
 */
public final class EntityColumn {

	/**
	 * Holds column names of Genre entity.
	 *
	 */
	public enum Genre {
		ID("id"),
		NAME("name");

		private final String name;

		Genre(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	/**
	 * Holds column names of Publication entity.
	 *
	 */
	public enum Publication {
		ID("id"),
		PRICE_PER_MONTH("price_per_month");

		private final String name;

		Publication(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	/**
	 * Holds column names of PublicationInfo entity.
	 *
	 */
	public enum PublicationInfo {
		TITLE("title"),
		DESCRIPTION("description");

		private final String name;

		PublicationInfo(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	private EntityColumn() {}

}
