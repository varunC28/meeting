package com.cluely.meeting.spec;

import com.cluely.meeting.entity.MeetingEntity;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.UUID;

public class MeetingSpecifications {

    public static Specification<MeetingEntity> hasUser(UUID userId) {
        return (root, query, cb) -> userId == null ? null : cb.equal(root.get("userId"), userId);
    }

    public static Specification<MeetingEntity> titleContains(String title) {
        return (root, query, cb) -> (title == null || title.isBlank()) ? null
                : cb.like(
                        cb.lower(root.get("title")),
                        "%" + title.toLowerCase() + "%");
    }

    public static Specification<MeetingEntity> hasSource(String source) {
        return (root, query, cb) -> (source == null || source.isBlank()) ? null : cb.equal(root.get("source"), source);
    }

    public static Specification<MeetingEntity> createdAfter(LocalDateTime from) {
        return (root, query, cb) -> from == null ? null : cb.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    public static Specification<MeetingEntity> createdBefore(LocalDateTime to) {
        return (root, query, cb) -> to == null ? null : cb.lessThanOrEqualTo(root.get("createdAt"), to);
    }

    public static Specification<MeetingEntity> notDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

}
