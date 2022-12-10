package nextstep.subway.path.dto;

import java.util.List;

import nextstep.subway.line.domain.AdditionalFee;
import nextstep.subway.line.domain.Distance;
import nextstep.subway.path.domain.Fare;
import nextstep.subway.station.domain.Station;

public class Path {

    private List<Station> stations;
    private Distance distance;
    private Fare fare;

    public Path(List<Station> stations, Distance distance, AdditionalFee additionalFee) {
        this.stations = stations;
        this.distance = distance;
        this.fare = new Fare(distance).add(additionalFee);
    }

    public List<Station> getStations() {
        return stations;
    }

    public Fare getFare() {
        return fare;
    }

    public Distance getDistance() {
        return distance;
    }
}
