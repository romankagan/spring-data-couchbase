package org.springframework.data.couchbase.repository;

import org.springframework.data.couchbase.core.mapping.annotation.Id;
import org.springframework.data.couchbase.core.mapping.id.GeneratedValue;
import org.springframework.data.couchbase.core.mapping.id.GenerationStrategy;

/**
 * @author David Kelly
 */
public class ReactivePlace {
    @Id
    @GeneratedValue(strategy= GenerationStrategy.UNIQUE)
    public String id;

    public String name;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ReactivePlace(String name) {
        this.name = name;
    }

    public ReactivePlace() {}
}
