package de.esg.ax.devops.aggregation.api.entity;

import lombok.Data;

@Data
public class VehicleStatus {
	private final String vin;

	private final GeoPosition geoPosition;

	private final BatteryStatus batteryStatus;

	@Data
	public static class GeoPosition {
		private final Double latitude;
		private final Double longitude;
		private final boolean stale;
	}

	@Data
	public static class BatteryStatus {
		private final Short chargedPercentage;
		private final boolean stale;
	}
}
