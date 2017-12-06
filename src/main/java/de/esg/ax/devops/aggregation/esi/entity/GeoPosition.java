package de.esg.ax.devops.aggregation.esi.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeoPosition {
	private final Double latitude;
	private final Double longitude;
	private final Long timestampUtc;
}
