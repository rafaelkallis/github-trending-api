package com.rafaelkallis;

import lombok.*;
import org.springframework.data.annotation.Transient;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by rafaelkallis on 22.09.16.
 */
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Project {

    @Id
    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private Integer commits;

    @Transient
    public Project incrementCommits(){
        commits++;
        return this;
    }
}
