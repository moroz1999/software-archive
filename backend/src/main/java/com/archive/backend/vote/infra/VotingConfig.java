package com.archive.backend.vote.infra;

import com.archive.backend.vote.domain.AverageVote;
import com.archive.backend.vote.domain.RatingRule;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** The rating rule with the average vote of the archive from the settings. */
@Configuration
@EnableConfigurationProperties(VotingProperties.class)
public class VotingConfig {

    @Bean
    public RatingRule ratingRule(VotingProperties properties) {
        return new RatingRule(new AverageVote(properties.averageVote()));
    }
}
