package com.tagit.mobeix.tools.datacli.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class Geolocation {

	  @NonNull
	  private Double latitude;
	  @NonNull
	  private Double longitude;
	  @NonNull
	  private Double accuracy;
	  private Double altitude;
	  private Double altitudeAccuracy;
	  private Double heading;
	  private Double speed;

}
