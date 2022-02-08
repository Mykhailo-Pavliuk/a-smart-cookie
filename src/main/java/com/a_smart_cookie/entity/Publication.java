package com.a_smart_cookie.entity;

import java.math.BigDecimal;

public final class Publication extends Entity {
    private static final long serialVersionUID = 5722602323493897338L;
    private final Genre genre;
    private final String title;
    private final String description;
    private final BigDecimal pricePerMonth;

    public Publication(Genre genre, String title, String description, BigDecimal pricePerMonth) {
        this.genre = genre;
        this.title = title;
        this.description = description;
        this.pricePerMonth = pricePerMonth;
    }

    public Genre getGenre() {
        return genre;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPricePerMonth() {
        return pricePerMonth;
    }

	@Override
	public String toString() {
		return "Publication{" +
				"genre=" + genre +
				", title='" + title + '\'' +
				", description='" + description + '\'' +
				", pricePerMonth=" + pricePerMonth +
				'}';
	}
}
