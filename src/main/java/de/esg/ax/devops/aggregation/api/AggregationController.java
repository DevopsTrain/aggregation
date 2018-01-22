package de.esg.ax.devops.aggregation.api;

import de.esg.ax.devops.aggregation.api.entity.VehicleStatus;
import de.esg.ax.devops.aggregation.esi.entity.BatteryStatus;
import de.esg.ax.devops.aggregation.esi.entity.GeoPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Spring REST entrypoint
 */
@RestController
public class AggregationController {

	private static final Logger LOG = LoggerFactory.getLogger(AggregationController.class);

	private static final long THRESHOLD_STALE_MS = 600000l; // 10 min threshold until data is considered stale

    private static final String ERROR_TRIGGER_MSG = "Error trigger has been called to provoke an exception";

	@Autowired
	private Environment env;

	/**
	 * Retrieval method to aggregate responses from the backend status services.
	 *
	 * @param vin
	 * 		- the vehicle's identification number
	 * @param restTemplate
	 * 		- automatically injected by spring
	 *
	 * @return the aggregated status reponses
	 */
	@RequestMapping(value = "/api/vehiclestatus/{vin}", method = RequestMethod.GET)
	public VehicleStatus retrieveVehicleStatus(@PathVariable String vin, RestTemplate restTemplate) {
		LOG.info("Received vehiclestatus request for {}", vin);


		// special trigger for throwing errors: vin param is all zeroes (length does not matter)
		if(vin.matches("^0+$")) {
		    // throw in some variance and randomize for different exception types
            switch (ThreadLocalRandom.current().nextInt(3)) {
                case 0:
                    throw new RestClientException(ERROR_TRIGGER_MSG);
                case 1:
                    throw new NullPointerException(ERROR_TRIGGER_MSG);
                case 2:
                    throw new RuntimeException(ERROR_TRIGGER_MSG);
            }
        }

		final long staleEpoch_ms = System.currentTimeMillis() - THRESHOLD_STALE_MS;

		GeoPosition pos = null;
		boolean geoPosIsStale = false;
		try {
			pos = restTemplate
					.getForObject(env.getProperty("aggregation.service.geoposition.endpoint") + vin, GeoPosition.class);
			Long timestampUtc_ms = pos.getTimestampUtc() / 10000; //TODO this is not (yet) an epoch timestamp
			geoPosIsStale = timestampUtc_ms < staleEpoch_ms;
		} catch (RestClientException e) {
			LOG.warn("Calling GeoPosition service failed with exception: {}", e.getMessage());
			//TODO fallback to latest status from local DB
		}

		BatteryStatus bstat = null;
		boolean bstatIsStale = false;

		try {
			bstat = restTemplate.getForObject(env.getProperty("aggregation.service.batterystatus.endpoint") + vin,
					BatteryStatus.class);
			bstatIsStale = bstat.getLastCheck().getTime() < staleEpoch_ms;
		} catch (RestClientException e) {
			LOG.warn("Calling BatteryStatus service failed with exception: {}", e.getMessage());
			//TODO fallback to latest status from local DB
		}

		LOG.info("Responding to vehiclestatus request for {}", vin);

		// if both esis failed to return anything, we will just move on, hystrix will take care of it!
		return new VehicleStatus(vin, (pos != null) ?
				new VehicleStatus.GeoPosition(pos.getLatitude(), pos.getLongitude(), geoPosIsStale) :
				null,
				(bstat != null) ? new VehicleStatus.BatteryStatus(bstat.getChargedPercentage(), bstatIsStale) : null);
	}

	/**
	 * Liveness check if required by application orchestrator.
	 * Always returns HTTP 200 (without any content, so yes, 204 would technically be the correct status code).
	 */
	@RequestMapping(value = "/api/live")
	@ResponseStatus(value = HttpStatus.OK)
	public void liveness() {
		// nothing to do
	}

	/**
	 * Single error handler for now, which returns HTTP 500 for all types of errors.
	 *
	 * @return Simple String message and HTTP 500 on erroneous api invocation
	 */
	@ExceptionHandler(Throwable.class)
	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
	public @ResponseBody
	String handleExceptions(Exception e) {
		LOG.error("An exception occured: " + e.getMessage(),e);
		return "Whooops...";
	}

	/**
	 * Creates a (singleton) Bean with the Spring Rest Client which can then be auto-injected where needed.
	 *
	 * @param builder
	 * 		the builder itself gets injected into this bean
	 *
	 * @return the globally used RestTemplate with (for now) global timeout settings
	 */
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		// set timouts globally for now
		return builder.setConnectTimeout(1500).setReadTimeout(2500).build(); //TODO check or use hystrix instead
	}
}
