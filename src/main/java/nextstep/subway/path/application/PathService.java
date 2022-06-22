package nextstep.subway.path.application;

import java.util.List;
import nextstep.subway.auth.domain.LoginMember;
import nextstep.subway.line.domain.Line;
import nextstep.subway.line.domain.LineRepository;
import nextstep.subway.path.domain.DijkstraShortestPathFinder;
import nextstep.subway.path.domain.Path;
import nextstep.subway.path.domain.ShortestPathFinder;
import nextstep.subway.path.dto.PathResponse;
import nextstep.subway.station.application.StationService;
import nextstep.subway.station.domain.Station;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PathService {
    private final StationService stationService;
    private final LineRepository lineRepository;

    public PathService(StationService stationService, LineRepository lineRepository) {
        this.stationService = stationService;
        this.lineRepository = lineRepository;
    }

    @Transactional(readOnly = true)
    public PathResponse findShortestPath(LoginMember loginMember, Long sourceStationId, Long targetStationId) {
        List<Line> lines = lineRepository.findAll();
        Station sourceStation = stationService.findStationById(sourceStationId);
        Station targetStation = stationService.findStationById(targetStationId);
        ShortestPathFinder shortestPathFinder = new DijkstraShortestPathFinder(lines);
        Path path = shortestPathFinder.getPath(sourceStation, targetStation);
        return PathResponse.of(path, loginMember);
    }
}
