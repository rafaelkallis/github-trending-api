package com.rafaelkallis;

import com.jayway.jsonpath.JsonPath;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

/**
 * Created by rafaelkallis on 22.09.16.
 */
@Service
@Slf4j
public class DataService {

    @Autowired
    private ProjectRepository projectRepository;

    @Async
    public void refreshHour(int year,
                            int month,
                            int day,
                            int hour) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            List<ClientHttpRequestInterceptor> interceptorList = restTemplate.getInterceptors();
            interceptorList.add((httpRequest, bytes, clientHttpRequestExecution) -> {
                log.info(httpRequest.getURI().toString());
                return clientHttpRequestExecution.execute(httpRequest, bytes);
            });
            restTemplate.setInterceptors(interceptorList);
            byte[] response = restTemplate.getForObject("http://data.githubarchive.org/{year}-{month}-{day}-{hour}.json.gz",
                    byte[].class,
                    year,
                    addLeadingZero(month),
                    addLeadingZero(day),
                    hour);
            byte[] buffer = new byte[1024];
            @Cleanup InputStream in = new GZIPInputStream(new ByteArrayInputStream(response));
            @Cleanup OutputStream out = new ByteArrayOutputStream();
            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            Stream.of(out.toString().split("\n")).forEach(json -> {
                if (JsonPath.read(json, "$.type").equals("PushEvent")) {
                    String name = JsonPath.read(json, "$.repo.name");
                    if (!name.equals("/")) { // if not deleted
                        projectRepository.save(
                                Optional.ofNullable(projectRepository.findOne(name))
                                        .map(Project::incrementCommits)
                                        .orElse(Project.builder().name(name).commits(1).build())
                        );
                    }
                }
            });
        } catch (IOException e) {
            log.error("stream error", e);
        }
    }

    @Async
    public void refreshDay(int year,
                           int month,
                           int day) {
        for (int hour = 0; hour < 2; hour++) {
            refreshHour(year, month, day, hour);
        }
    }

//    @Scheduled(cron = "0 0 0 1/1 * ?")
//    protected void updateScedule() {
//        log.info("running daily pull");
//        Calendar cal = Calendar.getInstance();
//        cal.add(Calendar.DATE, -1);
//        refreshHour(cal.get(Calendar.DAY_OF_MONTH),
//                cal.get(Calendar.MONTH),
//                cal.get(Calendar.YEAR));
//    }

    private String addLeadingZero(int num) {
        return num < 10 ? "0" + num : "" + num;
    }
}
