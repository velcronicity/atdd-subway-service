package nextstep.subway.path.application;

import nextstep.subway.line.domain.Line;
import nextstep.subway.line.domain.LineRepository;
import nextstep.subway.path.domain.PathFinder;
import nextstep.subway.path.dto.PathResponse;
import nextstep.subway.station.application.StationService;
import nextstep.subway.station.domain.Station;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PathService {
    private final StationService stationService;
    private final LineRepository lineRepository;
    private final PathFinder pathFinder;

    public PathService(StationService stationService, LineRepository lineRepository, PathFinder pathFinder) {
        this.stationService = stationService;
        this.lineRepository = lineRepository;
        this.pathFinder = pathFinder;
    }

    public PathResponse findShortestPath(Long sourceStationId, Long targetStationId) {
        Station sourceStation = stationService.findById(sourceStationId);
        Station targetStation = stationService.findById(targetStationId);
        List<Line> lines = lineRepository.findAll();
        return pathFinder.findShortestPath(lines, sourceStation, targetStation);
    }
}
