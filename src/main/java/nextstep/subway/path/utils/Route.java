package nextstep.subway.path.utils;

import nextstep.subway.line.domain.Section;
import nextstep.subway.station.domain.Station;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.WeightedMultigraph;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

public class Route {
    private final WeightedMultigraph<Station, DefaultWeightedEdge> graph
            = new WeightedMultigraph<>(DefaultWeightedEdge.class);

    public GraphPath<Station, DefaultWeightedEdge> getShortestRoute(final Station startStation, final Station endStation) {
        if (Objects.equals(startStation, endStation)) {
            throw new IllegalStateException("출발역과 도착역을 달라야 된다.");
        }
        makeEdge(endStation);
        if (!isContainVertex(startStation)) {
            throw new IllegalStateException("경로를 검색할 수 없습니다.");
        }
        return new DijkstraShortestPath<>(graph).getPath(endStation, startStation);
    }

    private boolean isContainVertex(final Station start) {
        return graph.vertexSet().stream()
                .anyMatch(it -> Objects.equals(it, start));
    }

    private void makeEdge(final Station endStation) {
        final Queue<Station> queue = new LinkedList<>();
        queue.add(endStation);

        while (!queue.isEmpty()) {
            queue.poll().getSections().stream()
                    .peek(this::setVertexAndEdgeWeight)
                    .map(Section::getUpStation)
                    .forEach(queue::add);
        }
    }

    private void setVertexAndEdgeWeight(final Section section) {
        graph.addVertex(section.getUpStation());
        graph.addVertex(section.getDownStation());
        graph.setEdgeWeight(graph.addEdge(section.getUpStation(), section.getDownStation()), section.getDistance().of());
    }
}
