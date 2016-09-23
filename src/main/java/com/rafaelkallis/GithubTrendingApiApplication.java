package com.rafaelkallis;

import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@SpringBootApplication
@RestController
@Slf4j
@EnableAsync
@EnableScheduling
public class GithubTrendingApiApplication {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private DataService dataService;

    public static void main(String[] args) {
        SpringApplication.run(GithubTrendingApiApplication.class, args);
    }

    @RequestMapping(path = "/", method = RequestMethod.GET)
    public List<Project> getProjects(@RequestParam(name = "page", defaultValue = "0") int page,
                                     @RequestParam(name = "size", defaultValue = "20") int size) {
        return projectRepository.findAllByOrderByCommitsDesc(new PageRequest(page, size)).getContent();
    }

    @RequestMapping(path = "/refresh", method = RequestMethod.PUT)
    public void updateData() {
        log.info("manual refresh");
        DateTime yesterday = new DateTime().minusDays(1);
        dataService.refreshDay(yesterday.getYear(),
                yesterday.getMonthOfYear(),
                yesterday.getDayOfMonth());
    }
}
