package com.zhiyouxing.common.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 基于用户的协同过滤：用余弦相似度找最近邻，再按加权评分推荐目标用户未收藏过的条目。
 */
public class UserBasedCollaborativeFiltering {

    private static final int NEIGHBOR_SIZE = 20;

    private final Map<String, Map<String, Double>> userRatings;

    public UserBasedCollaborativeFiltering(Map<String, Map<String, Double>> userRatings) {
        this.userRatings = userRatings;
    }

    public List<String> recommendItems(String targetUser, int numRecommendations) {
        Map<String, Double> target = userRatings.getOrDefault(targetUser, Collections.emptyMap());
        List<Map.Entry<String, Double>> neighbors = new ArrayList<>();
        for (Map.Entry<String, Map<String, Double>> entry : userRatings.entrySet()) {
            if (entry.getKey().equals(targetUser)) {
                continue;
            }
            double similarity = similarity(target, entry.getValue());
            if (similarity > 0) {
                neighbors.add(Map.entry(entry.getKey(), similarity));
            }
        }
        neighbors.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        Map<String, Double> weightedScores = new HashMap<>();
        Map<String, Double> similaritySums = new HashMap<>();
        for (int i = 0; i < Math.min(NEIGHBOR_SIZE, neighbors.size()); i++) {
            Map.Entry<String, Double> neighbor = neighbors.get(i);
            for (Map.Entry<String, Double> rating : userRatings.get(neighbor.getKey()).entrySet()) {
                if (target.containsKey(rating.getKey())) {
                    continue;
                }
                weightedScores.merge(rating.getKey(), neighbor.getValue() * rating.getValue(), Double::sum);
                similaritySums.merge(rating.getKey(), Math.abs(neighbor.getValue()), Double::sum);
            }
        }

        return weightedScores.entrySet().stream()
                .filter(entry -> similaritySums.get(entry.getKey()) > 0)
                .sorted((a, b) -> Double.compare(
                        b.getValue() / similaritySums.get(b.getKey()),
                        a.getValue() / similaritySums.get(a.getKey())))
                .limit(numRecommendations)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private double similarity(Map<String, Double> a, Map<String, Double> b) {
        double dot = 0;
        double normA = 0;
        for (Map.Entry<String, Double> entry : a.entrySet()) {
            Double other = b.get(entry.getKey());
            dot += entry.getValue() * (other == null ? 0 : other);
            normA += entry.getValue() * entry.getValue();
        }
        double normB = 0;
        for (double value : b.values()) {
            normB += value * value;
        }
        if (normA == 0 || normB == 0) {
            return 0;
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
