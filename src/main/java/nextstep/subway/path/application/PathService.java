package nextstep.subway.path.application;

import nextstep.subway.line.application.LineService;
import nextstep.subway.line.domain.Line;
import nextstep.subway.path.domain.Path;
import nextstep.subway.path.domain.DijkstraPathFinder;
import nextstep.subway.path.domain.PathFinder;
import nextstep.subway.path.dto.PathResponse;
import nextstep.subway.station.application.StationService;
import nextstep.subway.station.domain.Station;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PathService {
    private final StationService stationService;
    private final LineService lineService;
    private final PathFinder pathFinder;

    public PathService(StationService stationService, LineService lineService, PathFinder pathFinder) {
        this.stationService = stationService;
        this.lineService = lineService;
        this.pathFinder = pathFinder;
    }

    public PathResponse findShortestPath(Long sourceStationId, Long targetStationId) {
        List<Line> lines = lineService.findAllLines();
        Station sourceStation = stationService.findStationById(sourceStationId);
        Station targetStation = stationService.findStationById(targetStationId);

        Path path = pathFinder.findShortestPathWithLines(lines, sourceStation, targetStation);
        return PathResponse.from(path);
    }
}
