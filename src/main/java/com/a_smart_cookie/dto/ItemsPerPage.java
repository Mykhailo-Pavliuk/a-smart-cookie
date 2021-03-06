package com.a_smart_cookie.dto;

import java.util.Arrays;

/**
 * Holder of possible pagination values of items per page.
 *
 */
public enum ItemsPerPage {
	TWO(2),
	SIX(6),
	TEN(10),
	TWENTY(20);

	private final Integer limit;

	ItemsPerPage(Integer limit) {
		this.limit = limit;
	}

	/**
	 * Gets ItemsPerPage value by input string and returns TWO by default, if something went wrong.
	 *
	 * @param limitString String meant to be listed as value of enum.
	 * @return ItemsPerPage enum value by itself.
	 */
	public static ItemsPerPage safeFromString(String limitString) {
		if (limitString == null || !limitString.matches("[0-9]+")) {
			return TWO;
		}

		return Arrays.stream(ItemsPerPage.values())
				.filter(limit -> limit.getLimit().equals(Integer.parseInt(limitString)))
				.findFirst()
				.orElse(TWO);
	}

	public Integer getLimit() {
		return limit;
	}
}
